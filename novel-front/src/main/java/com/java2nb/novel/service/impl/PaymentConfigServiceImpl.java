package com.java2nb.novel.service.impl;

import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.config.AlipayProperties;
import com.java2nb.novel.entity.PaymentConfig;
import com.java2nb.novel.mapper.PaymentConfigMapper;
import com.java2nb.novel.service.PaymentConfigService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 运行期支付配置读取服务。
 */
@Service
@RequiredArgsConstructor
public class PaymentConfigServiceImpl implements PaymentConfigService {

    private static final long PAYMENT_CONFIG_CACHE_SECONDS = 300L;

    private static final String DEFAULT_ALIPAY_APP_ID = "demo-app-id";
    private static final String DEFAULT_ALIPAY_MERCHANT_PRIVATE_KEY = "demo-merchant-private-key";
    private static final String DEFAULT_ALIPAY_PUBLIC_KEY = "demo-alipay-public-key";
    private static final String DEFAULT_ALIPAY_NOTIFY_URL = "http://127.0.0.1:8083/pay/aliPay/notify";
    private static final String DEFAULT_ALIPAY_RETURN_URL = "http://127.0.0.1:8083/user/userinfo.html";
    private static final String DEFAULT_ALIPAY_GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    private final CacheService cacheService;

    private final PaymentConfigMapper paymentConfigMapper;

    private final AlipayProperties alipayProperties;

    @PostConstruct
    public void initPaymentConfig() {
        PaymentConfig dbConfig = paymentConfigMapper.selectByChannelCode(CHANNEL_CODE_ALIPAY);
        if (dbConfig == null) {
            // 前台只在数据库中完全不存在配置时初始化一份默认记录，
            // 避免启动时把后台已经维护好的支付开关、二维码和文案再次覆盖回去。
            paymentConfigMapper.insert(buildLocalPaymentConfig());
            cacheService.del(CacheKey.PAYMENT_CONFIG_KEY_PREFIX + CHANNEL_CODE_ALIPAY);
        }
    }

    @Override
    public PaymentConfig getAlipayConfig() {
        PaymentConfig dbConfig = getDbPaymentConfig(CHANNEL_CODE_ALIPAY);
        return mergeWithLocalFallback(dbConfig);
    }

    @Override
    public boolean isAlipayEnabled() {
        PaymentConfig dbConfig = getDbPaymentConfig(CHANNEL_CODE_ALIPAY);
        return dbConfig == null || dbConfig.getEnabled() == null || dbConfig.getEnabled() == 1;
    }

    private PaymentConfig getDbPaymentConfig(String channelCode) {
        String cacheKey = CacheKey.PAYMENT_CONFIG_KEY_PREFIX + channelCode;
        PaymentConfig cachedConfig = cacheService.getObject(cacheKey, PaymentConfig.class);
        if (cachedConfig != null) {
            return cachedConfig;
        }
        PaymentConfig paymentConfig = paymentConfigMapper.selectByChannelCode(channelCode);
        if (paymentConfig != null) {
            // 支付配置允许后台即时调整，这里只做短缓存，并依赖后台保存后主动失效保证实时性。
            cacheService.setObject(cacheKey, paymentConfig, PAYMENT_CONFIG_CACHE_SECONDS);
        }
        return paymentConfig;
    }

    private PaymentConfig mergeWithLocalFallback(PaymentConfig dbConfig) {
        PaymentConfig effectiveConfig = new PaymentConfig();
        effectiveConfig.setChannelCode(CHANNEL_CODE_ALIPAY);
        effectiveConfig.setChannelName("支付宝");
        effectiveConfig.setAppId(firstNonPlaceholder(getValue(dbConfig, ValueGetter.APP_ID), alipayProperties.getAppId()));
        effectiveConfig.setPublicKey(firstNonPlaceholder(getValue(dbConfig, ValueGetter.PUBLIC_KEY), alipayProperties.getPublicKey()));
        effectiveConfig.setPrivateKey(firstNonPlaceholder(getValue(dbConfig, ValueGetter.PRIVATE_KEY), alipayProperties.getMerchantPrivateKey()));
        effectiveConfig.setNotifyUrl(firstNonPlaceholder(getValue(dbConfig, ValueGetter.NOTIFY_URL), alipayProperties.getNotifyUrl()));
        effectiveConfig.setReturnUrl(firstNonPlaceholder(getValue(dbConfig, ValueGetter.RETURN_URL), alipayProperties.getReturnUrl()));
        effectiveConfig.setGatewayUrl(firstNonPlaceholder(getValue(dbConfig, ValueGetter.GATEWAY_URL), alipayProperties.getGatewayUrl()));
        effectiveConfig.setSignType(firstNonBlank(getValue(dbConfig, ValueGetter.SIGN_TYPE), alipayProperties.getSignType()));
        effectiveConfig.setCharset(firstNonBlank(getValue(dbConfig, ValueGetter.CHARSET), alipayProperties.getCharset()));
        effectiveConfig.setPayEnvironment(firstNonBlank(getValue(dbConfig, ValueGetter.PAY_ENVIRONMENT), "sandbox"));
        effectiveConfig.setAlipayQrCodeUrl(dbConfig == null ? null : dbConfig.getAlipayQrCodeUrl());
        effectiveConfig.setWechatQrCodeUrl(dbConfig == null ? null : dbConfig.getWechatQrCodeUrl());
        effectiveConfig.setEnabled(dbConfig == null || dbConfig.getEnabled() == null ? 1 : dbConfig.getEnabled());
        effectiveConfig.setWechatEnabled(dbConfig == null || dbConfig.getWechatEnabled() == null ? 0 : dbConfig.getWechatEnabled());
        effectiveConfig.setAlipayPersonalEnabled(dbConfig == null || dbConfig.getAlipayPersonalEnabled() == null ? 0 : dbConfig.getAlipayPersonalEnabled());
        effectiveConfig.setWechatPersonalEnabled(dbConfig == null || dbConfig.getWechatPersonalEnabled() == null ? 0 : dbConfig.getWechatPersonalEnabled());
        effectiveConfig.setRemark(dbConfig == null ? null : dbConfig.getRemark());
        return effectiveConfig;
    }

