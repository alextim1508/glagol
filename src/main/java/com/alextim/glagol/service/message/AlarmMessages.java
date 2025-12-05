package com.alextim.glagol.service.message;

import com.alextim.glagol.client.SomeMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.alextim.glagol.service.protocol.Command.RESTART;

public class AlarmMessages {

    @Slf4j
    @Getter
    public static class RestartAlarm extends AlarmEvent {

        private final int deviceNumber;

        public RestartAlarm(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time);
            deviceNumber = baseMsg.id & 0x1F;
            log.debug("Device number: {}", deviceNumber);
        }

        @Override
        public String toString() {
            return RESTART.getDescription();
        }
    }

    public static class AlarmEvent extends SomeMessage {
        public AlarmEvent(int id, byte[] data, long time) {
            super(id, data, time);
        }
    }
}
