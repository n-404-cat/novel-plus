$(function () {
    initEbookUpload();
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
        url: "/novel/ebook/update",
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
            bookName: {required: true},
            fileUrl: {required: true},
            fileFormat: {required: true}
        },
        messages: {
            bookName: {required: icon + "请输入书名"},
            fileUrl: {required: icon + "请上传电子书文件"},
            fileFormat: {required: icon + "请上传支持的电子书文件"}
        }
    });
}
