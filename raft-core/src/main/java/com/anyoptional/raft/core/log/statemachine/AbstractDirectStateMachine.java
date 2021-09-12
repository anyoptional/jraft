package com.anyoptional.raft.core.log.statemachine;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDirectStateMachine implements StateMachine {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDirectStateMachine.class);

    protected int lastApplied = 0;

    @Override
    public int getLastApplied() {
        return lastApplied;
    }

    @Override
    public void applyLog(StateMachineContext context, int index, byte[] commandBytes, int firstLogIndex) {
        if (index <= lastApplied) {
            return;
        }
        logger.debug("apply log {}", index);
        applyCommand(commandBytes);
        lastApplied = index;
    }

    protected abstract void applyCommand(byte[] commandBytes);

}
