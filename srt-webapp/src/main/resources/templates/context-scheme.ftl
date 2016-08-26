<#import "default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="x_panel">
            <div class="x_title">
                <h2>Context Scheme</h2>
            </div>
            <div class="x_content">
                <table class="table table-hover table-bordered">
                    <thead>
                    <tr>
                        <th>GUID</th>
                        <th>Description</th>
                        <th>Version</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if contextSchemes?has_content>
                        <tr>
                            <#list contextSchemes as contextScheme>
                            <tr>
                                <td>${contextScheme.guid}</td>
                                <td>${contextScheme.description}</td>
                                <td>${contextScheme.version}</td>
                            </tr>
                            </#list>
                        </tr>
                        <#else>
                        <tr>
                            <td colspan="3" style="text-align: center;">No context scheme defined</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>

            </div>
        </div>
    </div>
</div>
</@layout.content>