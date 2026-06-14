package com.java2nb.novel.controller;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.java2nb.novel.domain.SensitiveWordDO;
import com.java2nb.novel.service.SensitiveWordService;
import com.java2nb.common.utils.PageBean;
import com.java2nb.common.utils.Query;
import com.java2nb.common.utils.R;

/**
 * 敏感词库
 */
@Controller
@RequestMapping("/novel/sensitiveWord")
public class SensitiveWordController {
    @Autowired
    private SensitiveWordService sensitiveWordService;

    @GetMapping()
    @RequiresPermissions("novel:sensitiveWord:sensitiveWord")
    String SensitiveWord() {
        return "novel/sensitiveWord/sensitiveWord";
    }

    @ResponseBody
    @GetMapping("/list")
    @RequiresPermissions("novel:sensitiveWord:sensitiveWord")
    public PageBean list(@RequestParam Map<String, Object> params) {
        //查询列表数据
        Query query = new Query(params);
        List<SensitiveWordDO> sensitiveWordList = sensitiveWordService.list(query);
        int total = sensitiveWordService.count(query);
        PageBean pageBean = new PageBean(sensitiveWordList, total);
        return pageBean;
    }

    @GetMapping("/add")
    @RequiresPermissions("novel:sensitiveWord:add")
    String add() {
        return "novel/sensitiveWord/add";
    }

    @GetMapping("/edit/{id}")
    @RequiresPermissions("novel:sensitiveWord:edit")
    String edit(@PathVariable("id") Long id, Model model) {
        SensitiveWordDO sensitiveWord = sensitiveWordService.get(id);
        model.addAttribute("sensitiveWord", sensitiveWord);
        return "novel/sensitiveWord/edit";
    }

    /**
     * 保存
     */
    @ResponseBody
    @PostMapping("/save")
    @RequiresPermissions("novel:sensitiveWord:add")
    public R save(SensitiveWordDO sensitiveWord) {
        sensitiveWord.setCreateTime(new java.util.Date());
        if (sensitiveWordService.save(sensitiveWord) > 0) {
            return R.ok();
        }
        return R.error();
    }

    /**
     * 修改
     */
    @ResponseBody
    @RequestMapping("/update")
    @RequiresPermissions("novel:sensitiveWord:edit")
    public R update(SensitiveWordDO sensitiveWord) {
        if (sensitiveWordService.update(sensitiveWord) > 0) {
            return R.ok();
        }
        return R.error();
    }

    /**
     * 删除
     */
    @PostMapping("/remove")
    @ResponseBody
    @RequiresPermissions("novel:sensitiveWord:remove")
    public R remove(Long id) {
        if (sensitiveWordService.remove(id) > 0) {
            return R.ok();
        }
        return R.error();
    }

    /**
     * 删除
     */
    @PostMapping("/batchRemove")
    @ResponseBody
    @RequiresPermissions("novel:sensitiveWord:batchRemove")
    public R remove(@RequestParam("ids[]") Long[] ids) {
        sensitiveWordService.batchRemove(ids);
        return R.ok();
    }

}