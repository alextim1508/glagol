package com.alextim.glagol.service;

import com.alextim.glagol.service.StatisticMeasService.StatisticMeasurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class ExportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public void exportMeasurements(ConcurrentLinkedQueue<StatisticMeasurement> measurements, String fileComment, File file, BiConsumer<Integer, Double> progressCallback) {
        log.info("Starting export of measurements to file: {}", file.getAbsolutePath());

        int initialSize = measurements.size();
        log.debug("Initial queue size for export: {}", initialSize);

        if (initialSize == 0) {
            log.warn("Queue is empty, nothing to export.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("# Комментарий: ");
            writer.write(fileComment != null ? fileComment : "Экспортированные измерения");
            writer.newLine();
            writer.write("# Формат: index;timestamp_ms;formatted_timestamp;current_dose_rate;average_dose_rate;current_counts[0-4];accumulated_counts[0-4];progress");
            writer.newLine();

            Iterator<StatisticMeasurement> iterator = measurements.iterator();
            int processedCount = 0;

            while (iterator.hasNext() && processedCount < initialSize) {
                StatisticMeasurement meas = iterator.next();
                if (meas == null) {
                    log.warn("Null element encountered in queue during export, skipping.");
                    continue;
                }

                writer.write(String.format("%d;%d;%s;%.6f;%.6f;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%.1f",
                        processedCount,
                        meas.localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        meas.localDateTime.format(DATE_TIME_FORMATTER),
                        meas.currentDoseRate,
                        meas.averageDoseRate,
                        meas.currentCounts[0], meas.currentCounts[1], meas.currentCounts[2], meas.currentCounts[3], meas.currentCounts[4],
                        meas.accumulatedCounts[0], meas.accumulatedCounts[1], meas.accumulatedCounts[2], meas.accumulatedCounts[3], meas.accumulatedCounts[4],
                        100*meas.progress
                ));
                writer.newLine();

                processedCount++;
                double progress = (double) processedCount / initialSize;
                progressCallback.accept(processedCount, progress);
            }

            log.info("Export completed successfully to file: {}. Processed {} out of initial {} elements.", file.getAbsolutePath(), processedCount, initialSize);
        } catch (IOException e) {
            log.error("Error during export to file: {}", file.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }
}