/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.ext.beans;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import freemarker.template.TemplateModelException;

class MethodMap
{
    private static final Class BIGDECIMAL_CLASS = java.math.BigDecimal.class;
    private static final Class NUMBER_CLASS = java.lang.Number.class;
    
    private static final Object[] EMPTY_ARGS = new Object[0];
    private static final Class OBJECT_CLASS = java.lang.Object.class;
    private static final ClassString EMPTY_STRING = new ClassString(EMPTY_ARGS);    
    
    private static final Object NO_SUCH_METHOD = new Object();
    private static final Object AMBIGUOUS_METHOD = new Object();
    
    private final String name;
    // Cache of Class[] --> AccessibleObject. Maps the actual types involved in
    // a method/constructor call to the most specific method/constructor for 
    // those types
    private final Map selectorCache = new HashMap();
    private Class[][] unwrapTypes;
    private final List methods = new LinkedList();
    
    MethodMap(String name)
    {
        this.name = name;
    }
    
    void addMethod(Method method)
    {
        methods.add(method);
        updateUnwrapTypes(method.getParameterTypes());
    }
    
    void addConstructor(Constructor constructor)
    {
        methods.add(constructor);
        updateUnwrapTypes(constructor.getParameterTypes());
    }
    
    private void updateUnwrapTypes(Class[] argTypes)
    {
        int l = argTypes.length - 1;
        if(l == -1)
        {
            return;
        }
        if(unwrapTypes == null)
        {
            unwrapTypes = new Class[l + 1][];
            unwrapTypes[l] = argTypes;
        }
        else if(unwrapTypes.length <= l)
        {
            Class[][] newUnwrapTypes = new Class[l + 1][];
            System.arraycopy(unwrapTypes, 0, newUnwrapTypes, 0, unwrapTypes.length);
            unwrapTypes = newUnwrapTypes;
            unwrapTypes[l] = argTypes;
        }
        else
        {
            Class[] oldTypes = unwrapTypes[l]; 
            if(oldTypes == null)
            {
                unwrapTypes[l] = argTypes;
            }
            else
            {
                for(int i = 0; i < oldTypes.length; ++i)
                {
                    oldTypes[i] = getMostSpecificCommonType(oldTypes[i], argTypes[i]);
                }
            }
        }
    }
    
    String getName()
    {
        return name;
    }
    
    Class[] getUnwrapTypes(List args) throws TemplateModelException
    {
        int l = args.size() - 1;
        if(l == -1) {
            return EMPTY_STRING.getClasses();
        }
        if(l < unwrapTypes.length) {
            Class[] retval = unwrapTypes[l];
            if(retval != null) {
                return retval;
            }
        }
        throw new TemplateModelException("No signature of method " + 
                name + " accepts " + (l + 1) + " arguments");
    }
    
    AccessibleObject getMostSpecific(final Object[] args)
        throws TemplateModelException
    {
        ClassString cs = null;
        if(args == null)
        {
            cs = EMPTY_STRING;
        }
        else
        {
            cs = new ClassString(args);
        }
        synchronized(selectorCache)
        {
            Object obj = selectorCache.get(cs);
            if(obj == null)
            {
                selectorCache.put(cs, obj = cs.getMostSpecific(methods));
            }
            if(obj instanceof AccessibleObject)
            {
                return (AccessibleObject)obj;
            }
            if(obj == NO_SUCH_METHOD)
            {
                throw new TemplateModelException("No signature of method " + 
                        name + " matches " + cs.listArgumentTypes());
            }
            else
            {
                // Can be only AMBIGUOUS_METHOD
                throw new TemplateModelException(
                        "Multiple signatures of method " + name + " match " + 
                        cs.listArgumentTypes());
            }
        }
    }
    
    private static final class ClassString
    {
        private final Class[] classes;
        
        ClassString(Object[] objects)
        {
            int l = objects.length;
            classes = new Class[l];
            for(int i = 0; i < l; ++i)
            {
                Object obj = objects[i];
                classes[i] = obj == null ? OBJECT_CLASS : obj.getClass();
            }
        }
        
        Class[] getClasses()
        {
            return classes;
        }
        
        public int hashCode()
        {
            int hash = 0;
            for(int i = 0; i < classes.length; ++i)
            {
                hash ^= classes[i].hashCode();
            }
            return hash;
        }
        
