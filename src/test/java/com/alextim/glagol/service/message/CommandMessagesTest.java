package com.alextim.glagol.service.message;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.service.message.CommandMessages.*;
import com.alextim.glagol.service.protocol.CommandStatus;
import com.alextim.glagol.service.protocol.Parameter;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.alextim.glagol.service.protocol.Command.*;
import static org.junit.jupiter.api.Assertions.*;

public class CommandMessagesTest {

    private static final int TEST_ID = 0x123;
    private static final long TEST_TIME = System.currentTimeMillis();

    @Test
    public void testRestartCommand() {
        RestartCommand cmd = new RestartCommand();

        assertEquals(RESTART, cmd.command);
        assertEquals(RESTART.getCode(), cmd.data[0]);
        assertEquals(CommandStatus.SUCCESS.getCode(), cmd.data[1]);
        for (int i = 2; i < cmd.data.length; i++) {
            assertEquals(0, cmd.data[i]);
        }
        assertEquals(RESTART.getDescription(), cmd.toString());
    }

    @Test
    public void testRestartAnswer() {
        byte[] testData = new byte[8];
        testData[0] = RESTART.getCode();
        testData[1] = CommandStatus.SUCCESS.getCode();

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);
        RestartAnswer ans = new RestartAnswer(baseMsg);

