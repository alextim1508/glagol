package com.alextim.glagol.service;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.client.ucan.CanTransfer;
import com.alextim.glagol.client.MessageReceiver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CanService {

    private MessageReceiver transfer;
    private final ScheduledExecutorService messageProcessor =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "CAN-Message-Processor"));

    public CanService() {
        transfer = new CanTransfer();

        messageProcessor.scheduleAtFixedRate(this::processQueuedMessages, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void processQueuedMessages() {
        SomeMessage message;
        while ((message = transfer.getNextMessage()) != null) {
            logReceivedMessage(message);
        }
    }

    private void logReceivedMessage(SomeMessage message) {
        System.out.println("MSG: " + message);
    }

    public void startMeas() {
        transfer.writeMsg(new SomeMessage(0x320, new byte[]{
                (byte) 0x40,
                (byte) 0x0,
                (byte) 0x0,
                (byte) 0x0,
                (byte) 0x0,
                (byte) 0x0,
                (byte) 0x0,
                (byte) 0x0
        }, 0));
    }

    public void shutDown() {
        messageProcessor.shutdown();
        try {
            if (!messageProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageProcessor.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageProcessor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        transfer.shutDown();
    }
}