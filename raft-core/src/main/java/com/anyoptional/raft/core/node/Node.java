package com.anyoptional.raft.core.node;

/**
 * 一致性核心组件，整合其它各个组件并负责处理
 * 组件之间的交互，
 */
public interface Node {

    /**
     * 启动服务
     */
    void start();

    /**
     * 关闭服务
     */
    void stop() throws InterruptedException;

}
