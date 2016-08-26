<#import "default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="x_panel">
            <div class="x_title">
                <h2>Business Context</h2>
            </div>
            <div class="x_content">
                <table class="table table-hover table-bordered">
                    <thead>
                    <tr>
                        <th>GUID</th>
                        <th>Name</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if businessContexts?has_content>
                        <tr>
                            <#list businessContexts as businessContext>
                            <tr>
                                <td>${businessContext.guid}</td>
                                <td>${businessContext.name}</td>
                            </tr>
                            </#list>
                        </tr>
                        <#else>
                        <tr>
                            <td colspan="2" style="text-align: center;">No business context defined</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>

            </div>
        </div>
    </div>
</div>
</@layout.content>