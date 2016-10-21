var adjustMainHeight = function () {
    var headerHeight = $('.header').outerHeight();
    var footerHeight = $('.footer').parent().parent().parent().outerHeight();
    var height = $(this).height();

    var mainHeight = height - headerHeight - footerHeight;
    $('.main').css('min-height', mainHeight + "px");
};
adjustMainHeight();
$(window).resize(adjustMainHeight);

var jsf = jsf || {};
if (jsf.ajax) {
    jsf.ajax.addOnError(function (data) {
        console.log(data);
        var response = $.parseJSON(data.responseText);
        if (response.status == 403) {
            location.href = '/';
        }
    });
}