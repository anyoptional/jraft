package com.anyoptional.raft.core.node.config;

import java.io.IOException;
import java.io.InputStream;

public interface NodeConfigLoader {

    NodeConfig load(InputStream input) throws IOException;

}
