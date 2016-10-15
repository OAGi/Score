<#import "../default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Code List</h3>
            </div>
            <div class="panel-body">
                <table id="td-code-list" class="table table-hover table-bordered">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Agency ID</th>
                        <th>Version</th>
                        <th>Extensible</th>
                        <th>State</th>
                        <th>Created Date</th>
                        <th>Last Updated Date</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list codeLists as codeList>
                        <tr>
                            <td>${codeList.name}</td>
                            <td>${codeList.agencyId}</td>
                            <td><#if codeList.version??>${codeList.version}</#if></td>
                            <td><#if codeList.extensibleIndicator == true>True<#else>False</#if></td>
                            <td>${codeList.state}</td>
                            <td>${codeList.creationTimestamp?string["yyyy-MM-dd hh:mm:ss a"]}</td>
                            <td>${codeList.lastUpdateTimestamp?string["yyyy-MM-dd hh:mm:ss a"]}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script src="/static/srt/js/code_list/list.js"></script>
</@layout.content>