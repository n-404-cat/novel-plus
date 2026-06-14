package com.java2nb.novel.controller;

import com.java2nb.novel.domain.OrderPayAuditDO;
import com.java2nb.novel.service.OrderPayAuditService;
import com.java2nb.common.utils.PageBean;
import com.java2nb.common.utils.Query;
import com.java2nb.common.utils.R;
import com.java2nb.common.utils.ShiroUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/novel/orderPayAudit")
public class OrderPayAuditController {

    private String prefix = "novel/orderPayAudit";

    @Autowired
    private OrderPayAuditService orderPayAuditService;

    @RequiresPermissions("novel:orderPayAudit:orderPayAudit")
    @GetMapping()
    public String orderPayAudit() {
        return prefix + "/orderPayAudit";
    }

    @ResponseBody
    @GetMapping("/list")
    @RequiresPermissions("novel:orderPayAudit:orderPayAudit")
    public PageBean list(@RequestParam Map<String, Object> params) {
        Query query = new Query(params);
        List<OrderPayAuditDO> orderPayAuditList = orderPayAuditService.list(query);
        int total = orderPayAuditService.count(query);
        return new PageBean(orderPayAuditList, total);
    }

    @RequiresPermissions("novel:orderPayAudit:audit")
    @GetMapping("/audit/{id}")
    public String audit(@PathVariable("id") Long id, Model model) {
        OrderPayAuditDO orderPayAudit = orderPayAuditService.get(id);
        model.addAttribute("orderPayAudit", orderPayAudit);
        return prefix + "/audit";
    }

    @ResponseBody
    @RequiresPermissions("novel:orderPayAudit:audit")
    @PostMapping("/audit")
    public R auditSave(OrderPayAuditDO orderPayAudit) {
        orderPayAudit.setAuditUserId(ShiroUtils.getUserId());
        if (orderPayAuditService.audit(orderPayAudit) > 0) {
            return R.ok();
        }
        return R.error();
    }
}