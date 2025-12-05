package com.alextim.glagol.service;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.service.message.AlarmMessages.RestartAlarm;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementHeader;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementCounts;
import com.alextim.glagol.service.message.ProcessingMessages.ProcessingErrorMessage;
import com.alextim.glagol.service.message.ProcessingMessages.UnknownMessage;
import com.alextim.glagol.service.protocol.AddressInfo;
import com.alextim.glagol.service.protocol.Command;
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
        Command cmd = Command.fromCode(originalMsg.data[0]);
        log.debug("Alarm command: {}", cmd);

        if (cmd == Command.RESTART) {
            return new RestartAlarm(originalMsg);
        }
        return new UnknownMessage(originalMsg);
    }

    private static SomeMessage parseResponseMessage(SomeMessage originalMsg) {
        Command cmd = Command.fromCode(originalMsg.data[0]);
        log.debug("Response command: {}", cmd);

        return switch (cmd) {
            case START_MEASURE -> {
                measMsgCounter = 0;
                yield new StartMeasureAnswer(originalMsg);
            }
            case STOP_MEASURE -> new StopMeasureAnswer(originalMsg);
            case GET_PARAM -> new GetParamAnswer(originalMsg);
            case SET_PARAM -> new SetParamAnswer(originalMsg);
            default -> new UnknownMessage(originalMsg);
        };
    }

    private static SomeMessage parseMeasurementMessage(SomeMessage originalMsg) {
        return switch (++measMsgCounter) {
            case 1 -> new MeasurementHeader(originalMsg);
            case 2 -> new MeasurementCounts(originalMsg);
            case 3 -> new MeasurementDoseRate(originalMsg);
            default -> new UnknownMessage(originalMsg);
        };
    }
}