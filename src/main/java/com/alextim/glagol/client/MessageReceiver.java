package com.alextim.glagol.client;

public interface MessageReceiver {
    void writeMsg(SomeMessage someMessage);

    void shutDown();

    SomeMessage getNextMessage();
}
