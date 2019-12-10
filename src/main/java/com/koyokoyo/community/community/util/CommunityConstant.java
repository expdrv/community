package com.koyokoyo.community.community.util;

public interface CommunityConstant {
    /**
     *
     * 一些常量：激活成功
     */
    int ACTIVATION_SUCCESS=0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT=1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE=2;

    /**
     * 默认凭证时间
     *
     */
    int DEFAULT_EXPIRED_SECONDS=3600*12;

    /**
     * 勾选记住的凭证时间
     */
    int REMEMBER_EXPIRED_SECONDS=3600*24*30;

    /**
     * 实体类型
     */

    int ENTITY_TYPE_POST=1;

    int ENTITY_TYPE_COMMENT=2;

    int ENTITY_TYPE_USER=3;
}
