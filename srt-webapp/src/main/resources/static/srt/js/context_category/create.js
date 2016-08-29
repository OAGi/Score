$(document).ready(function () {
    $('form[data-async]').on('submit', function (event) {
        var $form = $(this);
        var $target = $($form.attr('data-target'));

        $("#content").animate({
            opacity: 0.25
        }, 0, function () {
            var spinner = spin('content');
            $.ajax({
                type: $form.attr('method'),
                url: $form.attr('action'),
                data: $form.serialize(),

                success: function (data, status) {
                    location.href = '/context-category/list';
                }
            }).always(function () {
                if (spinner !== undefined) {
                    spinner.stop();
                }
            });
        });

        event.preventDefault();

    });
});