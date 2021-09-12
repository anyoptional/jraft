package com.anyoptional.kvserver.server;

import com.anyoptional.kvserver.message.*;
import com.anyoptional.raft.core.log.statemachine.AbstractSingleThreadStateMachine;
import com.anyoptional.raft.core.node.Node;
import com.anyoptional.raft.core.node.role.RoleName;
import com.anyoptional.raft.core.node.role.RoleNameAndLeaderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Service {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    /**
     * 一致性核心组件
     */
    private final Node node;

    /**
     * 待处理的请求
     * key -> requestId
     * value -> 与客户端的连接及待处理的命令
     */
    private final ConcurrentMap<String, CommandRequest<?>> pendingCommands = new ConcurrentHashMap<>();

    /**
     * K/V 服务的数据
     */
    private final Map<String, byte[]> map = new HashMap<>();

    public Service(Node node) {
        this.node = node;
        this.node.registerStateMachine(new StateMachineImpl());
    }

    /**
     * SET 命令
     */
    public void set(CommandRequest<SetCommand> commandRequest) {
        // 如果当前节点不是 leader，返回重定向
        Redirect redirect = checkLeadership();
        if (redirect != null) {
            commandRequest.reply(redirect);
            return;
        }

        // 记录与客户端连接的映射，并在连接关闭时清除
        SetCommand command = commandRequest.getCommand();
        logger.debug("set {}", command.getKey());
        pendingCommands.put(command.getRequestId(), commandRequest);
        commandRequest.addCloseListener(() -> pendingCommands.remove(command.getRequestId()));

        node.appendLog(command.toBytes());
    }

    /**
     * GET
     */
    public void get(CommandRequest<GetCommand> commandRequest) {
        String key = commandRequest.getCommand().getKey();
        logger.debug("get {}", key);
        byte[] value = this.map.get(key);
        commandRequest.reply(new GetCommandResponse(value));
    }


    @Nullable
    private Redirect checkLeadership() {
        RoleNameAndLeaderId state = node.getRoleNameAndLeaderId();
        if (state.getRoleName() != RoleName.LEADER) {
            return new Redirect(state.getLeaderId());
        }
        return null;
    }


    private class StateMachineImpl extends AbstractSingleThreadStateMachine {
        @Override
        protected void applyCommand(byte[] commandBytes) {
            // 恢复命令
            SetCommand command = SetCommand.fromBytes(commandBytes);
            // 修改数据
            map.put(command.getKey(), command.getValue());
            // 查找连接
            CommandRequest<?> commandRequest = pendingCommands.remove(command.getRequestId());
            if (commandRequest != null) {
                commandRequest.reply(Success.INSTANCE);
            }
        }
    }
}
