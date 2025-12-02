package com.alextim.glagol.client.ucan;

import com.alextim.glagol.client.MessageReceiver;
import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.client.ucan.callback.ConnectControlCallbackEx;
import com.alextim.glagol.client.ucan.constant.CanBaudRate;
import com.alextim.glagol.client.ucan.constant.UsbCanError;
import com.alextim.glagol.client.ucan.event.CanEvent;
import com.alextim.glagol.client.ucan.structure.CanMsg;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.alextim.glagol.client.ucan.CanDriver.*;
import static com.alextim.glagol.client.ucan.CanInitializationConfig.createInitParam;
import static com.alextim.glagol.client.ucan.constant.CanBaudRate.USBCAN_BAUD_125kBit;
import static com.alextim.glagol.client.ucan.constant.CanModule.USBCAN_ANY_MODULE;

@Slf4j
public class CanTransfer implements MessageReceiver {

    private NativeLong ucanHandler;
    private final CanMsg reusableCanMsg;

    private final ScheduledExecutorService messagePoller =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "CAN-Message-Poller"));
    private final Queue<SomeMessage> messageQueue = new ConcurrentLinkedQueue<>();
    private final long POLL_PERIOD_MS = 50;

    public CanTransfer() {
        ucanHandler = new NativeLong(-1);
        reusableCanMsg = new CanMsg();
        hwConnect();
        initialize(USBCAN_ANY_MODULE.getValue(), USBCAN_BAUD_125kBit.getValue());
        reset();
    }

    private void hwConnect() {
        ConnectControlCallbackEx connectControlCallback = this::connectControlCallbackEx;

        UsbCanError connectControlResult = initHwConnectControl(connectControlCallback, ucanHandler);
        if (connectControlResult.isSuccess()) {
            log.info("Hardware connect control initialized successfully");
        } else {
            throw new RuntimeException("Failed to initialize hardware connect control: " + connectControlResult);
        }
    }

    private void initialize(byte deviceNr, short wBTR) {
        IntByReference handleRef = new IntByReference();

        UsbCanError initHwResult = initHardware(handleRef, deviceNr, null, null);
        if (!initHwResult.isSuccess()) {
            UsbCanError initCanResult = deinitHardware(new NativeLong(handleRef.getValue()));
            if (!initCanResult.isSuccess()) {
                log.warn("Hardware deinitialized after error: {}", initCanResult);
            }

            throw new RuntimeException(
                    "Failed to initialize hardware: " + initHwResult.getValue() +
                            (initCanResult.isSuccess() ? ". Hardware deinitialized successfully" :
                                    ". Failed to deinitialize hardware: " + initCanResult.getValue()));
        }

        ucanHandler = new NativeLong(handleRef.getValue());

        UsbCanError initCanResult = initCan(ucanHandler, createInitParam(wBTR));
        if (!initCanResult.isSuccess()) {
            UsbCanError deinitCanResult = deinitCan(ucanHandler);
            if (!deinitCanResult.isSuccess()) {
                log.warn("CAN interface deinitialized after error: {}", deinitCanResult);
            }

            UsbCanError deinitHwResult = deinitHardware(ucanHandler);
            if (!deinitHwResult.isSuccess()) {
                log.warn("Hardware deinitialized after error: {}", deinitHwResult);
            }

            throw new RuntimeException(
                    "Failed to initialize CAN interface: " + initCanResult.getValue() +
                            (deinitCanResult.isSuccess() ? ". CAN interface deinitialized successfully" :
                                    ". Failed to deinitialize CAN interface: " + deinitCanResult.getValue()) +
                            (deinitHwResult.isSuccess() ? ", Hardware deinitialized successfully" :
                                    ", Failed to deinitialize hardware: " + deinitHwResult.getValue()));
        }

        messagePoller.scheduleAtFixedRate(this::pollCanMessages, 0, POLL_PERIOD_MS, TimeUnit.MILLISECONDS);
        log.info("CAN interface initialized successfully with baud rate: {}", CanBaudRate.fromValue(wBTR));
    }

    private void reset() {
        log.info("Resetting CAN interface");

        UsbCanError resetCanResult = resetCan(ucanHandler);
        if (!resetCanResult.isSuccess()) {
            throw new RuntimeException(
                    "Failed to reset CAN interface: " + resetCanResult.getValue());
        }

        log.info("CAN interface reset successfully");
    }

    private void pollCanMessages() {
        reusableCanMsg.write();

        UsbCanError result = readCanMsg(ucanHandler, reusableCanMsg);
        if (!result.isError()) {
            reusableCanMsg.read();

            if (result != UsbCanError.USBCAN_WARN_NODATA) {
                byte[] data = new byte[reusableCanMsg.m_bDLC & 0xFF];
                System.arraycopy(reusableCanMsg.m_bData, 0, data, 0, data.length);

                SomeMessage someMessage = new SomeMessage(
                        reusableCanMsg.m_dwID,
                        data,
                        reusableCanMsg.m_dwTime
                );

                messageQueue.offer(someMessage);
            }
        } else  {
            throw new RuntimeException("Error reading CAN messages: " + result);
        }
    }

    @Override
    public void writeMsg(SomeMessage someMessage) {
        log.info("Writing CAN message: {}", someMessage);

        CanMsg canMsg = new CanMsg(someMessage);
        log.debug("Converted to internal CAN structure: {}", canMsg);

        UsbCanError result = writeCanMsg(ucanHandler, canMsg);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to write CAN message: " + result);
        }

        log.info("Successfully wrote CAN message");
    }

    @Override
    public void shutDown() {
        log.info("Shutting down CAN transfer");

        messagePoller.shutdown();
        try {
            if (!messagePoller.awaitTermination(5, TimeUnit.SECONDS)) {
                messagePoller.shutdownNow();
            }
        } catch (InterruptedException e) {
            messagePoller.shutdownNow();
            Thread.currentThread().interrupt();
        }

        try {
            UsbCanError deinitCanResult = deinitCan(ucanHandler);
            if (!deinitCanResult.isSuccess()) {
                log.warn("Failed to deinitialize CAN interface: {}", deinitCanResult);
            } else {
                log.debug("CAN interface deinitialized successfully: {}", deinitCanResult);
            }

            UsbCanError deinitHwResult = deinitHardware(ucanHandler);
            if (!deinitHwResult.isSuccess()) {
                log.warn("Failed to deinitialize hardware: {}", deinitHwResult);
            } else {
                log.debug("Hardware deinitialized successfully: {}", deinitHwResult);
            }
        } finally {
            ucanHandler = new NativeLong(-1);
        }

        try {
            UsbCanError deinitConnectControlResult = deinitHwConnectControl();
            if (!deinitConnectControlResult.isSuccess()) {
                log.warn("Failed to deinitialize hardware connect control: {}", deinitConnectControlResult);
            } else {
                log.debug("Hardware connect control deinitialized successfully: {}", deinitConnectControlResult);
            }
        } catch (RuntimeException e) {
            log.error("Exception during hardware connect control deinitialization", e);
        }

        log.info("CAN transfer shut down completed");

    }

    private void connectControlCallbackEx(int event, int param, Pointer arg) {
        CanEvent canEvent = CanEvent.fromValue(event);
        log.debug("Connect control event: {}, param: {}", event, param);

        switch (canEvent) {
            case USBCAN_EVENT_CONNECT:
                log.info("Event: New USB-CAN module connected");
                break;
            case USBCAN_EVENT_DISCONNECT:
                log.info("Event: USB-CAN module disconnected");
                break;
            case USBCAN_EVENT_FATALDISCON:
                log.warn("Event: Used USB-CAN module disconnected, data loss possible");
                break;
            default:
                log.warn("Event: Unknown connect control event: {}", event);
                break;
        }
    }

    @Override
    public SomeMessage getNextMessage() {
        return messageQueue.poll();
    }
}