        assertEquals(RESTART, ans.command);
        assertEquals(CommandStatus.SUCCESS, ans.commandStatus);
        assertEquals(TEST_ID, ans.id);
        assertEquals(TEST_TIME, ans.time);
        assertEquals(RESTART.getDescription(), ans.toString());
    }

    @Test
    public void testGetParamCommand() {
        Parameter param = Parameter.BD_BG_RANGE1_DEAD_TIME;
        GetParamCommand cmd = new GetParamCommand(param);

        assertEquals(GET_PARAM, cmd.command);
        assertEquals(GET_PARAM.getCode(), cmd.data[0]);
        assertEquals(param.getCode(), cmd.data[1]);
        assertEquals(param, cmd.param);
        for (int i = 2; i < cmd.data.length; i++) {
            assertEquals(0, cmd.data[i]);
        }
        assertTrue(cmd.toString().contains(GET_PARAM.getDescription()));
        assertTrue(cmd.toString().contains(param.getDescription()));
    }

    @Test
    public void testGetParamAnswer_Float() {
        Parameter param = Parameter.BD_BG_RANGE1_DEAD_TIME;
        float expectedValue = 12.34f;
        byte[] valueBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(expectedValue).array();

        byte[] testData = new byte[8];
        testData[0] = GET_PARAM.getCode();
        testData[1] = CommandStatus.SUCCESS.getCode();
        testData[2] = param.getCode();
        System.arraycopy(valueBytes, 0, testData, 4, 4);

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);
        GetParamAnswer ans = new GetParamAnswer(baseMsg);

        assertEquals(GET_PARAM, ans.command);
        assertEquals(CommandStatus.SUCCESS, ans.commandStatus);
        assertEquals(param, ans.param);
        assertEquals(expectedValue, (Float) ans.value, 0.0001f);
        assertTrue(ans.toString().contains(GET_PARAM.getDescription()));
        assertTrue(ans.toString().contains(param.getDescription()));
        assertTrue(ans.toString().contains(Float.toString(expectedValue)));
    }

    @Test
    public void testGetParamAnswer_Int() {
        Parameter param = Parameter.BD_ACCUM_TIME;
        int expectedValue = 100;
        byte[] valueBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(expectedValue).array();

        byte[] testData = new byte[8];
        testData[0] = GET_PARAM.getCode();
        testData[1] = CommandStatus.SUCCESS.getCode();
        testData[2] = param.getCode();
        System.arraycopy(valueBytes, 0, testData, 4, 4);

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);
        GetParamAnswer ans = new GetParamAnswer(baseMsg);

        assertEquals(GET_PARAM, ans.command);
        assertEquals(CommandStatus.SUCCESS, ans.commandStatus);
        assertEquals(param, ans.param);
        assertEquals(expectedValue, (Integer) ans.value);
        assertTrue(ans.toString().contains(GET_PARAM.getDescription()));
        assertTrue(ans.toString().contains(param.getDescription()));
        assertTrue(ans.toString().contains(Integer.toString(expectedValue)));
    }

    @Test
    public void testSetParamCommand_Float() {
        Parameter param = Parameter.BD_BG_RANGE1_DEAD_TIME;
        float value = 56.78f;
        SetParamCommand cmd = new SetParamCommand(param, value);

        assertEquals(SET_PARAM, cmd.command);
        assertEquals(SET_PARAM.getCode(), cmd.data[0]);
        assertEquals(param.getCode(), cmd.data[1]);
        assertEquals(param, cmd.param);
        assertEquals(value, cmd.value);

        byte[] valueBytes = new byte[4];
        System.arraycopy(cmd.data, 4, valueBytes, 0, 4);
        float readValue = ByteBuffer.wrap(valueBytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        assertEquals(value, readValue, 0.0001f);

        assertTrue(cmd.toString().contains(SET_PARAM.getDescription()));
        assertTrue(cmd.toString().contains(param.getDescription()));
        assertTrue(cmd.toString().contains(Float.toString(value)));
    }

    @Test
    public void testSetParamCommand_Int() {
        Parameter param = Parameter.BD_BG_CURRENT_RANGE;
        int value = 200;
        SetParamCommand cmd = new SetParamCommand(param, value);

        assertEquals(SET_PARAM, cmd.command);
        assertEquals(SET_PARAM.getCode(), cmd.data[0]);
        assertEquals(param.getCode(), cmd.data[1]);
        assertEquals(param, cmd.param);
        assertEquals(value, cmd.value);

        byte[] valueBytes = new byte[4];
        System.arraycopy(cmd.data, 4, valueBytes, 0, 4);
        int readValue = ByteBuffer.wrap(valueBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        assertEquals(value, readValue);

        assertTrue(cmd.toString().contains(SET_PARAM.getDescription()));
        assertTrue(cmd.toString().contains(param.getDescription()));
        assertTrue(cmd.toString().contains(Integer.toString(value)));
    }

    @Test
    public void testSetParamAnswer_Float() {
        Parameter param = Parameter.BD_BG_RANGE1_DEAD_TIME;
        float expectedValue = 99.11f;
        byte[] valueBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(expectedValue).array();

        byte[] testData = new byte[8];
        testData[0] = SET_PARAM.getCode();
        testData[1] = CommandStatus.SUCCESS.getCode();
        testData[2] = param.getCode();
        System.arraycopy(valueBytes, 0, testData, 4, 4);

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);
        SetParamAnswer ans = new SetParamAnswer(baseMsg);

        assertEquals(SET_PARAM, ans.command);
        assertEquals(CommandStatus.SUCCESS, ans.commandStatus);
        assertEquals(param, ans.param);
        assertEquals(expectedValue, (Float) ans.value, 0.0001f);
        assertTrue(ans.toString().contains(SET_PARAM.getDescription()));
        assertTrue(ans.toString().contains(param.getDescription()));
        assertTrue(ans.toString().contains(Float.toString(expectedValue)));
    }

    @Test
    public void testSetParamAnswer_Int() {
        Parameter param = Parameter.BD_BG_CURRENT_RANGE;
        int expectedValue = 300;
        byte[] valueBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(expectedValue).array();

        byte[] testData = new byte[8];
        testData[0] = SET_PARAM.getCode();
        testData[1] = CommandStatus.SUCCESS.getCode();
        testData[2] = param.getCode();
        System.arraycopy(valueBytes, 0, testData, 4, 4);

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);
        SetParamAnswer ans = new SetParamAnswer(baseMsg);

        assertEquals(SET_PARAM, ans.command);
        assertEquals(CommandStatus.SUCCESS, ans.commandStatus);
        assertEquals(param, ans.param);
        assertEquals(expectedValue, (Integer) ans.value);
        assertTrue(ans.toString().contains(SET_PARAM.getDescription()));
        assertTrue(ans.toString().contains(param.getDescription()));
        assertTrue(ans.toString().contains(Integer.toString(expectedValue)));
    }

    @Test
    public void testStartMeasureCommand() {
        StartMeasureCommand cmd = new StartMeasureCommand();

        assertEquals(START_MEASURE, cmd.command);
        assertEquals(START_MEASURE.getCode(), cmd.data[0]);
        for (int i = 1; i < cmd.data.length; i++) {
            assertEquals(0, cmd.data[i]);
        }
        assertEquals(START_MEASURE.getDescription(), cmd.toString());
    }

    @Test
    public void testStartMeasureAnswer() {
        byte[] testData = new byte[8];
        testData[0] = START_MEASURE.getCode();
        testData[1] = CommandStatus.SUCCESS.getCode();

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);
        StartMeasureAnswer ans = new StartMeasureAnswer(baseMsg);

        assertEquals(START_MEASURE, ans.command);
        assertEquals(CommandStatus.SUCCESS, ans.commandStatus);
        assertEquals(TEST_ID, ans.id);
        assertEquals(TEST_TIME, ans.time);
        assertEquals(START_MEASURE.getDescription(), ans.toString());
    }

    @Test
    public void testStopMeasureCommand() {
        StopMeasureCommand cmd = new StopMeasureCommand();

        assertEquals(STOP_MEASURE, cmd.command);
        assertEquals(STOP_MEASURE.getCode(), cmd.data[0]);
        for (int i = 1; i < cmd.data.length; i++) {
            assertEquals(0, cmd.data[i]);
        }
        assertEquals(STOP_MEASURE.getDescription(), cmd.toString());
    }

    @Test
    public void testStopMeasureAnswer() {
        byte[] testData = new byte[8];
        testData[0] = STOP_MEASURE.getCode();
        testData[1] = CommandStatus.SUCCESS.getCode();

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);
        StopMeasureAnswer ans = new StopMeasureAnswer(baseMsg);

        assertEquals(STOP_MEASURE, ans.command);
        assertEquals(CommandStatus.SUCCESS, ans.commandStatus);
        assertEquals(TEST_ID, ans.id);
        assertEquals(TEST_TIME, ans.time);
        assertEquals(STOP_MEASURE.getDescription(), ans.toString());
    }
}