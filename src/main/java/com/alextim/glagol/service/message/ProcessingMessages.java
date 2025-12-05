package com.alextim.glagol.service.message;

import com.alextim.glagol.client.SomeMessage;

public class ProcessingMessages {

    public static class UnknownMessage extends SomeMessage {

        public UnknownMessage(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time);
        }

        @Override
        public String toString() {
            return "Неизвестное сообщение";
        }
    }

    public static class ProcessingErrorMessage extends SomeMessage {
        public final String errorMessage;
        public final SomeMessage originalMessage;

        public ProcessingErrorMessage(SomeMessage originalMsg, String errorMessage) {
            super(originalMsg.id, originalMsg.data, originalMsg.time);
            this.originalMessage = originalMsg;
            this.errorMessage = errorMessage != null ? errorMessage : "Unknown error";
        }

        @Override
        public String toString() {
            return String.format("Ошибка во время обработки сообщения: '%s'}", errorMessage);
        }
    }
}
