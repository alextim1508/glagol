package com.alextim.glagol.service.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Parameter {
    // Общие параметры устройств (из основного протокола)
    SERIAL_NUMBER((byte) 0x00, 4, "Серийный номер", true, false),
    DEVICE_TYPE((byte) 0x01, 2, "Фактический тип устройства", true, false),
    FIRMWARE_VERSION((byte) 0x02, 2, "Версия ПО устройства", true, false),
    WORK_TIME((byte) 0x03, 4, "Отработанное время (сек)", true, false),
    PROTOCOL_VERSION((byte) 0x04, 2, "Реализованная версия общей части протокола обмена", true, false),
    PROTOCOL_EXT_VERSION((byte) 0x05, 2, "Реализованная версия расширения протокола обмена", true, false),

    // Параметры БД (из основного протокола)
    BD_ACCUM_TIME((byte) 0x40, 2, "Время накопления (дс)", false, false),
    BD_SYNC_TYPE((byte) 0x41, 1, "Тип синхронизации", false, false),
    BD_ACCUMULATED_DOSE((byte) 0x42, 4, "Накопленная доза (Гр)", true, true),

    // Параметры БДБГ (из расширенного протокола)
    BD_BG_POWER_PERIOD((byte) 0x80, 2, "Период импульсного питания (мкс)", false, false),
    BD_BG_POWER_DURATION((byte) 0x81, 2, "Длительность импульса питания (мкс)", false, false),
    BD_BG_WORK_MODE((byte) 0x83, 1, "Вариант работы в режиме измерения", false, false),
    BD_BG_CURRENT_RANGE((byte) 0x90, 1, "Номер текущего диапазона", false, false),

    BD_BG_RANGE1_SIZE((byte) 0x91, 1, "Размер движка в 1 диапазоне", false, false),
    BD_BG_RANGE2_SIZE((byte) 0x92, 1, "Размер движка в 2 диапазоне", false, false),
    BD_BG_RANGE3_SIZE((byte) 0x93, 1, "Размер движка в 3 диапазоне", false, false),

    BD_BG_RANGE1_DEAD_TIME((byte) 0x94, 4, "Мертвое время 1 диапазона", false, true),
    BD_BG_RANGE2_DEAD_TIME((byte) 0x95, 4, "Мертвое время 2 диапазона", false, true),
    BD_BG_RANGE3_DEAD_TIME((byte) 0x96, 4, "Мертвое время 3 диапазона", false, true),

    BD_BG_THRESHOLD_1_TO_2((byte) 0x97, 2, "Порог перехода из 1 диапазона в 2", false, false),
    BD_BG_THRESHOLD_2_TO_3((byte) 0x98, 2, "Порог перехода из 2 диапазона в 3", false, false),
    BD_BG_THRESHOLD_3_MAX((byte) 0x99, 2, "Порог перехода из 3 (максимальная скорость счета 3 диапазона)", false, false),
    BD_BG_THRESHOLD_3_TO_2((byte) 0x9A, 2, "Порог перехода из 3 диапазона в 2", false, false),
    BD_BG_THRESHOLD_2_TO_1((byte) 0x9B, 2, "Порог перехода из 2 диапазона в 1", false, false),

    BD_BG_SI29G_COEFF_1((byte) 0xA1, 4, "СИ29Г. Поправочный коэффициент счётчика 1", false, true),
    BD_BG_SI29G_COEFF_2((byte) 0xA2, 4, "СИ29Г. Поправочный коэффициент счётчика 2", false, true),
    BD_BG_SI29G_COEFF_3((byte) 0xA3, 4, "СИ29Г. Поправочный коэффициент счётчика 3", false, true),
    BD_BG_SI29G_COEFF_4((byte) 0xA4, 4, "СИ29Г. Поправочный коэффициент счётчика 4", false, true),
    BD_BG_SBM21_COEFF_1((byte) 0xB1, 4, "СБМ21. Поправочный коэффициент счётчика 1", false, true),
    BD_BG_SBM21_COEFF_2((byte) 0xB2, 4, "СБМ21. Поправочный коэффициент счётчика 2", false, true),
    BD_BG_SI38G_COEFF_1((byte) 0xC1, 4, "СИ38Г. Поправочный коэффициент счётчика 1", false, true),
    BD_BG_SI38G_COEFF_2((byte) 0xC2, 4, "СИ38Г. Поправочный коэффициент счётчика 2", false, true);

    private final byte code;
    private final int length;
    private final String description;
    private final boolean readOnly;
    private final boolean isFloat;

    public static Parameter fromCode(byte code) {
        for (Parameter param : Parameter.values()) {
            if (param.getCode() == code) {
                return param;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown parameter code: 0x%02X", code));
    }
}