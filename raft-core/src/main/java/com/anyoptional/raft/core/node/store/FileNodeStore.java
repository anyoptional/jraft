package com.anyoptional.raft.core.node.store;

import com.anyoptional.raft.core.node.NodeId;
import com.anyoptional.raft.core.support.RandomAccessFileAdapter;
import com.anyoptional.raft.core.support.SeekableFile;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 以二进制格式存储 term 和 votedFor
 *  term 4bytes
 *  votedFor
 *    4 bytes length
 *    n bytes content
 */
public class FileNodeStore implements NodeStore {

    public static final String FILE_NAME = "node.bin";


    private static final long OFFSET_TERM = 0;

    private static final long OFFSET_VOTED_FOR = 4;

    private int term;

    @Nullable
    private NodeId votedFor;

    private final SeekableFile seekableFile;

    public FileNodeStore(File file) {
        try {
            if (!file.exists()) {
                Files.touch(file);
            }
            seekableFile = new RandomAccessFileAdapter(file);
            initializeOrLoad();
        } catch (IOException ex) {
            throw new NodeStoreException(ex);
        }
    }

    public FileNodeStore(SeekableFile seekableFile) {
        Preconditions.checkNotNull(seekableFile);
        this.seekableFile = seekableFile;
        try {
            initializeOrLoad();
        } catch (IOException ex) {
            throw new NodeStoreException(ex);
        }
    }

    private void initializeOrLoad() throws IOException {
        // 空文件
        // 初始个零值
        if (seekableFile.size() == 0) {
            // 4 + 4
            seekableFile.truncate(8);
            seekableFile.seek(0);
            // term
            seekableFile.writeInt(0);
            // votedFor length
            seekableFile.writeInt(0);
        }
        // 有内容了，进行读取
        else {
            term = seekableFile.readInt();
            int votedForLength = seekableFile.readInt();
            if (votedForLength > 0) {
                byte[] bytes = new byte[votedForLength];
                int numRead = seekableFile.read(bytes);
                if (numRead != votedForLength) {
                    throw new NodeStoreException("VotedFor content mismatch");
                }
                votedFor = new NodeId(new String(bytes, StandardCharsets.UTF_8));
            }
        }
    }

    @Override
    public int getTerm() {
        return term;
    }

    @Override
    public void setTerm(int term) {
        try {
            seekableFile.seek(OFFSET_TERM);
            seekableFile.writeInt(term);
        } catch (IOException e) {
            throw new NodeStoreException(e);
        }
        this.term = term;
    }

    @Override
    @Nullable
    public NodeId getVotedFor() {
        return votedFor;
    }

    @Override
    public void setVotedFor(@Nullable NodeId votedFor) {
        try {
            seekableFile.seek(OFFSET_VOTED_FOR);
            if (votedFor == null) {
                seekableFile.writeInt(0);
            } else {
                byte[] bytes = votedFor.getValue().getBytes(StandardCharsets.UTF_8);
                seekableFile.writeInt(bytes.length);
                seekableFile.write(bytes);
            }
        } catch (IOException e) {
            throw new NodeStoreException(e);
        }
        this.votedFor = votedFor;
    }

    @Override
    public void close() {
        try {
            seekableFile.close();
        } catch (IOException e) {
            throw new NodeStoreException(e);
        }
    }

}
