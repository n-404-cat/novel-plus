package com.java2nb.novel.dao;

import com.java2nb.novel.domain.PaymentConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 支付配置表
 */
@Mapper
public interface PaymentConfigDao {

    PaymentConfigDO get(Long id);

    PaymentConfigDO getByChannelCode(@Param("channelCode") String channelCode);

    int save(PaymentConfigDO paymentConfig);

    int update(PaymentConfigDO paymentConfig);
}
