var prefix = "/novel/announcement";

$(function () {
    load();
});

function typeName(value) {
    if (value === 2) return '首页滚动';
    if (value === 3) return '弹窗公告';
    if (value === 4) return '右下角公告';
    return '文章公告';
}

function closeModeName(value) {
    return value === 1 ? '关闭后永不提示' : '下次打开重新提示';
}

function load() {
    $('#exampleTable').bootstrapTable({
        method: 'get',
        url: prefix + "/list",
        iconSize: 'outline',
        toolbar: '#exampleToolbar',
        striped: true,
        dataType: "json",
        pagination: true,
        singleSelect: false,
        pageSize: 10,
        pageNumber: 1,
        showColumns: false,
        sidePagination: "server",
        queryParams: function (params) {
            var queryParams = getFormJson("searchForm");
            queryParams.limit = params.limit;
            queryParams.offset = params.offset;
            return queryParams;
        },
        responseHandler: function (rs) {
            if (rs.code == 0) {
                return rs.data;
            }
            parent.layer.alert(rs.msg);
            return {total: 0, rows: []};
        },
        columns: [
            {checkbox: true},
            {title: '序号', formatter: function () { return arguments[2] + 1; }},
            {field: 'title', title: '标题'},
            {field: 'type', title: '类型', formatter: typeName},
            {
                field: 'status',
                title: '状态',
                formatter: function (value) {
                    return value === 1 ? '<span class="label label-primary">已上架</span>' : '<span class="label label-default">已下架</span>';
                }
            },
            {field: 'closeMode', title: '关闭策略', formatter: closeModeName},
            {field: 'sort', title: '排序'},
            {field: 'startTime', title: '开始时间'},
            {field: 'endTime', title: '结束时间'},
            {field: 'createTime', title: '创建时间'},
            {
                title: '操作',
                field: 'id',
                align: 'center',
                formatter: function (value, row) {
                    var d = '<a class="btn btn-primary btn-sm ' + s_detail_h + '" href="#" title="详情" onclick="detail(\'' + row.id + '\')"><i class="fa fa-file"></i></a> ';
                    var e = '<a class="btn btn-primary btn-sm ' + s_edit_h + '" href="#" title="编辑" onclick="edit(\'' + row.id + '\')"><i class="fa fa-edit"></i></a> ';
                    var r = '<a class="btn btn-warning btn-sm ' + s_remove_h + '" href="#" title="删除" onclick="remove(\'' + row.id + '\')"><i class="fa fa-remove"></i></a> ';
                    return d + e + r;
                }
            }]
    });
}

function reLoad() {
    $('#exampleTable').bootstrapTable('refresh');
}

function add() {
    layer.open({type: 2, title: '添加公告', maxmin: true, shadeClose: false, area: ['900px', '680px'], content: prefix + '/add'});
}

function detail(id) {
    layer.open({type: 2, title: '公告详情', maxmin: true, shadeClose: false, area: ['900px', '680px'], content: prefix + '/detail/' + id});
}

function edit(id) {
    layer.open({type: 2, title: '编辑公告', maxmin: true, shadeClose: false, area: ['900px', '680px'], content: prefix + '/edit/' + id});
}

function remove(id) {
    layer.confirm('确定要删除选中的公告？', {btn: ['确定', '取消']}, function () {
        $.ajax({
            url: prefix + "/remove",
            type: "post",
            data: {'id': id},
            success: function (r) {
                layer.msg(r.msg);
                if (r.code == 0) {
                    reLoad();
                }
            }
        });
    });
}

function batchRemove() {
    var rows = $('#exampleTable').bootstrapTable('getSelections');
    if (rows.length == 0) {
        layer.msg("请选择要删除的数据");
        return;
    }
    layer.confirm("确认要删除选中的'" + rows.length + "'条公告吗?", {btn: ['确定', '取消']}, function () {
        var ids = [];
        $.each(rows, function (i, row) {
            ids[i] = row['id'];
        });
        $.ajax({
            type: 'POST',
            data: {"ids": ids},
            url: prefix + '/batchRemove',
            success: function (r) {
                layer.msg(r.msg);
                if (r.code == 0) {
                    reLoad();
                }
            }
        });
    });
}
