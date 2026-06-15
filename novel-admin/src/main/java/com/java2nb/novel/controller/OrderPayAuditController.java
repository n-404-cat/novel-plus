package com.java2nb.novel.controller;

import com.java2nb.novel.domain.OrderPayAuditDO;
import com.java2nb.novel.service.OrderPayAuditService;
import com.java2nb.novel.domain.WebsiteInfoDO;
import com.java2nb.novel.service.WebsiteInfoService;
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
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/novel/orderPayAudit")
public class OrderPayAuditController {

    private String prefix = "novel/orderPayAudit";

    @Autowired
    private OrderPayAuditService orderPayAuditService;

    @Autowired
    private WebsiteInfoService websiteInfoService;

    @RequiresPermissions("novel:orderPayAudit:orderPayAudit")
    @GetMapping()
    public String orderPayAudit() {
        return prefix + "/orderPayAudit";
    }

    @ResponseBody
    @GetMapping("/list")
    @RequiresPermissions("novel:orderPayAudit:orderPayAudit")
    public PageBean list(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        Query query = new Query(params);
        List<OrderPayAuditDO> orderPayAuditList = orderPayAuditService.list(query);
        fillVoucherViewUrl(orderPayAuditList, request);
        int total = orderPayAuditService.count(query);
        return new PageBean(orderPayAuditList, total);
    }

    @RequiresPermissions("novel:orderPayAudit:audit")
    @GetMapping("/audit/{id}")
    public String audit(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        OrderPayAuditDO orderPayAudit = orderPayAuditService.get(id);
        if (orderPayAudit != null) {
            orderPayAudit.setVoucherViewUrl(buildVoucherViewUrl(orderPayAudit.getVoucherPath(), request));
        }
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

    private void fillVoucherViewUrl(List<OrderPayAuditDO> audits, HttpServletRequest request) {
        if (audits == null || audits.isEmpty()) {
            return;
        }
        for (OrderPayAuditDO audit : audits) {
            audit.setVoucherViewUrl(buildVoucherViewUrl(audit.getVoucherPath(), request));
        }
    }

    private String buildVoucherViewUrl(String voucherPath, HttpServletRequest request) {
        if (voucherPath == null || voucherPath.trim().isEmpty()) {
            return "";
        }
        if (voucherPath.startsWith("http://") || voucherPath.startsWith("https://")) {
            return voucherPath;
        }
        if (!voucherPath.startsWith("/")) {
            return voucherPath;
        }
        String serverName = request == null ? null : request.getServerName();
        if (serverName != null && ("127.0.0.1".equals(serverName) || "localhost".equalsIgnoreCase(serverName))) {
            return "http://127.0.0.1:8083" + voucherPath;
        }
        WebsiteInfoDO websiteInfo = websiteInfoService.get(1L);
        if (websiteInfo == null || websiteInfo.getDomain() == null || websiteInfo.getDomain().trim().isEmpty()) {
            return voucherPath;
        }
        String domain = websiteInfo.getDomain().trim();
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "http://" + domain;
        }
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        return domain + voucherPath;
    }
}
