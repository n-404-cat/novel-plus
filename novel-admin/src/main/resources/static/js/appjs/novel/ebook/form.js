function initEbookUpload() {
    layui.use('upload', function () {
        var upload = layui.upload;
        var ebookSupportFormats = ['pdf', 'txt', 'epub', 'azw3', 'mobi'];
        var selectedEbookFormat = '';

        function getFileExt(fileName) {
            if (!fileName || fileName.indexOf('.') === -1) {
                return '';
            }
            return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        }

        upload.render({
            elem: '#picImagecoverUrl',
            url: '/common/sysFile/upload',
            size: 2048,
            accept: 'images',
            done: function (r) {
                if (r.code && r.code !== 0) {
                    layer.msg(r.msg || '封面上传失败');
                    return;
                }
                $("#picImagecoverUrl").attr("src", r.fileName);
                $("#coverUrl").val(r.fileName);
            },
            error: function () {
                layer.msg('封面上传失败');
            }
        });
        upload.render({
            elem: '#ebookFilePicker',
            url: '/common/sysFile/upload',
            size: 1024 * 200,
            accept: 'file',
            exts: 'pdf|txt|epub|azw3|mobi',
            before: function (obj) {
                // 上传前优先从用户选择的原始文件名识别格式，避免服务端重命名后格式字段被误判。
                var files = obj.pushFile();
                for (var key in files) {
                    if (files.hasOwnProperty(key)) {
                        var file = files[key];
                        selectedEbookFormat = getFileExt(file.name);
                        if (ebookSupportFormats.indexOf(selectedEbookFormat) === -1) {
                            layer.msg('暂只支持 PDF、TXT、EPUB、AZW3、MOBI 格式');
                            return false;
                        }
                        $("#fileSize").val(file.size || 0);
                        break;
                    }
                }
            },
            done: function (r) {
                if (r.code && r.code !== 0) {
                    layer.msg(r.msg || '电子书上传失败');
                    return;
                }
                var fileName = r.fileName || '';
                var format = selectedEbookFormat || getFileExt(fileName);
                $("#fileUrl").val(fileName);
                $("#fileFormat").val(format);
                $("#ebookFileName").text(fileName);
            },
            error: function () {
                layer.msg('电子书上传失败');
            }
        });
    });
}
