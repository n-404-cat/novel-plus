package com.java2nb.novel.controller;

import com.java2nb.common.utils.PageBean;
import com.java2nb.common.utils.Query;
import com.java2nb.common.utils.R;
import com.java2nb.novel.domain.EbookDO;
import com.java2nb.novel.service.EbookService;
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
 * 电子书管理。
 */
@Controller
@RequestMapping("/novel/ebook")
public class EbookController {

    @Autowired
    private EbookService ebookService;

    @GetMapping()
    @RequiresPermissions("novel:ebook:ebook")
    String ebook() {
        return "novel/ebook/ebook";
    }

    @ApiOperation(value = "获取电子书列表", notes = "获取电子书列表")
    @ResponseBody
    @GetMapping("/list")
    @RequiresPermissions("novel:ebook:ebook")
    public R list(@RequestParam Map<String, Object> params) {
        Query query = new Query(params);
        List<EbookDO> ebookList = ebookService.list(query);
        int total = ebookService.count(query);
        return R.ok().put("data", new PageBean(ebookList, total));
    }

    @ApiOperation(value = "新增电子书页面", notes = "新增电子书页面")
    @GetMapping("/add")
    @RequiresPermissions("novel:ebook:add")
    String add() {
        return "novel/ebook/add";
    }

    @ApiOperation(value = "修改电子书页面", notes = "修改电子书页面")
    @GetMapping("/edit/{id}")
    @RequiresPermissions("novel:ebook:edit")
    String edit(@PathVariable("id") Long id, Model model) {
        model.addAttribute("ebook", ebookService.get(id));
        return "novel/ebook/edit";
    }

    @ApiOperation(value = "查看电子书详情", notes = "查看电子书详情")
    @GetMapping("/detail/{id}")
    @RequiresPermissions("novel:ebook:detail")
    String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("ebook", ebookService.get(id));
        return "novel/ebook/detail";
    }

    @ApiOperation(value = "新增电子书", notes = "新增电子书")
    @ResponseBody
    @PostMapping("/save")
    @RequiresPermissions("novel:ebook:add")
    public R save(EbookDO ebook) {
        try {
            if (ebookService.save(ebook) > 0) {
                return R.ok();
            }
            return R.error();
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    @ApiOperation(value = "修改电子书", notes = "修改电子书")
    @ResponseBody
    @RequestMapping("/update")
    @RequiresPermissions("novel:ebook:edit")
    public R update(EbookDO ebook) {
        try {
            ebookService.update(ebook);
            return R.ok();
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    @ApiOperation(value = "删除电子书", notes = "删除电子书")
    @PostMapping("/remove")
    @ResponseBody
    @RequiresPermissions("novel:ebook:remove")
    public R remove(Long id) {
        if (ebookService.remove(id) > 0) {
            return R.ok();
        }
        return R.error();
    }

    @ApiOperation(value = "批量删除电子书", notes = "批量删除电子书")
    @PostMapping("/batchRemove")
    @ResponseBody
    @RequiresPermissions("novel:ebook:batchRemove")
    public R batchRemove(@RequestParam("ids[]") Long[] ids) {
        ebookService.batchRemove(ids);
        return R.ok();
    }
}
