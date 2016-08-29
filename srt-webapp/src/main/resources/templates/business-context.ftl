<#import "default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="x_panel">
            <div class="x_title">
                <h2>Business Context</h2>
            </div>
            <div class="x_content">
                <table id="td-business-context" class="table table-hover table-bordered">
                    <thead>
                    <tr>
                        <th>GUID</th>
                        <th>Name</th>
                        <th>Creation Date</th>
                        <th>Last Update Date</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list businessContexts as businessContext>
                        <tr>
                            <td>${businessContext.guid}</td>
                            <td>${businessContext.name}</td>
                            <td>${businessContext.creationTimestamp}</td>
                            <td>${businessContext.lastUpdateTimestamp}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>

            </div>
        </div>
    </div>
</div>
<script src="/static/srt/js/business-context.js"></script>
</@layout.content>