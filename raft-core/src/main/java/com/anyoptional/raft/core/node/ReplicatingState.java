package com.anyoptional.raft.core.node;

public class ReplicatingState {

    /**
     * 下一条需要复制的日志条目的索引
     */
    private int nextIndex;

    /**
     * 已匹配（复制）的日志条目索引
     */
    private int matchIndex;

    public int getNextIndex() {
        return nextIndex;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

}
