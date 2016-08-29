$(document).ready(function () {
    $('#td-context-category').DataTable({
        "order": [[1, "asc"]],
        "pagingType": "full_numbers",
        "language": {
            "emptyTable": "No context category data"
        }
    });
});