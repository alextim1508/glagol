package com.alextim.glagol.service.message;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.service.protocol.AddressInfo;
import com.alextim.glagol.service.protocol.CommandStatus;
import com.alextim.glagol.service.protocol.Parameter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.alextim.glagol.service.protocol.AddressInfo.createId;
import static com.alextim.glagol.service.protocol.Command.*;
import static com.alextim.glagol.service.protocol.DeviceType.ALL_DEVICES;
import static com.alextim.glagol.service.protocol.MessageCategory.CONTROL;

public class CommandMessages {

    @Slf4j
    @Getter
    public static class RestartCommand extends CommandMessage {

        public RestartCommand() {
            super(createId(new AddressInfo(CONTROL, true, ALL_DEVICES, 0)),
                    createDataBytes());
        }

        private static byte[] createDataBytes() {
            byte[] bytes = {
                    RESTART.getCode(),
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00};
            log.debug("Data: {}", formatDataAsHex(bytes));
            return bytes;
        }

        @Override
        public String toString() {
            return RESTART.getDescription();
        }
    }

    @Slf4j
    @Getter
    public static class GetParamCommand extends CommandMessage {

        public final Parameter param;

        public GetParamCommand(Parameter param) {
            super(createId(new AddressInfo(CONTROL, true, ALL_DEVICES, 0)),
                    createDataBytes(param));
            this.param = param;
        }

        private static byte[] createDataBytes(Parameter param) {
            byte[] bytes = {
                    GET_PARAM.getCode(),
                    param.getCode(),
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00};
            log.debug("Data: {}", formatDataAsHex(bytes));
            return bytes;
        }

        @Override
        public String toString() {
            return String.format("%s. Параметр=%s", GET_PARAM.getDescription(), param.getDescription());
        }
    }

    @Slf4j
    @Getter
    public static class GetParamAnswer extends AnswerMessage {

        public final Parameter param;
        public final Number value;

        public GetParamAnswer(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time);

            this.param = Parameter.fromCode(baseMsg.data[2]);
            log.debug("Param: {}", param);

            byte[] valueBytes = new byte[4];
            System.arraycopy(baseMsg.data, 4, valueBytes, 0, 4);

            ByteBuffer bb = ByteBuffer.wrap(valueBytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            if (param.isFloat()) {
                this.value = bb.getFloat();
            } else {
                this.value = bb.getInt();
            }
            log.debug("Value: {}", value);
        }

        @Override
        public String toString() {
            return String.format("%s. Параметр=%s, Значение=%s}",
                    GET_PARAM.getDescription(), param.getDescription(), value);
        }
    }

    @Slf4j
    @Getter
    public static class SetParamCommand extends CommandMessage {

        public final Parameter param;
        public final Number value;

        public SetParamCommand(Parameter param, Number value) {
            super(createId(new AddressInfo(CONTROL, true, ALL_DEVICES, 0)),
                    createDataBytes(param, value));
            this.param = param;
            this.value = value;
        }

        private static byte[] createDataBytes(Parameter param, Number value) {
            byte[] bytes = new byte[8];
            bytes[0] = SET_PARAM.getCode();
            bytes[1] = param.getCode();
            bytes[2] = 0x00;

            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            if (param.isFloat()) {
                bb.putFloat(value.floatValue());
            } else {
                bb.putInt(value.intValue());
            }

            byte[] valueBytes = bb.array();
            System.arraycopy(valueBytes, 0, bytes, 4, 4);

            log.debug("Data: {}", formatDataAsHex(bytes));
            return bytes;
        }

        @Override
        public String toString() {
            return String.format("%s. Параметр=%s, Значение=%s}",
                    SET_PARAM.getDescription(), param.getDescription(), value);
        }
    }

    @Slf4j
    @Getter
    public static class SetParamAnswer extends AnswerMessage {

        public final Parameter param;
        public final Number value;

        public SetParamAnswer(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time);

            this.param = Parameter.fromCode(baseMsg.data[2]);
            log.debug("Param: {}", param);

            byte[] valueBytes = new byte[4];
            System.arraycopy(baseMsg.data, 4, valueBytes, 0, 4);

            ByteBuffer bb = ByteBuffer.wrap(valueBytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            if (param.isFloat()) {
                this.value = bb.getFloat();
            } else {
                this.value = bb.getInt();
            }
            log.debug("Value: {}", value);
        }

        @Override
        public String toString() {
            return String.format("%s. Параметр=%s, Значение=%s}",
                    SET_PARAM.getDescription(), param.getDescription(), value);
        }
    }

    @Slf4j
    @Getter
    public static class StartMeasureCommand extends CommandMessage {

        public StartMeasureCommand() {
            super(createId(new AddressInfo(CONTROL, true, ALL_DEVICES, 0)),
                    createDataBytes());
        }

        private static byte[] createDataBytes() {
            byte[] bytes = {
                    START_MEASURE.getCode(),
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00};
            log.debug("Data: {}", formatDataAsHex(bytes));
            return bytes;
        }

        @Override
        public String toString() {
            return START_MEASURE.getDescription();
        }
    }

    @Slf4j
    @Getter
    public static class StartMeasureAnswer extends AnswerMessage {

        public StartMeasureAnswer(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time);
        }

        @Override
        public String toString() {
            return START_MEASURE.getDescription();
        }
    }

    @Slf4j
    @Getter
    public static class StopMeasureCommand extends CommandMessage {

        public StopMeasureCommand() {
            super(createId(new AddressInfo(CONTROL, true, ALL_DEVICES, 0)),
                    createDataBytes());
        }

        private static byte[] createDataBytes() {
            byte[] bytes = {
                    STOP_MEASURE.getCode(),
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00};
            log.debug("Data: {}", formatDataAsHex(bytes));
            return bytes;
        }

        @Override
        public String toString() {
            return STOP_MEASURE.getDescription();
        }
    }

    @Slf4j
    @Getter
    public static class StopMeasureAnswer extends AnswerMessage {

        public StopMeasureAnswer(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time);
        }

        @Override
        public String toString() {
            return STOP_MEASURE.getDescription();
        }
    }

    @Slf4j
    @Getter
    public static class AnswerMessage extends SomeMessage {

        private final CommandStatus commandStatus;

        public AnswerMessage(int id, byte[] data, long time) {
            super(id, data, time);
            this.commandStatus = CommandStatus.fromCode(data[1]);
            log.debug("Execution Status: {}", commandStatus);
        }
    }

    public static class CommandMessage extends SomeMessage {
        public CommandMessage(int id, byte[] data) {
            super(id, data, 0);
        }
    }
}
