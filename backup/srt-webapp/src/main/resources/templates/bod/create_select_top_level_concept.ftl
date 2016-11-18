<#import "../default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Select Top-Level Concept</h3>
            </div>
            <div class="panel-body">
                <table id="td-asccp-list" class="table table-hover table-bordered">
                    <thead>
                    <tr>
                        <th>ASCCP Property Term</th>
                        <th>Module</th>
                        <th>Created Date</th>
                        <th>Last Updated Date</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list asccpList as asccp>
                        <tr class="clickable-row" data-id="${asccp.asccpId}">
                            <td>${asccp.propertyTerm}</td>
                            <td><#if asccp.module??>${asccp.module.module}</#if></td>
                            <td>${asccp.creationTimestamp?string["yyyy-MM-dd hh:mm:ss a"]}</td>
                            <td>${asccp.lastUpdateTimestamp?string["yyyy-MM-dd hh:mm:ss a"]}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script src="/static/srt/js/bod/create-profile-bod.js"></script>
</@layout.content>