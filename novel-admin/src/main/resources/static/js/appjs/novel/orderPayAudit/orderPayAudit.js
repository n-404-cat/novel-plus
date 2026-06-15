var prefix = "/novel/orderPayAudit"
$(function() {
	load();
});

function load() {
	$('#exampleTable')
			.bootstrapTable(
					{
						method : 'get', // 服务器数据的请求方式 get or post
						url : prefix + "/list", // 服务器数据的加载地址
						showRefresh : true,
						showToggle : true,
						showColumns : true,
						iconSize : 'outline',
						toolbar : '#exampleToolbar',
						striped : true, // 设置为true会有隔行变色效果
						dataType : "json", // 服务器返回的数据类型
						pagination : true, // 设置为true会在底部显示分页条
						singleSelect : false, // 设置为true将禁止多选
						pageSize : 10, // 如果设置了分页，每页数据条数
						pageNumber : 1, // 如果设置了分布，首页页码
						search : false, // 是否显示搜索框
						showColumns : false, // 是否显示内容下拉框（选择显示的列）
						sidePagination : "server", // 设置在哪里进行分页，可选值为"client" 或者 "server"
						queryParams : function(params) {
							return {
								//说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
								limit: params.limit,
								offset:params.offset,
								outTradeNo:$('#searchName').val()
							};
						},
						columns : [
								{
									checkbox : true
								},
								{
									field : 'id', 
									title : '主键' 
								},
								{
									field : 'outTradeNo', 
									title : '商户订单号' 
								},
								{
									field : 'voucherPath', 
									title : '支付凭证',
                                    formatter: function(value, row, index) {
                                        var viewUrl = row.voucherViewUrl || value;
                                        if (viewUrl) {
                                            return '<a href="' + viewUrl + '" target="_blank"><img src="' + viewUrl + '" style="max-width: 100px; max-height: 100px; object-fit: contain;" title="点击查看大图"/></a>';
                                        }
                                        return '-';
                                    }
								},
								{
									field : 'payerAccount', 
									title : '付款账号/备注' 
								},
								{
									field : 'auditStatus', 
									title : '审核状态',
                                    formatter: function(value, row, index) {
                                        if (value == 0) {
                                            return '<span class="label label-warning">待审核</span>';
                                        } else if (value == 1) {
                                            return '<span class="label label-success">通过</span>';
                                        } else if (value == 2) {
                                            return '<span class="label label-danger">拒绝</span>';
                                        }
                                        return value;
                                    }
								},
								{
									field : 'auditRemark', 
									title : '审核意见' 
								},
								{
									field : 'createTime', 
									title : '提交时间' 
								},
								{
									title : '操作',
									field : 'id',
									align : 'center',
									formatter : function(value, row, index) {
										var e = '<a class="btn btn-primary btn-sm '+s_audit_h+'" href="#" mce_href="#" title="审核" onclick="audit(\''
												+ row.id
												+ '\')"><i class="fa fa-edit"></i>审核</a> ';
										return e;
									}
								} ]
					});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}
function audit(id) {
	layer.open({
		type : 2,
		title : '审核',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '520px' ],
		content : prefix + '/audit/' + id // iframe的url
	});
}
