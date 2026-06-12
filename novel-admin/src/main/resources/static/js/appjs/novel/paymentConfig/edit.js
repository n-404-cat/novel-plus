$().ready(function () {
    validateRule();
});

$.validator.setDefaults({
    submitHandler: function () {
        update();
    }
});

function update() {
    $.ajax({
        cache: true,
        type: "POST",
        url: "/novel/paymentConfig/update",
        data: $('#signupForm').serialize(),
        async: false,
        error: function (xhr) {
            layer.alert(xhr && xhr.responseText ? xhr.responseText : "Connection error");
        },
        success: function (data) {
            if (data && data.code == 0) {
                layer.msg(data.msg || "支付配置已保存并生效");
                return;
            }
            if (data && data.msg) {
                layer.alert(data.msg);
                return;
            }
            layer.alert("请求已返回，但响应不是预期的 JSON，请检查是否已登录或是否具备权限。");
        }
    });
}

function validateRule() {
    $("#signupForm").validate({
        ignore: "",
        rules: {
            notifyUrl: {
                url: true
            },
            returnUrl: {
                url: true
            },
            gatewayUrl: {
                url: true
            }
        }
    });
}

window.clearPaymentCache = function () {
    $.ajax({
        cache: true,
        type: "POST",
        url: "/novel/paymentConfig/clearCache",
        dataType: "json",
        async: false,
        error: function (xhr) {
            layer.alert(xhr && xhr.responseText ? xhr.responseText : "Connection error");
        },
        success: function (data) {
            if (data && data.code == 0) {
                layer.msg(data.msg || "支付配置缓存已清理");
                return;
            }
            if (data && data.msg) {
                layer.alert(data.msg);
                return;
            }
            layer.alert("请求已返回，但响应不是预期的 JSON，请检查是否已登录或是否具备权限。");
        }
    });
};
