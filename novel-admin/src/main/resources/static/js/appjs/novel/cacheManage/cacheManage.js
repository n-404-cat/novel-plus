var prefix = "/novel/cacheManage";

$(function () {
    loadCacheItems();
});

function loadCacheItems() {
    $.get(prefix + "/items", function (res) {
        if (res.code !== 0 || !res.data) {
            layer.alert(res.msg || "加载缓存项失败");
            return;
        }
        var html = "";
        for (var i = 0; i < res.data.length; i++) {
            var item = res.data[i];
            html += '<tr>'
                + '<td><input type="checkbox" class="cache-item" value="' + item.code + '"></td>'
                + '<td>' + item.name + '</td>'
                + '<td><code>' + item.pattern + '</code></td>'
                + '<td>' + item.remark + '</td>'
                + '</tr>';
        }
        $("#cacheItemBody").html(html);
    });
}

function selectAllItems() {
    $(".cache-item").prop("checked", true);
}

function invertSelection() {
    $(".cache-item").each(function () {
        $(this).prop("checked", !$(this).prop("checked"));
    });
}

function clearSelection() {
    $(".cache-item").prop("checked", false);
}

function clearCaches() {
    var cacheCodes = [];
    $(".cache-item:checked").each(function () {
        cacheCodes.push($(this).val());
    });
    if (cacheCodes.length === 0) {
        layer.msg("请至少选择一个缓存项");
        return;
    }
    $.ajax({
        type: "POST",
        url: prefix + "/clear",
        traditional: true,
        data: {
            "cacheCodes[]": cacheCodes
        },
        success: function (res) {
            if (res.code === 0) {
                layer.msg("缓存清理完成：清理项 " + res.clearedItemCount + " 个，删除 key " + res.deletedKeyCount + " 个");
            } else {
                layer.alert(res.msg || "缓存清理失败");
            }
        },
        error: function (xhr) {
            layer.alert(xhr && xhr.responseText ? xhr.responseText : "缓存清理失败");
        }
    });
}
