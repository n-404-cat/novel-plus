var prefix = "/novel/ebook";

$(function () {
    load();
});

function formatFileSize(size) {
    if (!size) return '-';
    if (size < 1024) return size + ' B';
    if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB';
    return (size / 1024 / 1024).toFixed(1) + ' MB';
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
            {
                field: 'coverUrl',
                title: '封面',
                formatter: function (value) {
                    return value ? '<img src="' + value + '" style="width:46px;height:62px;object-fit:cover;border:1px solid #ddd;">' : '-';
                }
            },
            {field: 'bookName', title: '书名'},
            {field: 'authorName', title: '作者'},
            {field: 'isbn', title: 'ISBN'},
            {field: 'fileFormat', title: '格式'},
            {field: 'fileSize', title: '大小', formatter: formatFileSize},
            {
                field: 'status',
                title: '状态',
                formatter: function (value) {
                    return value === 1 ? '<span class="label label-primary">已上架</span>' : '<span class="label label-default">已下架</span>';
                }
            },
            {field: 'sort', title: '排序'},
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
    layer.open({type: 2, title: '添加电子书', maxmin: true, shadeClose: false, area: ['900px', '760px'], content: prefix + '/add'});
}

function detail(id) {
    layer.open({type: 2, title: '电子书详情', maxmin: true, shadeClose: false, area: ['900px', '700px'], content: prefix + '/detail/' + id});
}

function edit(id) {
    layer.open({type: 2, title: '编辑电子书', maxmin: true, shadeClose: false, area: ['900px', '760px'], content: prefix + '/edit/' + id});
}

function remove(id) {
    layer.confirm('确定要删除选中的电子书？', {btn: ['确定', '取消']}, function () {
        $.post(prefix + "/remove", {id: id}, function (r) {
            layer.msg(r.msg);
            if (r.code == 0) reLoad();
        });
    });
}

function batchRemove() {
    var rows = $('#exampleTable').bootstrapTable('getSelections');
    if (rows.length == 0) {
        layer.msg("请选择要删除的数据");
        return;
    }
    layer.confirm("确认要删除选中的'" + rows.length + "'本电子书吗?", {btn: ['确定', '取消']}, function () {
        var ids = [];
        $.each(rows, function (i, row) {
            ids[i] = row.id;
        });
        $.post(prefix + '/batchRemove', {ids: ids}, function (r) {
            layer.msg(r.msg);
            if (r.code == 0) reLoad();
        });
    });
}
