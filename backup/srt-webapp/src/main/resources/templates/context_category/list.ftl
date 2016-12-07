<#import "../default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Context Category</h3>
            </div>
            <div class="panel-body">
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
                        <tr class="clickable-row" data-id="${contextCategory.ctxCategoryId}">
                            <td>${contextCategory.guid}</td>
                            <td><#if contextCategory.name??>${contextCategory.name}</#if></td>
                            <td><#if contextCategory.description??>${contextCategory.description}</#if></td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script src="/static/srt/js/context_category/list.js"></script>
</@layout.content>