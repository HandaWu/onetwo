<meta charset="UTF-8">
<div id="addDataDialog" class="easyui-dialog" 
    style="width:50%;height:80%;padding:10px 20px;"
    closed="true" buttons="#dlg-buttons" data-options="modal:true">
           填写[数据字典表]信息<hr/>
       <form id="dataForm" class="easyui-form" action="${siteConfig.baseURL}/configmgr/dictionary.json" method="post" >
            <input id="_method" name="_method" type="hidden" />
           <table cellpadding="5">
                <tr>
                   <td>字典编码:</td>
                    <td>
                        <input class="easyui-textbox" type="text" id="code" name="code" data-options="required:true"/>
                    </td>
               </tr>
                <tr>
                   <td>显示名称:</td>
                    <td>
                        <input class="easyui-textbox" type="text" name="name" data-options="required:true"/>
                    </td>
               </tr>
                <tr>
                   <td>字典值:</td>
                    <td>
                        <input class="easyui-textbox" type="text" name="value" data-options=""/>
                    </td>
               </tr>
                <tr>
                   <td>所属类别:</td>
                    <td>
                        <input class="easyui-textbox" type="text" id="parentCode" name="parentCode" data-options=""/>
                    </td>
               </tr>
                <tr>
                   <td>是否有效:</td>
                    <td>
                       <input class="easyui-combobox" name="isValid"
                                data-options="
                                    required:true,
                                    data: [{value:true, text:'是'}, {value:false, text:'否'}]
                                ">
                        </input>
                    </td>
               </tr>
                <tr>
                   <td>排序:</td>
                    <td>
                        <input class="easyui-textbox" type="text" name="sort" data-options=""/>
                    </td>
               </tr>
                <tr>
                   <td>描述:</td>
                    <td>
                        <input class="easyui-textbox" type="text" name="remark" data-options="multiline:true" style="width:171px;height:100px"/>
                    </td>
               </tr>
           </table>
           <@security.csrfInput/>
       </form>
</div>
<div id="dlg-buttons">
    <a href="#" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveData();">保存</a>
    <a href="#" class="easyui-linkbutton" iconCls="icon-cancel" onclick="cancel();">取消</a>
</div>
<script type="text/javascript">
    function saveData(){
        helper.submitEasyForm({dataForm: '#dataForm',dataDialog: '#addDataDialog', treegrid: '#dataGrid'});
    }
    function cancel(){
        $('#addDataDialog').dialog('close');
        $('#dataForm').form('reset');
    }

</script>
