var E = window.wangEditor;

$("[id^='contentEditor']").each(function (index, ele) {
    var relName = $(ele).attr("id").substring(13);
    var editor = new E('#contentEditor' + relName);
    editor.customConfig.menus = [
        'head', 'bold', 'fontSize', 'fontName', 'italic', 'underline', 'strikeThrough',
        'foreColor', 'list', 'justify', 'quote', 'emoticon', 'image', 'undo', 'redo'
    ];
    editor.customConfig.onchange = function (html) {
        $("#" + relName).val(html);
    };
    editor.customConfig.uploadImgShowBase64 = true;
    editor.create();
    // 编辑页从 textarea 读取未转义富文本，避免公告图片或 HTML 标签丢失。
    var rawContent = $("#contentRaw" + relName).val() || $("#" + relName).val() || "";
    editor.txt.html(rawContent);
    $("#" + relName).val(editor.txt.html());
});

$().ready(function () {
    validateRule();
});

$.validator.addMethod("dateTimeSecond", function (value, element) {
    // 展示时间提交给后端按 yyyy-MM-dd HH:mm:ss 解析，前端先拦截明显错误格式。
    return this.optional(element) || /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(value);
}, "请输入正确时间格式：yyyy-MM-dd HH:mm:ss，例如 2026-06-17 14:30:00");

$.validator.setDefaults({
    submitHandler: function () {
        update();
    }
});

function update() {
    $.ajax({
        cache: true,
        type: "POST",
        url: "/novel/announcement/update",
        data: $('#signupForm').serialize(),
        async: false,
        error: function () {
            parent.layer.alert("Connection error");
        },
        success: function (data) {
            if (data.code == 0) {
                parent.layer.msg("操作成功");
                parent.reLoad();
                var index = parent.layer.getFrameIndex(window.name);
                parent.layer.close(index);
            } else {
                parent.layer.alert(data.msg);
            }
        }
    });
}

function validateRule() {
    var icon = "<i class='fa fa-times-circle'></i> ";
    $("#signupForm").validate({
        ignore: "",
        rules: {
            title: {required: true},
            content: {required: true},
            type: {required: true},
            status: {required: true},
            startTime: {dateTimeSecond: true},
            endTime: {dateTimeSecond: true}
        },
        messages: {
            title: {required: icon + "请输入标题"},
            content: {required: icon + "请输入内容"},
            type: {required: icon + "请选择类型"},
            status: {required: icon + "请选择状态"},
            startTime: {dateTimeSecond: icon + "请输入正确时间格式：yyyy-MM-dd HH:mm:ss，例如 2026-06-17 14:30:00"},
            endTime: {dateTimeSecond: icon + "请输入正确时间格式：yyyy-MM-dd HH:mm:ss，例如 2026-06-30 23:59:59"}
        }
    });
}
