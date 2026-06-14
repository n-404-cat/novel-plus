$().ready(function () {
    validateRule();
});

$("[id^='picImage']").each(function (index, ele) {
    var relName = $(ele).attr("id").substring(8);
    layui.use('upload', function () {
        var upload = layui.upload;
        //执行实例
        var uploadInst = upload.render({
            elem: '#picImage' + relName, //绑定元素
            url: '/common/sysFile/upload', //上传接口
            size: 1000,
            accept: 'file',
            done: function (r) {
                if (r.code == 0) {
                    $("#picImage" + relName).attr("src", r.fileName);
                    $("#" + relName).val(r.fileName);
                } else {
                    layer.msg(r.msg);
                }
            },
            error: function (r) {
                layer.msg(r.msg);
            }
        });
    });
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

window.clearImg = function(relName) {
    $("#picImage" + relName).attr("src", "/img/webuploader.png");
    $("#" + relName).val("");
};
