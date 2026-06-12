package com.java2nb.novel.controller;

import com.java2nb.common.controller.BaseController;
import com.java2nb.common.utils.R;
import com.java2nb.novel.domain.PaymentConfigDO;
import com.java2nb.novel.service.PaymentConfigService;
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
 * 支付配置管理
 */
@Controller
@RequestMapping("/novel/paymentConfig")
public class PaymentConfigController extends BaseController {

    private static final String CHANNEL_CODE_ALIPAY = PaymentConfigService.CHANNEL_CODE_ALIPAY;

    @Autowired
    private PaymentConfigService paymentConfigService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping()
    @RequiresPermissions("novel:paymentConfig:paymentConfig")
    String paymentConfig() {
        return "novel/paymentConfig/paymentConfig";
    }

    @GetMapping("/detail")
    @RequiresPermissions("novel:paymentConfig:paymentConfig")
    String detail(Model model) {
        PaymentConfigDO paymentConfig = paymentConfigService.getByChannelCode(CHANNEL_CODE_ALIPAY);
        if (paymentConfig == null) {
            paymentConfig = defaultAlipayConfig();
        }
        model.addAttribute("paymentConfig", paymentConfig);
        return "novel/paymentConfig/detail";
    }

    /**
     * 保存或更新支付宝配置
     */
    @ApiOperation(value = "保存支付配置", notes = "保存支付配置")
    @ResponseBody
    @PostMapping("/update")
    @RequiresPermissions("novel:paymentConfig:edit")
    public R update(PaymentConfigDO paymentConfig) {
        paymentConfig.setChannelCode(CHANNEL_CODE_ALIPAY);
        paymentConfig.setChannelName("支付宝");
        if (paymentConfig.getEnabled() == null) {
            paymentConfig.setEnabled(1);
        }
        if (isBlank(paymentConfig.getSignType())) {
            paymentConfig.setSignType("RSA2");
        }
        if (isBlank(paymentConfig.getCharset())) {
            paymentConfig.setCharset("utf-8");
        }
        if (isBlank(paymentConfig.getPayEnvironment())) {
            paymentConfig.setPayEnvironment("sandbox");
        }
        paymentConfigService.saveOrUpdate(paymentConfig, getUserId());
        // 支付配置被前台短缓存，这里保存后立刻失效，确保下一次调起支付时读取最新配置。
        redisTemplate.delete(buildCacheKey(CHANNEL_CODE_ALIPAY));
        return R.ok("支付配置已保存并生效");
    }

    /**
     * 手动清理支付配置缓存
     */
    @ApiOperation(value = "清理支付配置缓存", notes = "清理支付配置缓存")
    @ResponseBody
    @RequestMapping(value = "/clearCache", method = {RequestMethod.POST, RequestMethod.GET})
    @RequiresPermissions("novel:paymentConfig:edit")
    public R clearCache() {
        redisTemplate.delete(buildCacheKey(CHANNEL_CODE_ALIPAY));
        return R.ok("支付配置缓存已清理");
    }

    private PaymentConfigDO defaultAlipayConfig() {
        PaymentConfigDO paymentConfig = new PaymentConfigDO();
        paymentConfig.setChannelCode(CHANNEL_CODE_ALIPAY);
        paymentConfig.setChannelName("支付宝");
        paymentConfig.setSignType("RSA2");
        paymentConfig.setCharset("utf-8");
        paymentConfig.setPayEnvironment("sandbox");
        paymentConfig.setEnabled(1);
        return paymentConfig;
    }

    private String buildCacheKey(String channelCode) {
        return com.java2nb.common.config.CacheKey.PAYMENT_CONFIG_KEY_PREFIX + channelCode;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
