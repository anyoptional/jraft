package com.anyoptional.raft.core.node.log;

import java.io.File;

public class NormalLogDir extends AbstractLogDir {

    NormalLogDir(File dir) {
        super(dir);
    }

    @Override
    public String toString() {
        return "NormalLogDir{" +
                "dir=" + dir +
                '}';
    }

}
