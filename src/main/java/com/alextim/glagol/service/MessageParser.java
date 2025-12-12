package com.alextim.glagol.service;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.service.message.AlarmMessages.Alarm;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementHeader;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementCounts;
import com.alextim.glagol.service.message.ProcessingMessages.ProcessingErrorMessage;
import com.alextim.glagol.service.message.ProcessingMessages.UnknownMessage;
import com.alextim.glagol.service.protocol.AddressInfo;
import com.alextim.glagol.service.protocol.Command;
import com.alextim.glagol.service.protocol.MeasurementMessageType;
import lombok.extern.slf4j.Slf4j;

import static com.alextim.glagol.client.SomeMessage.formatDataAsHex;
import static com.alextim.glagol.service.message.CommandMessages.*;
import static com.alextim.glagol.service.message.MeasurementMessages.*;
import static com.alextim.glagol.service.protocol.AddressInfo.parseAddress;

@Slf4j
public class MessageParser {

    private static int measMsgCounter;

    public static SomeMessage parse(SomeMessage someMessage) {
        try {
            log.debug(String.format("Parsing msg. ID=0x%04X, Data=[%s]", someMessage.id, formatDataAsHex(someMessage.data)));

            AddressInfo address = parseAddress(someMessage.id);
            log.debug("Address: {}", address);

            return switch (address.getCategory()) {
                case ALARM -> parseAlarmMessage(someMessage);
                case RESPONSE -> parseResponseMessage(someMessage);
                case MEASUREMENT -> parseMeasurementMessage(someMessage);
                default -> new UnknownMessage(someMessage);
            };
        } catch (Exception e) {
            return new ProcessingErrorMessage(someMessage, e.getMessage());
        }
    }

    private static SomeMessage parseAlarmMessage(SomeMessage originalMsg) {
        return new Alarm(originalMsg);
    }

    private static SomeMessage parseResponseMessage(SomeMessage originalMsg) {
        Command cmd = Command.fromCode(originalMsg.data[0]);
        log.debug("Response command: {}", cmd);

        return switch (cmd) {
            case RESTART -> new RestartAnswer(originalMsg);
            case START_MEASURE -> {
                measMsgCounter = 0;
                yield new StartMeasureAnswer(originalMsg);
            }
            case STOP_MEASURE -> new StopMeasureAnswer(originalMsg);
            case GET_PARAM -> new GetParamAnswer(originalMsg);
            case SET_PARAM -> new SetParamAnswer(originalMsg);
        };
    }


    private static SomeMessage parseMeasurementMessage(SomeMessage originalMsg) {
        MeasurementMessageType messageType = MeasurementMessageType.fromSequenceNumber(++measMsgCounter);
        return switch (messageType) {
            case HEADER -> new MeasurementHeader(originalMsg);
            case COUNTS -> new MeasurementCounts(originalMsg);
            case DOSE_RATE -> new MeasurementDoseRate(originalMsg);
        };
    }
}