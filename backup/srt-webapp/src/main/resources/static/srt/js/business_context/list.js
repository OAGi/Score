$(document).ready(function () {
    $('#td-business-context').DataTable({
        "order": [[3, "desc"]],
        "pagingType": "full_numbers",
        "language": {
            "emptyTable": "No business context data"
        }
    });
});