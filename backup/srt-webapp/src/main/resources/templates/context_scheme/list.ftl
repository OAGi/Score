<#import "../default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Context Scheme</h3>
            </div>
            <div class="panel-body">
                <table id="td-context-scheme" class="table table-hover table-bordered">
                    <thead>
                    <tr>
                        <th>GUID</th>
                        <th>Description</th>
                        <th>Version</th>
                        <th>Created Date</th>
                        <th>Last Updated Date</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list contextSchemes as contextScheme>
                        <tr>
                            <td>${contextScheme.guid}</td>
                            <td>${contextScheme.description}</td>
                            <td>${contextScheme.version}</td>
                            <td>${contextScheme.creationTimestamp?string["yyyy-MM-dd hh:mm:ss a"]}</td>
                            <td>${contextScheme.lastUpdateTimestamp?string["yyyy-MM-dd hh:mm:ss a"]}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script src="/static/srt/js/context_scheme/list.js"></script>
</@layout.content>