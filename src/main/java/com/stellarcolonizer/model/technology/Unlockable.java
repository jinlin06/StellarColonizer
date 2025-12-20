package com.stellarcolonizer.model.technology;

/**
 * 可解锁内容接口
 * 定义游戏中可通过科技解锁的内容
 */
public interface Unlockable {
    /**
     * 获取解锁内容的唯一ID
     *
     * @return 唯一ID
     */
    String getId();

    /**
     * 获取解锁内容的显示名称
     *
     * @return 显示名称
     */
    String getName();

    /**
     * 获取解锁内容的描述
     *
     * @return 描述文本
     */
    String getDescription();

    /**
     * 获取解锁内容的类型
     *
     * @return 类型字符串
     */
    String getType();
}