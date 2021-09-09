package com.anyoptional.raft.core.log.entry;

/**
 * 空日志，即选举产生的新 leader 节点增加的第一条空日志
 */
public class NoOpEntry extends AbstractEntry {

    public NoOpEntry(int index, int term) {
        super(KIND_NO_OP, index, term);
    }

    @Override
    public byte[] getCommandBytes() {
        return new byte[0];
    }

    @Override
    public String toString() {
        return "NoOpEntry{" +
                "index=" + index +
                ", term=" + term +
                '}';
    }

}
