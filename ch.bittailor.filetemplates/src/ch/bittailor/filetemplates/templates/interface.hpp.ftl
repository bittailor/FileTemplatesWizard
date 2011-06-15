//*************************************************************************************************
//
//  BITTAILOR.CH - ${global.project}     
//
//*************************************************************************************************

#ifndef INC__${global.filename?replace("/","_")?replace(".","__")?upper_case}
#define INC__${global.filename?replace("/","_")?replace(".","__")?upper_case}

<#if boolean.withNamespace >
   <#list string.namespace?split("::") as ns >
namespace ${ns} {
   </#list>
</#if>

class ${string.class} 
{
   public:
      virtual ~${string.class}() {}
      
      virtual void function() = 0;
};

<#if boolean.withNamespace >
   <#list string.namespace?split("::")?reverse as ns >
} // namespace ${ns}
   </#list>
</#if>

#endif // INC__${global.filename?replace("/","_")?replace(".","__")?upper_case}