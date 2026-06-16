package com.java2nb.novel.controller;

import com.java2nb.common.config.CacheKey;
import com.java2nb.common.exception.BusinessException;
import com.java2nb.common.utils.PageBean;
import com.java2nb.common.utils.Query;
import com.java2nb.common.utils.R;
import com.java2nb.novel.domain.NewsDO;
import com.java2nb.novel.service.NewsCrawlService;
import com.java2nb.novel.service.NewsService;
import com.java2nb.novel.vo.NewsCrawlResultVO;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 新闻表
 *
 * @author xiongxy
 * @email 1179705413@qq.com
 * @date 2020-12-01 10:05:51
 */

@Controller
@RequestMapping("/novel/news")
public class NewsController {

    @Autowired
    private NewsService newsService;
    @Autowired
    private NewsCrawlService newsCrawlService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping()
    @RequiresPermissions("novel:news:news")
    String News() {
        return "novel/news/news";
    }

    @ApiOperation(value = "获取新闻表列表", notes = "获取新闻表列表")
    @ResponseBody
    @GetMapping("/list")
    @RequiresPermissions("novel:news:news")
    public R list(@RequestParam Map<String, Object> params) {
        //查询列表数据
        Query query = new Query(params);
        List<NewsDO> newsList = newsService.list(query);
        int total = newsService.count(query);
        PageBean pageBean = new PageBean(newsList, total);
        return R.ok().put("data", pageBean);
    }

    @ApiOperation(value = "新增新闻表页面", notes = "新增新闻表页面")
    @GetMapping("/add")
    @RequiresPermissions("novel:news:add")
    String add() {
        return "novel/news/add";
    }

    @ApiOperation(value = "新闻采集页面", notes = "新闻采集页面")
    @GetMapping("/crawl")
    @RequiresPermissions("novel:news:add")
    String crawl() {
        return "novel/news/crawl";
    }

    @ApiOperation(value = "新闻内置采集源", notes = "新闻内置采集源")
    @ResponseBody
    @GetMapping("/crawl/sources")
    @RequiresPermissions("novel:news:add")
    public R crawlSources() {
        return R.ok().put("data", newsCrawlService.listBuiltInSources());
    }

    @ApiOperation(value = "预览采集新闻", notes = "预览采集新闻")
    @ResponseBody
    @PostMapping("/crawl/preview")
    @RequiresPermissions("novel:news:add")
    public R crawlPreview(String sourceCode, String url) {
        try {
            return R.ok().put("data", newsCrawlService.preview(sourceCode, url));
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
    }

    @ApiOperation(value = "保存采集新闻", notes = "保存采集新闻")
    @ResponseBody
    @PostMapping("/crawl/save")
    @RequiresPermissions("novel:news:add")
    public R crawlSave(String sourceCode, String url, Integer catId, String catName, Integer status) {
        try {
            // 采集入库后必须清理首页新闻缓存，避免前台还展示旧列表。
            NewsCrawlResultVO result = newsCrawlService.save(sourceCode, url, catId, catName, status);
            redisTemplate.delete(CacheKey.INDEX_NEWS_KEY);
            return R.ok().put("data", result);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
    }

    @ApiOperation(value = "修改新闻表页面", notes = "修改新闻表页面")
    @GetMapping("/edit/{id}")
    @RequiresPermissions("novel:news:edit")
    String edit(@PathVariable("id") Long id, Model model) {
        NewsDO news = newsService.get(id);
        model.addAttribute("news", news);
        return "novel/news/edit";
    }

    @ApiOperation(value = "查看新闻表页面", notes = "查看新闻表页面")
    @GetMapping("/detail/{id}")
    @RequiresPermissions("novel:news:detail")
    String detail(@PathVariable("id") Long id, Model model) {
        NewsDO news = newsService.get(id);
        model.addAttribute("news", news);
        return "novel/news/detail";
    }

    /**
     * 保存
     */
    @ApiOperation(value = "新增新闻表", notes = "新增新闻表")
    @ResponseBody
    @PostMapping("/save")
    @RequiresPermissions("novel:news:add")
    public R save(NewsDO news) {
        if (news.getStatus() == null) {
            news.setStatus(1);
        }
        if (newsService.save(news) > 0) {
            redisTemplate.delete(CacheKey.INDEX_NEWS_KEY);
            return R.ok();
        }
        return R.error();
    }

    /**
     * 修改
     */
    @ApiOperation(value = "修改新闻表", notes = "修改新闻表")
    @ResponseBody
    @RequestMapping("/update")
    @RequiresPermissions("novel:news:edit")
    public R update(NewsDO news) {
        newsService.update(news);
        redisTemplate.delete(CacheKey.INDEX_NEWS_KEY);
        return R.ok();
    }

    /**
     * 删除
     */
    @ApiOperation(value = "删除新闻表", notes = "删除新闻表")
    @PostMapping("/remove")
    @ResponseBody
    @RequiresPermissions("novel:news:remove")
    public R remove(Long id) {
        if (newsService.remove(id) > 0) {
            redisTemplate.delete(CacheKey.INDEX_NEWS_KEY);
            return R.ok();
        }
        return R.error();
    }

    /**
     * 删除
     */
    @ApiOperation(value = "批量删除新闻表", notes = "批量删除新闻表")
    @PostMapping("/batchRemove")
    @ResponseBody
    @RequiresPermissions("novel:news:batchRemove")
    public R remove(@RequestParam("ids[]") Long[] ids) {
        newsService.batchRemove(ids);
        redisTemplate.delete(CacheKey.INDEX_NEWS_KEY);
        return R.ok();
    }

}
