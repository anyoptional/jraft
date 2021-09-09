package com.anyoptional.raft.core.node;

import com.anyoptional.raft.core.node.config.NodeConfig;
import com.anyoptional.raft.core.log.Log;
import com.anyoptional.raft.core.node.store.NodeStore;
import com.anyoptional.raft.core.schedule.Scheduler;
import com.anyoptional.raft.core.support.TaskExecutor;
import com.google.common.eventbus.EventBus;
import com.anyoptional.raft.core.rpc.Connector;
import lombok.Getter;
import lombok.Setter;

/**
 * 核心组件基本上和所有组件都有联系，直接持有其它组件的引用
 * 会让核心组件的依赖非常复杂。
 * 将这些依赖组装成一个间接层，通过方法参数传递给核心组件，
 * 可以非常好的进行解耦，
 */
@Getter
@Setter
public class NodeContext {

    /**
     * 当前节点ID
     */
    private NodeId selfId;

    /**
     * 集群成员列表
     */
    private NodeGroup group;

    /**
     * 日志组件
     */
    private Log log;

    /**
     * rpc 组件
     */
    private Connector connector;

    /**
     * 部分角色状态数据存储
     */
    private NodeStore store;

    /**
     * 定时器组件
     */
    private Scheduler scheduler;

//    private NodeMode mode;

    private NodeConfig config;

    /**
     * 通信组件
     */
    @SuppressWarnings("all")
    private EventBus eventBus;

    /**
     * 主线程执行器，进行逻辑处理
     */
    private TaskExecutor taskExecutor;

    private TaskExecutor groupConfigChangeTaskExecutor;

}
