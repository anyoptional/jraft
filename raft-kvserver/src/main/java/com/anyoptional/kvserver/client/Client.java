package com.anyoptional.kvserver.client;


import com.anyoptional.kvserver.message.GetCommand;
import com.anyoptional.kvserver.message.SetCommand;
import com.anyoptional.raft.core.service.ServerRouter;

public class Client {

    public static final String VERSION = "0.1.0";

    private final ServerRouter serverRouter;

    public Client(ServerRouter serverRouter) {
        this.serverRouter = serverRouter;
    }

//    public void addNote(String nodeId, String host, int port) {
//        serverRouter.send(new AddNodeCommand(nodeId, host, port));
//    }
//
//    public void removeNode(String nodeId) {
//        serverRouter.send(new RemoveNodeCommand(nodeId));
//    }

    public void set(String key, byte[] value) {
        serverRouter.send(new SetCommand(key, value));
    }

    public byte[] get(String key) {
        return (byte[]) serverRouter.send(new GetCommand(key));
    }

    public ServerRouter getServerRouter() {
        return serverRouter;
    }

}
