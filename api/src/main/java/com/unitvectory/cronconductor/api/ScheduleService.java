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
package com.unitvectory.cronconductor.api;

import java.util.List;
import java.util.UUID;

import com.unitvectory.cronconductor.common.exception.CronConductorException;
import com.unitvectory.cronconductor.common.model.ScheduleEntry;
import com.unitvectory.cronconductor.common.model.ScheduleType;
import com.unitvectory.cronconductor.common.repository.ScheduleRepository;
import com.unitvectory.cronconductor.common.service.CronConductor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Service layer for managing schedule entries through the API.
 * 
 * Provides operations for creating, retrieving, and deleting cron job schedules. This service
 * validates inputs and computes next execution times for CRON type schedules.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@RequiredArgsConstructor
public class ScheduleService {

    @NonNull
    private final ScheduleRepository scheduleRepository;

    @NonNull
    private final CronConductor cronConductor;

    /**
     * Creates a new schedule entry.
     * 
     * For CRON type schedules, validates the cron expression and computes the initial runAt time.
     * For ONCE type schedules, validates that runAt is provided.
     * 
     * @param entry the schedule entry to create
     * @return the created schedule entry with generated scheduleId and computed runAt
     */
    public ScheduleEntry createSchedule(@NonNull ScheduleEntry entry) {
        // Generate a schedule ID if not provided
        if (entry.getScheduleId() == null || entry.getScheduleId().isBlank()) {
            entry.setScheduleId(UUID.randomUUID().toString());
        }

        // Validate timezone
        if (!CronConductor.isValidTimezone(entry.getTimezone())) {
            throw new IllegalArgumentException("Invalid timezone: " + entry.getTimezone());
        }

        if (entry.getScheduleType() == ScheduleType.CRON) {
            // Validate cron expression
            if (entry.getCron() == null || !cronConductor.isValidCronExpression(entry.getCron())) {
                throw new IllegalArgumentException("Invalid cron expression: " + entry.getCron());
            }

            // Compute initial runAt from current time
            String now = java.time.ZonedDateTime.now(
                    java.util.TimeZone.getTimeZone(entry.getTimezone()).toZoneId())
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            entry.setRunAt(
                    cronConductor.getNextExecutionTime(entry.getCron(), entry.getTimezone(), now));
        } else if (entry.getScheduleType() == ScheduleType.ONCE) {
            if (entry.getRunAt() == null || entry.getRunAt().isBlank()) {
                throw new IllegalArgumentException("runAt is required for ONCE schedule type");
            }
        } else {
            throw new IllegalArgumentException("scheduleType is required");
        }

        scheduleRepository.save(entry);
        return entry;
    }

    /**
     * Retrieves a schedule entry by its unique identifier.
     * 
     * @param scheduleId the schedule identifier
     * @return the schedule entry
     * @throws CronConductorException if the schedule is not found
     */
    public ScheduleEntry getSchedule(@NonNull String scheduleId) {
        ScheduleEntry entry = scheduleRepository.findById(scheduleId);
        if (entry == null) {
            throw new CronConductorException("Schedule not found: " + scheduleId);
        }
        return entry;
    }

    /**
     * Retrieves all schedule entries for a given namespace and resource identifier.
     * 
     * @param namespace the namespace
     * @param resourceId the resource identifier
     * @return list of matching schedule entries
     */
    public List<ScheduleEntry> getSchedulesByResource(@NonNull String namespace,
            @NonNull String resourceId) {
        return scheduleRepository.findByNamespaceAndResourceId(namespace, resourceId);
    }

    /**
     * Deletes a schedule entry by its unique identifier.
     * 
     * @param scheduleId the schedule identifier
     */
    public void deleteSchedule(@NonNull String scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }
}
