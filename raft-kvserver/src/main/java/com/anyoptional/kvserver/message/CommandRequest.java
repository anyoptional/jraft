package com.anyoptional.kvserver.message;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

public class CommandRequest<T> {

    /**
     * 客户端发送过来的命令
     */
    private final T command;

    /**
     * 与客户端的连接
     */
    private final Channel channel;

    public CommandRequest(T command, Channel channel) {
        this.command = command;
        this.channel = channel;
    }

    /**
     * 回复响应给客户端
     */
    public void reply(Object response) {
        this.channel.writeAndFlush(response);
    }

    /**
     * 连接关闭时做一些收尾工作
     */
    public void addCloseListener(Runnable runnable) {
        this.channel.closeFuture().addListener((ChannelFutureListener) future -> runnable.run());
    }

    public T getCommand() {
        return command;
    }

}
