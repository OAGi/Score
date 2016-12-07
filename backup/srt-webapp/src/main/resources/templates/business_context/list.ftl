<#import "../default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Business Context</h3>
            </div>
            <div class="panel-body">
                <table id="td-business-context" class="table table-hover table-bordered">
                    <thead>
                    <tr>
                        <th>GUID</th>
                        <th>Name</th>
                        <th>Created Date</th>
                        <th>Last Updated Date</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list businessContexts as businessContext>
                        <tr>
                            <td>${businessContext.guid}</td>
                            <td>${businessContext.name}</td>
                            <td>${businessContext.creationTimestamp?string["yyyy-MM-dd hh:mm:ss a"]}</td>
                            <td>${businessContext.lastUpdateTimestamp?string["yyyy-MM-dd hh:mm:ss a"]}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script src="/static/srt/js/business_context/list.js"></script>
</@layout.content>