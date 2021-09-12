package com.anyoptional.raft.core.log.statemachine;

/**
 * 由上层服务实现，由此 raft 核心可以间接调用到上层服务
 */
public interface StateMachine {

    /**
     * 最后一条由上层服务应用了的日志条目
     */
    int getLastApplied();

    /**
     * 应用日志
     * 日志组件回调上层服务的主要方法，上层服务在 applyLog(...)
     * 中实现命令应用、数据修改和回复客户端等操作
     */
    void applyLog(StateMachineContext context, int index, byte[] commandBytes, int firstLogIndex);

    /**
     * 关闭
     */
    void shutdown();
}
