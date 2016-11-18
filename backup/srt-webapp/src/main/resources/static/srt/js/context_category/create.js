$(document).ready(function () {
    $('form[data-async]').validator().on('submit', function (e) {
        if (!e.isDefaultPrevented()) {
            $('.alert').hide();

            var $form = $(this);
            var $target = $($form.attr('data-target'));

            $("#content").animate({
                opacity: 0.25
            }, 0, function () {
                var spinner = spin('content');

                $.ajax({
                    type: $form.attr('method'),
                    url: $form.attr('action'),
                    data: $form.serialize()
                }).done(function (data, textStatus, jqXHR) {
                    location.href = '/context-category/list';
                }).fail(function (jqXHR, textStatus, errorThrown) {
                    var resp = jqXHR.responseJSON;

                    if (resp.message.search("'name'") !== -1) {
                        $('#name').focus();
                    } else if (resp.message.search("'description'") !== -1) {
                        $('#description').focus();
                    } else {
                        $('.alert').text(resp.message);
                        $('.alert').show();
                    }

                }).always(function () {
                    if (spinner !== undefined) {
                        spinner.stop();
                    }
                    $("#content").animate({
                        opacity: 1.00
                    }, 0);
                });
            });

            e.preventDefault();
        }
    });
});