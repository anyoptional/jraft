package com.anyoptional.raft.core.support;

import java.io.IOException;
import java.io.InputStream;

public interface SeekableFile extends AutoCloseable {

    /**
     * 获取当前位置
     */
    long position() throws IOException;

    /**
     * 移动到指定位置
     */
    void seek(long position) throws IOException;

    /**
     * 写入整形
     */
    void writeInt(int i) throws IOException;

    /**
     * 写入长整形
     */
    void writeLong(long l) throws IOException;

    /**
     * 写入字节数组
     */
    void write(byte[] b) throws IOException;

    int readInt() throws IOException;

    /**
     * 读取长整型
     */
    long readLong() throws IOException;

    /**
     * 读取字节数组
     */
    int read(byte[] b) throws IOException;

    /**
     * 文件大小
     */
    long size() throws IOException;

    /**
     * 裁剪到指定大小
     */
    void truncate(long size) throws IOException;

    /**
     * 获取从指定位置开始的输入流
     */
    InputStream inputStream(long start) throws IOException;

    /**
     * 输出到磁盘
     */
    void flush() throws IOException;

    @Override
    void close() throws IOException;

}
