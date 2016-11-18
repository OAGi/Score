$(document).ready(function () {
    $('#td-code-list').DataTable({
        "order": [[0, "asc"]],
        "pagingType": "full_numbers",
        "language": {
            "emptyTable": "No code list data"
        }
    });
});