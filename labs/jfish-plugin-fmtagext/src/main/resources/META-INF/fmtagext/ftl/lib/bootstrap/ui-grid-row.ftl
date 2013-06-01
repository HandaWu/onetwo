<tr>
<#assign __rowData__ = __this__.component.getUIValue(__this__.data)/>
<#list row.columns as field>
	<td <@helper.td_attributes field/> ><#t>
		<#if field.hasContainer()>
			<@jfishui component=field.container data=__rowData__/>
		<#else>
			${field.getUIValue(__rowData__)}
		</#if>
	</td>
</#list><#t>
</tr>
