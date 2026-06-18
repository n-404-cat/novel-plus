$(function () {
    $("#ebookKeyword").on("keydown", function (event) {
        if (event.keyCode === 13) {
            searchEbook(1, 12);
        }
    });
    searchEbook(1, 12);
});

function searchEbook(curr, limit) {
    var keyword = $.trim($("#ebookKeyword").val() || "");
    $.ajax({
        type: "get",
        url: "/ebook/listByPage",
        data: {curr: curr, limit: limit, keyword: keyword},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                renderEbookList(data.data.list || []);
                layui.use('laypage', function () {
                    layui.laypage.render({
                        elem: 'ebookPage',
                        count: data.data.total,
                        curr: data.data.pageNum,
                        limit: data.data.pageSize,
                        jump: function (obj, first) {
                            if (!first) {
                                // 分页切换时保留当前关键词，避免翻页后回到全量列表。
                                searchEbook(obj.curr, obj.limit);
                            }
                        }
                    });
                });
            } else {
                layer.alert(data.msg);
            }
        },
        error: function () {
            layer.alert('网络异常');
        }
    });
}

function renderEbookList(list) {
    if (!list.length) {
        $("#ebookList").html('<div class="ebook-empty">暂无电子书</div>');
        return;
    }
    var html = "";
    for (var i = 0; i < list.length; i++) {
        var item = list[i];
        var cover = item.coverUrl || "/images/default.gif";
        var author = item.authorName || "未知作者";
        var format = (item.fileFormat || "").toUpperCase();
        html += '<a class="ebook-card" href="/ebook/' + item.id + '.html">' +
            '<img src="' + cover + '" alt="' + escapeHtml(item.bookName || "") + '">' +
            '<strong>' + escapeHtml(item.bookName || "") + '</strong>' +
            '<span>' + escapeHtml(author) + '</span>' +
            '<em>' + escapeHtml(format) + '</em>' +
            '</a>';
    }
    $("#ebookList").html(html);
}

function escapeHtml(value) {
    return String(value || "").replace(/[&<>"']/g, function (ch) {
        return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[ch];
    });
}
