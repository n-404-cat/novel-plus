package com.java2nb.novel.controller;

import com.java2nb.common.utils.R;
import com.java2nb.novel.domain.WebsiteInfoDO;
import com.java2nb.novel.service.WebsiteInfoService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 网站信息表
 *
 * @author xiongxy
 * @email 1179705413@qq.com
 * @date 2023-04-14 11:05:43
 */

@Controller
@RequestMapping("/novel/websiteInfo")
public class WebsiteInfoController {

    @Autowired
    private WebsiteInfoService websiteInfoService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping()
    @RequiresPermissions("novel:websiteInfo:websiteInfo")
    String detail(Model model) {
        WebsiteInfoDO websiteInfo = websiteInfoService.get(1L);
        model.addAttribute("websiteInfo", websiteInfo);
        return "novel/websiteInfo/detail";
    }

    /**
     * 修改
     */
    @ApiOperation(value = "修改网站信息表", notes = "修改网站信息表")
    @ResponseBody
    @RequestMapping("/update")
    @RequiresPermissions("novel:websiteInfo:edit")
    public R update(WebsiteInfoDO websiteInfo) {
        websiteInfoService.update(websiteInfo);
        // 网站信息与模板配置由前台请求期动态读取，这里同步清理 Redis 缓存以保证保存后立即生效。
        redisTemplate.delete(com.java2nb.common.config.CacheKey.WEBSITE_INFO_KEY);
        return R.ok("操作成功，已生效");
    }

    /**
     * 清理网站信息缓存
     */
    @ApiOperation(value = "清理网站信息缓存", notes = "清理网站信息缓存")
    @ResponseBody
    @RequestMapping(value = "/clearCache", method = {RequestMethod.POST, RequestMethod.GET})
    @RequiresPermissions("novel:websiteInfo:edit")
    public R clearCache() {
        // 提供后台人工清理入口，便于运营在排查模板切换与站点配置问题时主动触发失效。
        redisTemplate.delete(com.java2nb.common.config.CacheKey.WEBSITE_INFO_KEY);
        return R.ok("缓存已清理");
    }

}
