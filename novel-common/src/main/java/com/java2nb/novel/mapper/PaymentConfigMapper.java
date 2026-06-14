package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.PaymentConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

/**
 * 前台支付配置只需要按渠道读取，因此这里提供最小可用的共享 Mapper。
 */
@Mapper
public interface PaymentConfigMapper {

    @Select("""
        select id,
               channel_code,
               channel_name,
               app_id,
               public_key,
               private_key,
               notify_url,
               return_url,
               gateway_url,
               sign_type,
               charset,
               pay_environment,
               enabled,
               wechat_enabled,
               alipay_personal_enabled,
               wechat_personal_enabled,
               remark,
               alipay_qr_code_url,
               wechat_qr_code_url,
               create_time,
               create_user_id,
               update_time,
               update_user_id
          from payment_config
         where channel_code = #{channelCode}
         limit 1
        """)
    @Results(id = "PaymentConfigResult", value = {
        @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, id = true),
        @Result(column = "channel_code", property = "channelCode", jdbcType = JdbcType.VARCHAR),
        @Result(column = "channel_name", property = "channelName", jdbcType = JdbcType.VARCHAR),
        @Result(column = "app_id", property = "appId", jdbcType = JdbcType.VARCHAR),
        @Result(column = "public_key", property = "publicKey", jdbcType = JdbcType.LONGVARCHAR),
        @Result(column = "private_key", property = "privateKey", jdbcType = JdbcType.LONGVARCHAR),
        @Result(column = "notify_url", property = "notifyUrl", jdbcType = JdbcType.VARCHAR),
        @Result(column = "return_url", property = "returnUrl", jdbcType = JdbcType.VARCHAR),
        @Result(column = "gateway_url", property = "gatewayUrl", jdbcType = JdbcType.VARCHAR),
        @Result(column = "sign_type", property = "signType", jdbcType = JdbcType.VARCHAR),
        @Result(column = "charset", property = "charset", jdbcType = JdbcType.VARCHAR),
        @Result(column = "pay_environment", property = "payEnvironment", jdbcType = JdbcType.VARCHAR),
        @Result(column = "enabled", property = "enabled", jdbcType = JdbcType.TINYINT),
        @Result(column = "wechat_enabled", property = "wechatEnabled", jdbcType = JdbcType.TINYINT),
        @Result(column = "alipay_personal_enabled", property = "alipayPersonalEnabled", jdbcType = JdbcType.TINYINT),
        @Result(column = "wechat_personal_enabled", property = "wechatPersonalEnabled", jdbcType = JdbcType.TINYINT),
        @Result(column = "remark", property = "remark", jdbcType = JdbcType.VARCHAR),
        @Result(column = "alipay_qr_code_url", property = "alipayQrCodeUrl", jdbcType = JdbcType.VARCHAR),
        @Result(column = "wechat_qr_code_url", property = "wechatQrCodeUrl", jdbcType = JdbcType.VARCHAR),
        @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
        @Result(column = "create_user_id", property = "createUserId", jdbcType = JdbcType.BIGINT),
        @Result(column = "update_time", property = "updateTime", jdbcType = JdbcType.TIMESTAMP),
        @Result(column = "update_user_id", property = "updateUserId", jdbcType = JdbcType.BIGINT)
    })
    PaymentConfig selectByChannelCode(@Param("channelCode") String channelCode);

    @Insert("""
        insert into payment_config (
            channel_code,
            channel_name,
            app_id,
            public_key,
            private_key,
            notify_url,
            return_url,
            gateway_url,
            sign_type,
            charset,
            pay_environment,
            enabled,
            wechat_enabled,
            alipay_personal_enabled,
            wechat_personal_enabled,
            remark,
            alipay_qr_code_url,
            wechat_qr_code_url,
            create_time,
            create_user_id,
            update_time,
            update_user_id
        ) values (
            #{channelCode},
            #{channelName},
            #{appId},
            #{publicKey},
            #{privateKey},
            #{notifyUrl},
            #{returnUrl},
            #{gatewayUrl},
            #{signType},
            #{charset},
            #{payEnvironment},
            #{enabled},
            #{wechatEnabled},
            #{alipayPersonalEnabled},
            #{wechatPersonalEnabled},
            #{remark},
            #{alipayQrCodeUrl},
            #{wechatQrCodeUrl},
            #{createTime},
            #{createUserId},
            #{updateTime},
            #{updateUserId}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PaymentConfig paymentConfig);

    @Update("""
        update payment_config
           set channel_name = #{channelName},
               app_id = #{appId},
               public_key = #{publicKey},
               private_key = #{privateKey},
               notify_url = #{notifyUrl},
               return_url = #{returnUrl},
               gateway_url = #{gatewayUrl},
               sign_type = #{signType},
               charset = #{charset},
               pay_environment = #{payEnvironment},
               enabled = #{enabled},
               wechat_enabled = #{wechatEnabled},
               alipay_personal_enabled = #{alipayPersonalEnabled},
               wechat_personal_enabled = #{wechatPersonalEnabled},
               remark = #{remark},
               alipay_qr_code_url = #{alipayQrCodeUrl},
               wechat_qr_code_url = #{wechatQrCodeUrl},
               update_time = #{updateTime},
               update_user_id = #{updateUserId}
         where channel_code = #{channelCode}
        """)
    int updateByChannelCode(PaymentConfig paymentConfig);
}
