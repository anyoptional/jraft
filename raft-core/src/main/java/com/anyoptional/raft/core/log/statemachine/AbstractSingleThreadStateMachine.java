package com.anyoptional.raft.core.log.statemachine;

import com.anyoptional.raft.core.support.SingleThreadTaskExecutor;
import com.anyoptional.raft.core.support.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异步单线程状态机
 */
public abstract class AbstractSingleThreadStateMachine implements StateMachine {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSingleThreadStateMachine.class);

    /**
     * 最后一条应用了的日志
     */
    private volatile int lastApplied = 0;

    /**
     * 状态机线程
     */
    private final TaskExecutor taskExecutor;

    public AbstractSingleThreadStateMachine() {
        taskExecutor = new SingleThreadTaskExecutor("state-machine");
    }

    @Override
    public int getLastApplied() {
        return lastApplied;
    }

    @Override
    public void applyLog(StateMachineContext context, int index, byte[] commandBytes, int firstLogIndex) {
        taskExecutor.submit(() -> doApplyLog(context, index, commandBytes, firstLogIndex));
    }

    private void doApplyLog(StateMachineContext context, int index, byte[] commandBytes, int firstLogIndex) {
        // 忽略已经应用过的日志
        if (index <= lastApplied) {
            return;
        }
        logger.debug("apply log {}", index);
        applyCommand(commandBytes);
        lastApplied = index;
    }

    @Override
    public void shutdown() {
        try {
            taskExecutor.shutdown();
        } catch (Exception e) {
            throw new StateMachineException(e);
        }
    }

    protected abstract void applyCommand(byte[] commandBytes);

}