        public boolean equals(Object o)
        {
            if(o instanceof ClassString)
            {
                ClassString cs = (ClassString)o;
                if(cs.classes.length != classes.length)
                {
                    return false;
                }
                for(int i = 0; i < classes.length; ++i)
                {
                    if(cs.classes[i] != classes[i])
                    {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        
        private static final int MORE_SPECIFIC = 0;
        private static final int LESS_SPECIFIC = 1;
        private static final int INDETERMINATE = 2;
        
        Object getMostSpecific(List methods)
        {
            LinkedList applicables = getApplicables(methods);
            if(applicables.isEmpty())
            {
                return NO_SUCH_METHOD;
            }
            if(applicables.size() == 1)
            {
                return applicables.getFirst();
            }
            LinkedList maximals = new LinkedList();
            for (Iterator applicable = applicables.iterator(); 
                 applicable.hasNext();)
            {
                Object objapp = applicable.next();
                Class[] appArgs = getParameterTypes(objapp);
                boolean lessSpecific = false;
                for (Iterator maximal = maximals.iterator(); 
                     !lessSpecific && maximal.hasNext();)
                {
                    Object max = maximal.next();
                    switch(moreSpecific(appArgs, getParameterTypes(max)))
                    {
                        case MORE_SPECIFIC:
                        {
                            maximal.remove();
                            break;
                        }
                        case LESS_SPECIFIC:
                        {
                            lessSpecific = true;
                            break;
                        }
                    }
                }
                if(!lessSpecific)
                {
                    maximals.addLast(objapp);
                }
            }
            if(maximals.size() > 1)
            {
                return AMBIGUOUS_METHOD;
            }
            return maximals.getFirst();
        }
        
        private static Class[] getParameterTypes(Object obj)
        {
            if(obj instanceof Method)
            {
                return ((Method)obj).getParameterTypes();
            }
            if(obj instanceof Constructor)
            {
                return ((Constructor)obj).getParameterTypes();
            }
            // Cannot happen
            throw new Error();
        }
        
        private static int moreSpecific(Class[] c1, Class[] c2)
        {
            boolean c1MoreSpecific = false;
            boolean c2MoreSpecific = false;
            for(int i = 0; i < c1.length; ++i)
            {
                if(c1[i] != c2[i])
                {
                    c1MoreSpecific = 
                        c1MoreSpecific ||
                        isMoreSpecific(c1[i], c2[i]);
                    c2MoreSpecific = 
                        c2MoreSpecific ||
                        isMoreSpecific(c2[i], c1[i]);
                }
            }
            if(c1MoreSpecific)
            {
                if(c2MoreSpecific)
                {
                    return INDETERMINATE;
                }
                return MORE_SPECIFIC;
            }
            if(c2MoreSpecific)
            {
                return LESS_SPECIFIC;
            }
            return INDETERMINATE;
        }
        
        
        /**
         * Returns all methods that are applicable to actual
         * parameter classes represented by this ClassString object.
         */
        LinkedList getApplicables(List methods)
        {
            LinkedList list = new LinkedList();
            for (Iterator imethod = methods.iterator(); imethod.hasNext();)
            {
                Object method = imethod.next();
                if(isApplicable(method))
                {
                    list.add(method);
                }
                
            }
            return list;
        }
        
        /**
         * Returns true if the supplied method is applicable to actual
         * parameter classes represented by this ClassString object.
         * 
         */
        private boolean isApplicable(Object method)
        {
            Class[] methodArgs = getParameterTypes(method);
            if(methodArgs.length != classes.length)
            {
                return false;
            }
            for(int i = 0; i < classes.length; ++i)
            {
                if(!isMethodInvocationConvertible(methodArgs[i], classes[i]))
                {
                    return false;
                }
            }
            return true;
        }
        
        /**
         * Determines whether a type represented by a class object is
         * convertible to another type represented by a class object using a 
         * method invocation conversion, treating object types of primitive 
         * types as if they were primitive types (that is, a Boolean actual 
         * parameter type matches boolean primitive formal type). This behavior
         * is because this method is used to determine applicable methods for 
         * an actual parameter list, and primitive types are represented by 
         * their object duals in reflective method calls.
         * @param formal the formal parameter type to which the actual 
         * parameter type should be convertible
         * @param actual the actual parameter type.
         * @return true if either formal type is assignable from actual type, 
         * or formal is a primitive type and actual is its corresponding object
         * type or an object type of a primitive type that can be converted to
         * the formal type.
         */
        private static boolean isMethodInvocationConvertible(Class formal, Class actual)
        {
            // Check for identity or widening reference conversion
            if(formal.isAssignableFrom(actual))
            {
                return true;
            }
            // Check for boxing with widening primitive conversion. Note that 
            // actual parameters are never primitives.
            if(formal.isPrimitive())
            {
                if(formal == Boolean.TYPE && actual == Boolean.class)
                    return true;
                if(formal == Character.TYPE && actual == Character.class)
                    return true;
                if(formal == Byte.TYPE && actual == Byte.class)
                    return true;
                if(formal == Short.TYPE &&
                   (actual == Short.class || actual == Byte.class))
                    return true;
                if(formal == Integer.TYPE && 
                   (actual == Integer.class || actual == Short.class || 
                    actual == Byte.class))
                    return true;
                if(formal == Long.TYPE && 
                   (actual == Long.class || actual == Integer.class || 
                    actual == Short.class || actual == Byte.class))
                    return true;
                if(formal == Float.TYPE && 
                   (actual == Float.class || actual == Long.class || 
                    actual == Integer.class || actual == Short.class || 
                    actual == Byte.class))
                    return true;
                if(formal == Double.TYPE && 
                   (actual == Double.class || actual == Float.class || 
                    actual == Long.class || actual == Integer.class || 
                    actual == Short.class || actual == Byte.class))
                    return true; 
            }
            // Special case for BigDecimals as we deem BigDecimal to be
            // convertible to any numeric type - either object or primitive.
            // This can actually cause us trouble as this is a narrowing 
            // conversion, not widening. 
            return isBigDecimalConvertible(formal, actual);
        }
        
        private String listArgumentTypes()
        {
            StringBuffer buf = 
                new StringBuffer(classes.length * 32).append('(');
            for(int i = 0; i < classes.length; ++i)
            {
                buf.append(classes[i].getName()).append(',');
            }
            buf.setLength(buf.length() - 1);
            return buf.append(')').toString();
        }
    }

    /**
     * Determines whether a type represented by a class object is 
     * convertible to another type represented by a class object using a 
     * method invocation conversion, without matching object and primitive
     * types. This method is used to determine the more specific type when
     * comparing signatures of methods.
     * @return true if either formal type is assignable from actual type, 
     * or formal and actual are both primitive types and actual can be
     * subject to widening conversion to formal.
     */
    private static boolean isMoreSpecific(Class specific, Class generic)
    {
        // Check for identity or widening reference conversion
        if(generic.isAssignableFrom(specific))
        {
            return true;
        }
        // Check for widening primitive conversion.
        if(generic.isPrimitive())
        {
            if(generic == Short.TYPE && (specific == Byte.TYPE))
                return true;
            if(generic == Integer.TYPE && 
               (specific == Short.TYPE || specific == Byte.TYPE))
                return true;
            if(generic == Long.TYPE && 
               (specific == Integer.TYPE || specific == Short.TYPE || 
                specific == Byte.TYPE))
                return true;
            if(generic == Float.TYPE && 
               (specific == Long.TYPE || specific == Integer.TYPE || 
                specific == Short.TYPE || specific == Byte.TYPE))
                return true;
            if(generic == Double.TYPE && 
               (specific == Float.TYPE || specific == Long.TYPE || 
                specific == Integer.TYPE || specific == Short.TYPE || 
                specific == Byte.TYPE))
                return true; 
        }
        return isBigDecimalConvertible(generic, specific);
    }
    
    private static boolean isBigDecimalConvertible(Class formal, Class actual)
    {
        // BigDecimal 
        if(BIGDECIMAL_CLASS.isAssignableFrom(actual))
        {
            if(NUMBER_CLASS.isAssignableFrom(formal))
            {
                return true;
            }
            if(formal.isPrimitive() && 
               formal != Boolean.TYPE && formal != Character.TYPE)
            {
               return true;
            }
        }
        return false;
    }
    
    private static Class getMostSpecificCommonType(Class c1, Class c2)
    {
        if(c1 == c2) {
            return c1;
        }
        if(c2.isPrimitive()) {
            if(c2 == Byte.TYPE) c2 = Byte.class;
            else if(c2 == Short.TYPE) c2 = Short.class;
            else if(c2 == Character.TYPE) c2 = Character.class;
            else if(c2 == Integer.TYPE) c2 = Integer.class;
            else if(c2 == Float.TYPE) c2 = Float.class;
            else if(c2 == Long.TYPE) c2 = Long.class;
            else if(c2 == Double.TYPE) c2 = Double.class;
        }
        Set a1 = getAssignables(c1, c2);
        Set a2 = getAssignables(c2, c1);
        a1.retainAll(a2);
        if(a1.isEmpty()) {
            // Can happen when at least one of the arguments is an interface, as
            // they don't have Object at the root of their hierarchy
            return Object.class;
        }
        // Gather maximally specific elements. Yes, there can be more than one 
        // thank to interfaces. I.e., if you call this method for String.class 
        // and Number.class, you'll have Comparable, Serializable, and Object as 
        // maximal elements. 
        List max = new ArrayList();
outer:  for (Iterator iter = a1.iterator(); iter.hasNext();) {
            Class clazz = (Class) iter.next();
            for (Iterator maxiter = max.iterator(); maxiter.hasNext();) {
                Class maxClazz = (Class) maxiter.next();
                if(isMoreSpecific(maxClazz, clazz)) {
                    // It can't be maximal, if there's already a more specific
                    // maximal than it.
                    continue outer;
                }
                if(isMoreSpecific(clazz, maxClazz)) {
                    // If it's more specific than a currently maximal element,
                    // that currently maximal is no longer a maximal.
                    maxiter.remove();
                }
            }
            // If we get here, no current maximal is more specific than the
            // current class, so it is considered maximal as well
            max.add(clazz);
        }
        if(max.size() > 1) {
            return OBJECT_CLASS;
        }
        return (Class)max.get(0);
    }

    private static Set getAssignables(Class c1, Class c2)
    {
        Set s = new HashSet();
        collectAssignables(c1, c2, s);
        return s;
    }
    
    private static void collectAssignables(Class c1, Class c2, Set s)
    {
        if(c1.isAssignableFrom(c2)) {
            s.add(c1);
        }
        Class sc = c1.getSuperclass();
        if(sc != null) {
            collectAssignables(sc, c2, s);
        }
        Class[] itf = c1.getInterfaces();
        for(int i = 0; i < itf.length; ++i) {
            collectAssignables(itf[i], c2, s);
        }
    }
}
