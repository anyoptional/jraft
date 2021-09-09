package com.anyoptional.raft.core.log.entry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 日志条目
 */
@JsonDeserialize(using = EntryDeserializer.class)
public interface Entry {

    // 空日志
    int KIND_NO_OP = 0;
    // 普通日志，携带负载
    int KIND_GENERAL = 1;

    /**
     * 获取日志类型
     */
    int getKind();

    /**
     * 获取日志的term
     */
    int getTerm();

    /**
     * 获取日志的索引
     */
    int getIndex();

    /**
     * 获取日志元信息
     */
    EntryMeta getMeta();

    /**
     * 获取日志负载
     */
    byte[] getCommandBytes();

}
