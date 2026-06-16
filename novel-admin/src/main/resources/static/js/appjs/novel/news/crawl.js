var prefix = "/novel/news";
var crawlSources = [];

$(function () {
    loadSources();
    loadCategories();
});

function loadSources() {
    $.get(prefix + "/crawl/sources", function (r) {
        if (r.code !== 0) {
            layer.msg(r.msg);
            return;
        }
        crawlSources = r.data || [];
        var html = "";
        $.each(crawlSources, function (i, item) {
            html += '<option value="' + item.sourceCode + '">' + item.sourceName + '</option>';
        });
        $("#sourceCode").html(html);
        refreshSourceTip();
        $("#sourceCode").change(refreshSourceTip);
    });
}

function loadCategories() {
    $.ajax({
        type: "GET",
        url: "/novel/category/list",
        data: {limit: 1000, offset: 0},
        success: function (r) {
            if (r.code !== 0) {
                layer.msg(r.msg);
                return;
            }
            var html = '<option value="">请选择</option>';
            $.each(r.data.rows || [], function (i, item) {
                html += '<option value="' + item.id + '">' + item.name + '</option>';
            });
            $("#catId").html(html);
            $("#catId").change(function () {
                var catName = $(this).find("option:selected").text();
                $("#catName").val(catName === "请选择" ? "" : catName);
            });
        }
    });
}

function refreshSourceTip() {
    var sourceCode = $("#sourceCode").val();
    var source = findSource(sourceCode);
    $("#sourceTip").text(source && source.demoUrl ? "示例：" + source.demoUrl : "");
}

function findSource(sourceCode) {
    for (var i = 0; i < crawlSources.length; i++) {
        if (crawlSources[i].sourceCode === sourceCode) {
            return crawlSources[i];
        }
    }
    return null;
}

function buildCrawlData() {
    return {
        sourceCode: $("#sourceCode").val(),
        url: $.trim($("#url").val()),
        catId: $("#catId").val(),
        catName: $("#catName").val(),
        status: $("input[name='status']:checked").val()
    };
}

function previewNews() {
    var data = buildCrawlData();
    if (!data.url) {
        layer.msg("请输入新闻详情页地址");
        return;
    }
    layer.load(1);
    $.ajax({
        type: "POST",
        url: prefix + "/crawl/preview",
        data: data,
        success: function (r) {
            layer.closeAll("loading");
            if (r.code !== 0) {
                layer.msg(r.msg);
                return;
            }
            renderPreview(r.data);
        },
        error: function () {
            layer.closeAll("loading");
            layer.msg("采集预览失败，请查看后台日志");
        }
    });
}

function saveCrawlNews() {
    var data = buildCrawlData();
    if (!data.url) {
        layer.msg("请输入新闻详情页地址");
        return;
    }
    if (!data.catId) {
        layer.msg("请选择新闻分类");
        return;
    }
    layer.load(1);
    $.ajax({
        type: "POST",
        url: prefix + "/crawl/save",
        data: data,
        success: function (r) {
            layer.closeAll("loading");
            if (r.code !== 0) {
                layer.msg(r.msg);
                return;
            }
            renderPreview(r.data);
            layer.msg("采集保存成功");
            // 保存成功后刷新父页面新闻列表，让新采集内容立即可见。
            if (parent && parent.reLoad) {
                parent.reLoad();
            }
        },
        error: function () {
            layer.closeAll("loading");
            layer.msg("采集保存失败，请查看后台日志");
        }
    });
}

function renderPreview(data) {
    $("#previewTitle").text(data.title || "采集预览");
    $("#previewContent").html(data.content || "");
}
