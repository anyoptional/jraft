package com.anyoptional.raft.core.log.sequence;

import com.anyoptional.raft.core.log.entry.Entry;
import com.anyoptional.raft.core.log.entry.EntryFactory;
import com.anyoptional.raft.core.support.RandomAccessFileAdapter;
import com.anyoptional.raft.core.support.SeekableFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 日志条目文件，包含全部日志条目内容的二进制文件，文件按照写入顺序
 * 存放日志条目
 *
 *  4bytes  4bytes  4bytes  4bytes  length bytes
 * +------+-------+------+--------+---------+
 * | kind | index | term | length | payload | --->> 每行对应一个 {@link Entry}
 * +------+-------+------+--------+---------+
 * | kind | index | term | length | payload |
 * +------+-------+------+--------+---------+
 * | kind | index | term | length | payload |
 * +------+-------+------+--------+---------+
 */
public class EntriesFile implements AutoCloseable {

    private final SeekableFile seekableFile;

    public EntriesFile(File file) throws FileNotFoundException {
        seekableFile = new RandomAccessFileAdapter(file);
    }

    public EntriesFile(SeekableFile seekableFile) {
        this.seekableFile = seekableFile;
    }

    /**
     * 写入一条日志，返回写入前的文件长度，也就是
     * 待写入日志的 offset 了
     */
    public long appendEntry(Entry entry) throws IOException {
        long offset = seekableFile.size();
        seekableFile.seek(offset);
        seekableFile.writeInt(entry.getKind());
        seekableFile.writeInt(entry.getIndex());
        seekableFile.writeInt(entry.getTerm());
        byte[] commandBytes = entry.getCommandBytes();
        seekableFile.writeInt(commandBytes.length);
        seekableFile.write(commandBytes);
        return offset;
    }

    /**
     * 从指定偏移加载一条日志
     */
    public Entry loadEntry(long offset, EntryFactory factory) throws IOException {
        if (offset > seekableFile.size()) {
            throw new IllegalArgumentException("offset > size");
        }
        seekableFile.seek(offset);
        int kind = seekableFile.readInt();
        int index = seekableFile.readInt();
        int term = seekableFile.readInt();
        int length = seekableFile.readInt();
        byte[] commandBytes = new byte[length];
        seekableFile.read(commandBytes);
        return factory.create(kind, index, term, commandBytes);
    }

    public long size() throws IOException {
        return seekableFile.size();
    }

    public void clear() throws IOException {
        seekableFile.truncate(0);
    }

    public void truncate(long size) throws IOException {
        seekableFile.truncate(size);
    }

    @Override
    public void close() throws IOException {
        seekableFile.close();
    }

}
