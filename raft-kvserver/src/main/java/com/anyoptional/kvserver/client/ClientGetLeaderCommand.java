package com.anyoptional.kvserver.client;

public class ClientGetLeaderCommand implements Command {

    @Override
    public String getName() {
        return "get-leader";
    }

    @Override
    public void execute(String arguments, CommandContext context) {
        System.out.println(context.getClientLeader());
    }

}
