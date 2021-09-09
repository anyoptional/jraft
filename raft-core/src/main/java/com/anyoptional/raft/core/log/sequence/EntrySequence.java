package com.anyoptional.raft.core.log.sequence;

import com.anyoptional.raft.core.log.entry.Entry;
import com.anyoptional.raft.core.log.entry.EntryMeta;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 对日志文件中系列日志条目的抽象
 */
public interface EntrySequence extends AutoCloseable {

    /**
     * 是否为空
     */
    boolean isEmpty();

    /**
     * 获取第一条日志的索引
     */
    int getFirstLogIndex();

    /**
     * 获取最后一条日志的索引
     */
    int getLastLogIndex();

    /**
     * 获取下一条日志的索引
     */
    int getNextLogIndex();

    /**
     * 获取序列的子视图，从 fromIndex 开始
     */
    List<Entry> subList(int fromIndex);

    // [fromIndex, toIndex)
    List<Entry> subList(int fromIndex, int toIndex);

//    GroupConfigEntryList buildGroupConfigEntryList();

    /**
     * 检查某个日志条目是否存在
     */
    boolean isEntryPresent(int index);

    /**
     * 获取某个日志条目的元信息
     */
    @Nullable
    EntryMeta getEntryMeta(int index);

    /**
     * 获取某个日志条目
     */
    @Nullable
    Entry getEntry(int index);

    /**
     * 获取最后一条日志条目
     */
    @Nullable
    Entry getLastEntry();

    /**
     * 追加日志条目
     */
    void append(Entry entry);

    /**
     * 批量追加日志条目
     */
    void append(List<Entry> entries);

    /**
     * 推进 commit index
     */
    void commit(int index);

    /**
     * 获取当前 commit index
     */
    int getCommitIndex();

    /**
     * 删除自 index 后的日志条目，如果追加来自 leader
     * 节点的日志出现冲突，移除现有日志，以 leader 为准
     */
    void removeAfter(int index);

    @Override
    void close();

}
