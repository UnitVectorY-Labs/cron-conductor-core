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
package com.unitvectory.cronconductor.core;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.Builder;
import lombok.NonNull;

/**
 * The CronConductor
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class CronConductor {

    /**
     * The date format for the cron
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";

    /**
     * Set of all of the timezones supported by Java
     */
    private static final Set<String> ALL_TIMEZONES = Collections
            .unmodifiableSet(new TreeSet<String>(Arrays.asList(TimeZone.getAvailableIDs())));

    /**
     * The cron definition
     */
    private final CronDefinition cronDefinition;

    /**
     * The cron parser
     */
    private final CronParser cronParser;

    /**
     * Creates a new instance of the CronCondutor class
     * 
     * @param cronDefinition the cron definition
     */
    @Builder
    private CronConductor(CronDefinition cronDefinition) {
        if (cronDefinition == null) {
            this.cronDefinition = CronDefinitionBuilder.defineCron().withSupportedNicknameAnnually()
                    .withSupportedNicknameDaily().withSupportedNicknameHourly()
                    .withSupportedNicknameMidnight().withSupportedNicknameMonthly()
                    .withSupportedNicknameWeekly().withSupportedNicknameYearly().withMinutes().and()
                    .withHours().and().withDayOfMonth().and().withMonth().and().withDayOfWeek()
                    .and().instance();
        } else {
            this.cronDefinition = cronDefinition;
        }

        this.cronParser = new CronParser(this.cronDefinition);
    }

    /**
     * Tests if the provided cron expiression is valid
     * 
     * @param cronExpression the cron expiression
     * @return true if valid; otherwise false
     */
    public boolean isValidCronExpression(@NonNull String cronExpression) {
        try {
            this.cronParser.parse(cronExpression);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets the next execution time based on the last time
     * 
     * @param cronExpression the cron expression
     * @param timezone the timezone
     * @param lastRunAt the last time the cron was run; used to determine the next execution time
     * @return the next execution time
     */
    public String getNextExecutionTime(@NonNull String cronExpression, @NonNull String timezone,
            @NonNull String lastRunAt) {
        // Parse the cron expression
        Cron cron;
        try {
            cron = this.cronParser.parse(cronExpression);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }

        // Ensure the timezone is valid
        if (!isValidTimezone(timezone)) {
            throw new IllegalArgumentException("Invalid timezone: " + timezone);
        }

        // Define the formatter for parsing and formatting dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(TimeZone.getTimeZone(timezone).toZoneId());

        // Parse the lastRunAt into a ZonedDateTime
        ZonedDateTime previous;
        try {
            previous = ZonedDateTime.parse(lastRunAt, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid lastRunAt: " + lastRunAt);
        }

        // Create an ExecutionTime object for the cron expression
        ExecutionTime executionTime = ExecutionTime.forCron(cron);


        // Calculate the next execution time
        Optional<ZonedDateTime> nextOptional = executionTime.nextExecution(previous);
        if (!nextOptional.isPresent()) {
            throw new CronConductorException(
                    "No next execution time found for cron expression: " + cronExpression);
        }

        ZonedDateTime next = nextOptional.get();

        // Format and return the next execution time as String
        return formatter.format(next);
    }

    /**
     * Tests if the specified timezone name is valid.
     * 
     * @param timezone the timezone
     * @return true if valid; otherwise false
     */
    public static boolean isValidTimezone(String timezone) {
        return ALL_TIMEZONES.contains(timezone);
    }
}
