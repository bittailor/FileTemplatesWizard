package ch.bittailor.filetemplates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.bittailor.filetemplates.preferences.PreferenceConstants;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

   // The plug-in ID
   public static final String PLUGIN_ID = "ch.bittailor.filetemplates";

   // The shared instance
   private static Activator fPlugin;

   public static final String FILETEMPLATES_XML = "filetemplates.xml";
   public static final String CLASS_HPP = "class.hpp.ftl";
   public static final String CLASS_CPP = "class.cpp.ftl";



   /**
    * The constructor
    */
   public Activator() {
   }

   /*
    * (non-Javadoc)
    * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
    */
   @Override
   public void start(BundleContext context) throws Exception {
      super.start(context);
      fPlugin = this;
      System.out.println(getTemplateLocation());
   }

   /*
    * (non-Javadoc)
    * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
    */
   @Override
   public void stop(BundleContext context) throws Exception {
      fPlugin = null;
      super.stop(context);
   }

   public String getTemplateLocation() throws IOException{   
      String location = getPreferenceStore().getString(PreferenceConstants.P_TEMPLATES_LOCATION);
      File directory = new File(location);
      if(!directory.exists()) {
         directory.mkdirs();
      }
      File xml = new File(directory,FILETEMPLATES_XML);
      if (!xml.exists()){
         copy(getClass().getResourceAsStream("templates/"+FILETEMPLATES_XML),xml);
         copy(getClass().getResourceAsStream("templates/"+CLASS_HPP),new File(directory,CLASS_HPP));
         copy(getClass().getResourceAsStream("templates/"+CLASS_CPP),new File(directory,CLASS_CPP));
      }
      return location;
   }

   private static void copy(InputStream source, File destination) throws IOException{  
      if(source == null){
         throw new IOException("Resource stream for "+destination.getName()+" is null");
      }
      FileOutputStream out = new FileOutputStream(destination);
      byte[] buffer = new byte[100];	
      int len = 0;
      while((len=source.read(buffer))!=-1 ){
         out.write(buffer, 0, len);
      }
   }

   public static void log(int severity ,String message, Throwable throwable){
      Status status = new Status(severity,Activator.PLUGIN_ID,IStatus.OK,message,throwable);
      getDefault().getLog().log(status);
   }

   public static void logThrowable(Throwable  throwable){
      throwable.printStackTrace();
      Status status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,IStatus.OK,throwable.getMessage(),throwable);
      getDefault().getLog().log(status);
   }

   /**
    * Returns the shared instance
    *
    * @return the shared instance
    */
   public static Activator getDefault() {
      return fPlugin;
   }

   /**
    * Returns an image descriptor for the image file at the given
    * plug-in relative path
    *
    * @param path the path
    * @return the image descriptor
    */
   public static ImageDescriptor getImageDescriptor(String path) {
      return imageDescriptorFromPlugin(PLUGIN_ID, path);
   }
}
