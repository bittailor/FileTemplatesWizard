//*************************************************************************************************
//
//  BITTAILOR.CH - ${global.project}     
//
//*************************************************************************************************

#include "${global.basename}.hpp"

<#if boolean.withNamespace >
   <#list string.namespace?split("::") as ns >
namespace ${ns} {
   </#list>
</#if>


//-------------------------------------------------------------------------------------------------

${string.class}::${string.class}()
{

}

//-------------------------------------------------------------------------------------------------

${string.class}::~${string.class}()
{

}

//-------------------------------------------------------------------------------------------------

<#if boolean.withNamespace >
   <#list string.namespace?split("::")?reverse as ns >
} // namespace ${ns}
   </#list>
</#if>