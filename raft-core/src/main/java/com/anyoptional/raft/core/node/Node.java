package com.anyoptional.raft.core.node;

import com.anyoptional.raft.core.log.statemachine.StateMachine;
import com.anyoptional.raft.core.node.role.RoleNameAndLeaderId;

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

    /**
     * 注册状态机
     */
    void registerStateMachine(StateMachine stateMachine);

    /**
     * 追加日志
     * 上层服务提供命令内容，核心组件负责追加日志、节点间复制等操作
     */
    void appendLog(byte[] commandBytes);

    /**
     * 当前节点的角色和集群的leader id
     */
    RoleNameAndLeaderId getRoleNameAndLeaderId();

}
