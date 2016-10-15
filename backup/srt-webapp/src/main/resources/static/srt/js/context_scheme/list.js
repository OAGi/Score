$(document).ready(function () {
    $('#td-context-scheme').DataTable({
        "order": [[4, "desc"]],
        "pagingType": "full_numbers",
        "language": {
            "emptyTable": "No context scheme data"
        }
    });
});