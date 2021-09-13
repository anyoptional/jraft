package com.anyoptional.kvserver.client;

public interface Command {

    String getName();

    void execute(String arguments, CommandContext context);

}