    private String firstNonBlank(String preferredValue, String fallbackValue) {
        if (preferredValue != null && !preferredValue.trim().isEmpty()) {
            return preferredValue.trim();
        }
        return fallbackValue;
    }

    private String firstNonPlaceholder(String preferredValue, String fallbackValue) {
        if (isUsableValue(preferredValue)) {
            return preferredValue.trim();
        }
        return fallbackValue;
    }

    private boolean isUsableValue(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        return !isKnownPlaceholder(trimmed);
    }

    private boolean isKnownPlaceholder(String value) {
        if (value.contains("${")) {
            return true;
        }
        if (value.startsWith("demo-")) {
            return true;
        }
        return DEFAULT_ALIPAY_APP_ID.equals(value)
            || DEFAULT_ALIPAY_MERCHANT_PRIVATE_KEY.equals(value)
            || DEFAULT_ALIPAY_PUBLIC_KEY.equals(value)
            || DEFAULT_ALIPAY_NOTIFY_URL.equals(value)
            || DEFAULT_ALIPAY_RETURN_URL.equals(value)
            || DEFAULT_ALIPAY_GATEWAY_URL.equals(value);
    }

    private PaymentConfig buildLocalPaymentConfig() {
        Date now = new Date();
        PaymentConfig config = new PaymentConfig();
        config.setChannelCode(CHANNEL_CODE_ALIPAY);
        config.setChannelName("支付宝");
        config.setAppId(alipayProperties.getAppId());
        config.setPublicKey(alipayProperties.getPublicKey());
        config.setPrivateKey(alipayProperties.getMerchantPrivateKey());
        config.setNotifyUrl(alipayProperties.getNotifyUrl());
        config.setReturnUrl(alipayProperties.getReturnUrl());
        config.setGatewayUrl(alipayProperties.getGatewayUrl());
        config.setSignType(alipayProperties.getSignType());
        config.setCharset(alipayProperties.getCharset());
        config.setPayEnvironment("sandbox");
        config.setEnabled(1);
        config.setWechatEnabled(0);
        config.setAlipayPersonalEnabled(0);
        config.setWechatPersonalEnabled(0);
        config.setRemark("init-from-local");
        config.setCreateTime(now);
        config.setCreateUserId(0L);
        config.setUpdateTime(now);
        config.setUpdateUserId(0L);
        return config;
    }
    private String getValue(PaymentConfig dbConfig, ValueGetter getter) {
        if (dbConfig == null) {
            return null;
        }
        return getter.get(dbConfig);
    }

    private interface ValueGetter {

        ValueGetter APP_ID = PaymentConfig::getAppId;
        ValueGetter PUBLIC_KEY = PaymentConfig::getPublicKey;
        ValueGetter PRIVATE_KEY = PaymentConfig::getPrivateKey;
        ValueGetter NOTIFY_URL = PaymentConfig::getNotifyUrl;
        ValueGetter RETURN_URL = PaymentConfig::getReturnUrl;
        ValueGetter GATEWAY_URL = PaymentConfig::getGatewayUrl;
        ValueGetter SIGN_TYPE = PaymentConfig::getSignType;
        ValueGetter CHARSET = PaymentConfig::getCharset;
        ValueGetter PAY_ENVIRONMENT = PaymentConfig::getPayEnvironment;

        String get(PaymentConfig paymentConfig);
    }
}
