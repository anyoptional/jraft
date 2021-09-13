package com.anyoptional.raft.core.service;

public class NoAvailableServerException extends RuntimeException {

    public NoAvailableServerException(String message) {
        super(message);
    }

}
