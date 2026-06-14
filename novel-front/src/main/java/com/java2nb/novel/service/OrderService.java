package com.java2nb.novel.service;


/**
 * @author 11797
 */
public interface OrderService {


    /**
     * 创建充值订单
     *
     * @param payChannel 支付渠道
     * @param payAmount  支付金额
     * @param userId     用户ID
     * @return 商户订单号
     */
    Long createPayOrder(Byte payChannel, Integer payAmount, Long userId);


    /**
     * 更新订单状态
     *
     * @param outTradeNo 商户订单号
     * @param tradeNo    支付宝/微信 订单号
     * @param payStatus  支付状态
     */
    void updatePayOrder(Long outTradeNo, String tradeNo, int payStatus);

    /**
     * 创建待审核充值订单（个人支付凭证上传）
     *
     * @param payChannel 支付渠道
     * @param payAmount  支付金额
     * @param userId     用户ID
     * @param voucherUrl 凭证URL
     * @return 商户订单号
     */
    Long createAuditOrder(Byte payChannel, Integer payAmount, Long userId, String voucherUrl);
}
