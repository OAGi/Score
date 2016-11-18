$(document).ready(function () {
    var dataTable = $('#td-context-category').DataTable({
        "order": [[1, "asc"]],
        "pagingType": "full_numbers",
        "language": {
            "emptyTable": "No context category data"
        }
    });

    $('#td-context-category tbody').on('click', 'tr', function () {
        if ($(this).hasClass('bg-info')) {
            $(this).removeClass('bg-info');
        }
        else {
            dataTable.$('tr.bg-info').removeClass('bg-info');
            $(this).addClass('bg-info');

            var id = $(this).data('id');
            if (id > 0) {
                location.href = '/context-category/edit/' + id;
            }
        }
    });
});