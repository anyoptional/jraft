package com.anyoptional.raft.core.log.statemachine;

public class EmptyStateMachine implements StateMachine {

    private int lastApplied = 0;

    @Override
    public int getLastApplied() {
        return lastApplied;
    }

    @Override
    public void setLastApplied(int lastApplied) {
        this.lastApplied = lastApplied;
    }

    @Override
    public void applyLog(StateMachineContext context, int index,byte[] commandBytes, int firstLogIndex) {
        lastApplied = index;
    }

    @Override
    public void shutdown() {
    }

}
