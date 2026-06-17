package com.java2nb.novel.controller;

import com.java2nb.common.utils.PageBean;
import com.java2nb.common.utils.Query;
import com.java2nb.common.utils.R;
import com.java2nb.novel.domain.AnnouncementDO;
import com.java2nb.novel.service.AnnouncementService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 公告管理
 */
@Controller
@RequestMapping("/novel/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping()
    @RequiresPermissions("novel:announcement:announcement")
    String announcement() {
        return "novel/announcement/announcement";
    }

    @ApiOperation(value = "获取公告列表", notes = "获取公告列表")
    @ResponseBody
    @GetMapping("/list")
    @RequiresPermissions("novel:announcement:announcement")
    public R list(@RequestParam Map<String, Object> params) {
        Query query = new Query(params);
        List<AnnouncementDO> announcementList = announcementService.list(query);
        int total = announcementService.count(query);
        return R.ok().put("data", new PageBean(announcementList, total));
    }

    @ApiOperation(value = "新增公告页面", notes = "新增公告页面")
    @GetMapping("/add")
    @RequiresPermissions("novel:announcement:add")
    String add() {
        return "novel/announcement/add";
    }

    @ApiOperation(value = "修改公告页面", notes = "修改公告页面")
    @GetMapping("/edit/{id}")
    @RequiresPermissions("novel:announcement:edit")
    String edit(@PathVariable("id") Long id, Model model) {
        model.addAttribute("announcement", announcementService.get(id));
        return "novel/announcement/edit";
    }

    @ApiOperation(value = "查看公告页面", notes = "查看公告页面")
    @GetMapping("/detail/{id}")
    @RequiresPermissions("novel:announcement:detail")
    String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("announcement", announcementService.get(id));
        return "novel/announcement/detail";
    }

    @ApiOperation(value = "新增公告", notes = "新增公告")
    @ResponseBody
    @PostMapping("/save")
    @RequiresPermissions("novel:announcement:add")
    public R save(AnnouncementDO announcement) {
        // 公告类型和关闭策略影响前台展示，保存前统一补齐默认值，避免空值导致前台判断异常。
        if (announcementService.save(announcement) > 0) {
            return R.ok();
        }
        return R.error();
    }

    @ApiOperation(value = "修改公告", notes = "修改公告")
    @ResponseBody
    @RequestMapping("/update")
    @RequiresPermissions("novel:announcement:edit")
    public R update(AnnouncementDO announcement) {
        announcementService.update(announcement);
        return R.ok();
    }

    @ApiOperation(value = "删除公告", notes = "删除公告")
    @PostMapping("/remove")
    @ResponseBody
    @RequiresPermissions("novel:announcement:remove")
    public R remove(Long id) {
        if (announcementService.remove(id) > 0) {
            return R.ok();
        }
        return R.error();
    }

    @ApiOperation(value = "批量删除公告", notes = "批量删除公告")
    @PostMapping("/batchRemove")
    @ResponseBody
    @RequiresPermissions("novel:announcement:batchRemove")
    public R batchRemove(@RequestParam("ids[]") Long[] ids) {
        announcementService.batchRemove(ids);
        return R.ok();
    }
}
