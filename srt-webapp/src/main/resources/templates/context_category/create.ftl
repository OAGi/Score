<#import "../default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Create Context Category</h3>
            </div>
            <div class="panel-body">
                <form id="create-context-category-form" action="/context-category/create" method="POST" data-async
                      data-toggle="validator" role="form">
                    <div class="form-group">
                        <label for="name">Name :</label>
                        <input type="text" id="name" class="form-control" name="name" required="">
                    </div>

                    <div class="form-group">
                        <label for="description">Description :</label>
                        <textarea id="description" required="required" class="form-control"
                                  name="description"></textarea>
                    </div>

                    <div class="form-group">
                        <button type="submit" class="btn btn-primary">Create</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script src="/static/bower_components/bootstrap-validator/dist/validator.min.js"></script>
<script src="/static/bower_components/spin.js/spin.min.js"></script>
<script src="/static/srt/js/context_category/create.js"></script>
</@layout.content>