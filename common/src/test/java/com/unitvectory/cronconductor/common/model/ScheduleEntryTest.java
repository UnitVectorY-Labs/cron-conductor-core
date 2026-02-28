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
package com.unitvectory.cronconductor.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 * The ScheduleEntry test
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ScheduleEntryTest {

    @Test
    public void builderTest() {
        ScheduleEntry entry = ScheduleEntry.builder()
                .scheduleId("test-id")
                .namespace("default")
                .timezone("UTC")
                .cron("0 17 * * 2")
                .scheduleType(ScheduleType.CRON)
                .resourceId("user123")
                .scheduleName("WeeklyBackup")
                .payload("{\"task\":\"backup\"}")
                .build();

        assertEquals("test-id", entry.getScheduleId());
        assertEquals("default", entry.getNamespace());
        assertEquals("UTC", entry.getTimezone());
        assertEquals("0 17 * * 2", entry.getCron());
        assertEquals(ScheduleType.CRON, entry.getScheduleType());
        assertEquals("user123", entry.getResourceId());
        assertEquals("WeeklyBackup", entry.getScheduleName());
        assertEquals("{\"task\":\"backup\"}", entry.getPayload());
        assertNull(entry.getRunAt());
    }

    @Test
    public void onceScheduleTest() {
        ScheduleEntry entry = ScheduleEntry.builder()
                .scheduleId("once-id")
                .namespace("default")
                .timezone("UTC")
                .runAt("2024-12-31T23:59")
                .scheduleType(ScheduleType.ONCE)
                .resourceId("server456")
                .scheduleName("EndOfYearReport")
                .payload("{\"report\":\"yearly\"}")
                .build();

        assertEquals(ScheduleType.ONCE, entry.getScheduleType());
        assertEquals("2024-12-31T23:59", entry.getRunAt());
        assertNull(entry.getCron());
    }
}
