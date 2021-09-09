package com.anyoptional.raft.core.log.entry;

/**
 * 普通日志条目，即上层服务产生的日志，日志的负载是上层服务的操作内容
 */
public class GeneralEntry extends AbstractEntry {

    private final byte[] commandBytes;

    public GeneralEntry(int index, int term, byte[] commandBytes) {
        super(KIND_GENERAL, index, term);
        this.commandBytes = commandBytes;
    }

    @Override
    public byte[] getCommandBytes() {
        return commandBytes;
    }

    @Override
    public String toString() {
        return "GeneralEntry{" +
                "index=" + index +
                ", term=" + term +
                '}';
    }

}
