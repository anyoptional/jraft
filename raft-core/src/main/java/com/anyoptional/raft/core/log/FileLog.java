package com.anyoptional.raft.core.log;

import com.anyoptional.raft.core.log.sequence.FileEntrySequence;

import java.io.File;

public class FileLog extends AbstractLog {

    private final RootDir rootDir;

    public FileLog(File baseDir) {
        rootDir = new RootDir(baseDir);
        LogGeneration latest = rootDir.getLatestGeneration();
        if (latest == null) {
            LogGeneration first = rootDir.createFirstGeneration();
            entrySequence = new FileEntrySequence(first, 1);
        } else {
            entrySequence = new FileEntrySequence(latest, latest.getLastIncludedIndex() + 1);
        }
    }

}
