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
      ${string.class}();
      ~${string.class}();
   
   private:
   	  // Constructor to prohibit copy construction.
      ${string.class}(const ${string.class}&);

      // Operator= to prohibit copy assignment
      ${string.class}& operator=(const ${string.class}&);
};

<#if boolean.withNamespace >
   <#list string.namespace?split("::")?reverse as ns >
} // namespace ${ns}
   </#list>
</#if>

#endif // INC__${global.filename?replace("/","_")?replace(".","__")?upper_case}