<#import "default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="x_panel">
            <div class="x_title">
                <h2>Context Category</h2>
            </div>
            <div class="x_content">
                <table id="td-context-category" class="table table-hover table-bordered">
                    <thead>
                    <tr>
                        <th>GUID</th>
                        <th>Name</th>
                        <th>Description</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list contextCategories as contextCategory>
                        <tr>
                            <td>${contextCategory.guid}</td>
                            <td>${contextCategory.name}</td>
                            <td>${contextCategory.description}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>

            </div>
        </div>
    </div>
</div>
<script src="/static/srt/js/context-category.js"></script>
</@layout.content>