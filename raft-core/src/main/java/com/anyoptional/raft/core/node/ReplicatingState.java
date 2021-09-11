package com.anyoptional.raft.core.node;

public class ReplicatingState {

    /**
     * 下一条需要复制的日志条目的索引
     * leader 准备发给 follower 的下一条日志条目的索引，
     * 初始化为 leader 日志的 nextLogIndex，也就是初始
     * 情况下默认 follower 和 leader 有着相同的进度，后
     * 续根据 AppendEntries RPCs 的响应来进行调整，最终
     * nextIndex 会来到一个 leader 和 follower 都持有的位置
     */
    private int nextIndex;

    /**
     * 已匹配（复制）的日志条目索引
     */
    private int matchIndex;

    private boolean replicating = false;
    private long lastReplicatedAt = 0;

    ReplicatingState(int nextIndex) {
        this(nextIndex, 0);
    }

    ReplicatingState(int nextIndex, int matchIndex) {
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    /**
     * Back off next index, in other word, decrease.
     *
     * @return true if decrease successfully, false if next index is less than or equal to {@code 1}
     */
    boolean backOffNextIndex() {
        if (nextIndex > 1) {
            nextIndex--;
            return true;
        }
        return false;
    }

    /**
     * Advance next index and match index by last entry index.
     *
     * @param lastEntryIndex last entry index
     * @return true if advanced, false if no change
     */
    boolean advance(int lastEntryIndex) {
        // changed
        boolean result = (matchIndex != lastEntryIndex || nextIndex != (lastEntryIndex + 1));

        matchIndex = lastEntryIndex;
        nextIndex = lastEntryIndex + 1;

        return result;
    }

    /**
     * Test if replicating.
     *
     * @return true if replicating, otherwise false
     */
    boolean isReplicating() {
        return replicating;
    }

    /**
     * Set replicating.
     *
     * @param replicating replicating
     */
    void setReplicating(boolean replicating) {
        this.replicating = replicating;
    }

    /**
     * Get last replicated timestamp.
     *
     * @return last replicated timestamp
     */
    long getLastReplicatedAt() {
        return lastReplicatedAt;
    }

    /**
     * Set last replicated timestamp.
     *
     * @param lastReplicatedAt last replicated timestamp
     */
    void setLastReplicatedAt(long lastReplicatedAt) {
        this.lastReplicatedAt = lastReplicatedAt;
    }

    @Override
    public String toString() {
        return "ReplicatingState{" +
                "nextIndex=" + nextIndex +
                ", matchIndex=" + matchIndex +
                ", replicating=" + replicating +
                ", lastReplicatedAt=" + lastReplicatedAt +
                '}';
    }

}
