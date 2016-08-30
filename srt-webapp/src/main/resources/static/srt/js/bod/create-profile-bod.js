$(document).ready(function () {
    var dataTable = $('#td-asccp-list').DataTable({
        "order": [[0, "asc"]],
        "pagingType": "full_numbers",
        "language": {
            "emptyTable": "No ASCCP data"
        }
    });

    $('#td-asccp-list tbody').on('click', 'tr', function () {
        if ($(this).hasClass('bg-info')) {
            $(this).removeClass('bg-info');
        }
        else {
            dataTable.$('tr.bg-info').removeClass('bg-info');
            $(this).addClass('bg-info');

            var id = $(this).data('id');
            if (id > 0) {

            }
        }
    });
});