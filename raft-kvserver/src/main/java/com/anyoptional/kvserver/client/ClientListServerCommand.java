package com.anyoptional.kvserver.client;

public class ClientListServerCommand implements Command {

    @Override
    public String getName() {
        return "list-server";
    }

    @Override
    public void execute(String arguments, CommandContext context) {
        context.printSeverList();
    }

}
