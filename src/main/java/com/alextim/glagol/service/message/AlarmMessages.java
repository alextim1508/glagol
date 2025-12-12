package com.alextim.glagol.service.message;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.service.protocol.Command;
import lombok.extern.slf4j.Slf4j;

import static com.alextim.glagol.service.protocol.Command.RESTART;

public class AlarmMessages {

    @Slf4j
    public static class Alarm extends AlarmEvent {

        public final int deviceNumber;

        public Alarm(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time, RESTART);
            deviceNumber = baseMsg.id & 0x1F;
            log.debug("Device number: {}", deviceNumber);
        }

        @Override
        public String toString() {
            return "Unknown alarm";
        }
    }

    @Slf4j
    public static class AlarmEvent extends SomeMessage {

        public final Command command;

        public AlarmEvent(int id, byte[] data, long time, Command command) {
            super(id, data, time);
            this.command = command;
            log.debug("Command: {}", command);
        }
    }
}
