<#import "default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="x_panel">
            <div class="x_title">
                <h2>Context Scheme</h2>
            </div>
            <div class="x_content">
                <table id="td-context-scheme" class="table table-hover table-bordered">
                    <thead>
                    <tr>
                        <th>GUID</th>
                        <th>Description</th>
                        <th>Version</th>
                        <th>Creation Date</th>
                        <th>Last Update Date</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list contextSchemes as contextScheme>
                        <tr>
                            <td>${contextScheme.guid}</td>
                            <td>${contextScheme.description}</td>
                            <td>${contextScheme.version}</td>
                            <td>${contextScheme.creationTimestamp}</td>
                            <td>${contextScheme.lastUpdateTimestamp}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>

            </div>
        </div>
    </div>
</div>
<script src="/static/srt/js/context-scheme.js"></script>
</@layout.content>