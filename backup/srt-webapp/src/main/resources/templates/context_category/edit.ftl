<#import "../default-layout.ftl" as layout>
<@layout.content>
<div id="content">
    <div class="col-md-12 col-sm-12 col-xs-12">
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Edit Context Category</h3>
            </div>
            <div class="panel-body">
                <div class="alert alert-danger hide">
                    <a href="#" class="close" data-dismiss="alert">&times;</a>
                </div>
                <form id="create-context-category-form"
                      action="/context-category/edit/${contextCategory.ctxCategoryId}"
                      method="POST" data-async data-toggle="validator" role="form"
                      data-id="${contextCategory.ctxCategoryId}">
                    <div class="form-group">
                        <label for="name">Name :</label>
                        <input type="text" id="name" name="name" class="form-control" placeholder="Name"
                               required value="${contextCategory.name}">
                        <div class="help-block with-errors"></div>
                    </div>

                    <div class="form-group">
                        <label for="description">Description :</label>
                        <textarea id="description" name="description" class="form-control"
                                  placeholder="Description" required>${contextCategory.description}</textarea>
                        <div class="help-block with-errors"></div>
                    </div>

                    <div class="form-group">
                        <button type="submit" id="update" class="btn btn-primary">Update</button>
                        <button type="button" id="delete" class="btn btn-danger">Delete</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script src="/static/bower_components/bootstrap-validator/dist/validator.min.js"></script>
<script src="/static/bower_components/spin.js/spin.min.js"></script>
<script src="/static/srt/js/context_category/edit.js"></script>
</@layout.content>