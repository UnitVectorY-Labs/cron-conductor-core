/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.unitvectory.cronconductor.runner;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

import com.unitvectory.cronconductor.common.model.ScheduleEntry;
import com.unitvectory.cronconductor.common.model.ScheduleType;
import com.unitvectory.cronconductor.common.publisher.SchedulePublisher;
import com.unitvectory.cronconductor.common.repository.ScheduleRepository;
import com.unitvectory.cronconductor.common.service.CronConductor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Runner component responsible for executing due cron jobs.
 * 
 * Queries the schedule repository for entries that are due, publishes execution events, and updates
 * or removes schedule entries based on their type. For CRON schedules, the next execution time is
 * computed and the entry is updated. For ONCE schedules, the entry is deleted after execution.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@RequiredArgsConstructor
public class ScheduleRunner {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";

    @NonNull
    private final ScheduleRepository scheduleRepository;

    @NonNull
    private final SchedulePublisher schedulePublisher;

    @NonNull
    private final CronConductor cronConductor;

    /**
     * Processes all due schedules for the given namespace.
     * 
     * Finds all schedule entries whose runAt time is at or before the current time, publishes each
     * one, and then either updates the next run time (CRON) or deletes the entry (ONCE).
     * 
     * @param namespace the namespace to process
     */
    public void processDueSchedules(@NonNull String namespace) {
        String now = ZonedDateTime.now(TimeZone.getTimeZone("UTC").toZoneId())
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        processDueSchedules(namespace, now);
    }

    /**
     * Processes all due schedules for the given namespace up to the specified time.
     * 
     * @param namespace the namespace to process
     * @param runAtBefore the cutoff time in 'yyyy-MM-dd'T'HH:mm' format
     */
    public void processDueSchedules(@NonNull String namespace, @NonNull String runAtBefore) {
        List<ScheduleEntry> dueEntries =
                scheduleRepository.findDueSchedules(namespace, runAtBefore);

        for (ScheduleEntry entry : dueEntries) {
            // Publish the execution event
            schedulePublisher.publish(entry);

            if (entry.getScheduleType() == ScheduleType.CRON) {
                // Compute the next execution time and update the entry
                String nextRunAt = cronConductor.getNextExecutionTime(entry.getCron(),
                        entry.getTimezone(), entry.getRunAt());
                entry.setRunAt(nextRunAt);
                scheduleRepository.save(entry);
            } else if (entry.getScheduleType() == ScheduleType.ONCE) {
                // One-time schedules are removed after execution
                scheduleRepository.deleteById(entry.getScheduleId());
            }
        }
    }
}
