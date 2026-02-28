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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.unitvectory.cronconductor.common.exception.CronConductorException;
import com.unitvectory.cronconductor.common.model.ScheduleEntry;
import com.unitvectory.cronconductor.common.model.ScheduleType;
import com.unitvectory.cronconductor.common.repository.ScheduleRepository;
import com.unitvectory.cronconductor.common.service.CronConductor;

/**
 * The ScheduleService test
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ScheduleServiceTest {

    private final CronConductor cronConductor = CronConductor.builder().build();

    @Test
    public void createCronScheduleTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        ScheduleService service = new ScheduleService(repo, cronConductor);

        ScheduleEntry entry = ScheduleEntry.builder()
                .namespace("default")
                .timezone("UTC")
                .cron("0 17 * * 2")
                .scheduleType(ScheduleType.CRON)
                .resourceId("user123")
                .scheduleName("WeeklyTask")
                .payload("{\"task\":\"test\"}")
                .build();

        ScheduleEntry result = service.createSchedule(entry);

        assertNotNull(result.getScheduleId());
        assertNotNull(result.getRunAt());
        assertEquals(ScheduleType.CRON, result.getScheduleType());
        assertEquals(1, repo.store.size());
    }

    @Test
    public void createOnceScheduleTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        ScheduleService service = new ScheduleService(repo, cronConductor);

        ScheduleEntry entry = ScheduleEntry.builder()
                .namespace("default")
                .timezone("UTC")
                .runAt("2024-12-31T23:59")
                .scheduleType(ScheduleType.ONCE)
                .resourceId("server456")
                .scheduleName("EndOfYearReport")
                .payload("{\"report\":\"yearly\"}")
                .build();

        ScheduleEntry result = service.createSchedule(entry);

        assertNotNull(result.getScheduleId());
        assertEquals("2024-12-31T23:59", result.getRunAt());
        assertEquals(ScheduleType.ONCE, result.getScheduleType());
    }

    @Test
    public void createScheduleInvalidTimezoneTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        ScheduleService service = new ScheduleService(repo, cronConductor);

        ScheduleEntry entry = ScheduleEntry.builder()
                .namespace("default")
                .timezone("INVALID/TIMEZONE")
                .cron("0 17 * * 2")
                .scheduleType(ScheduleType.CRON)
                .resourceId("user123")
                .scheduleName("Task")
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.createSchedule(entry));
    }

    @Test
    public void createScheduleInvalidCronTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        ScheduleService service = new ScheduleService(repo, cronConductor);

        ScheduleEntry entry = ScheduleEntry.builder()
                .namespace("default")
                .timezone("UTC")
                .cron("invalid")
                .scheduleType(ScheduleType.CRON)
                .resourceId("user123")
                .scheduleName("Task")
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.createSchedule(entry));
    }

    @Test
    public void getScheduleNotFoundTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        ScheduleService service = new ScheduleService(repo, cronConductor);

        assertThrows(CronConductorException.class, () -> service.getSchedule("nonexistent"));
    }

    @Test
    public void getScheduleTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        ScheduleService service = new ScheduleService(repo, cronConductor);

        ScheduleEntry entry = ScheduleEntry.builder()
                .namespace("default")
                .timezone("UTC")
                .runAt("2024-12-31T23:59")
                .scheduleType(ScheduleType.ONCE)
                .resourceId("user123")
                .scheduleName("Task")
                .payload("{}")
                .build();

        ScheduleEntry created = service.createSchedule(entry);
        ScheduleEntry found = service.getSchedule(created.getScheduleId());

        assertEquals(created.getScheduleId(), found.getScheduleId());
    }

    @Test
    public void deleteScheduleTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        ScheduleService service = new ScheduleService(repo, cronConductor);

        ScheduleEntry entry = ScheduleEntry.builder()
                .namespace("default")
                .timezone("UTC")
                .runAt("2024-12-31T23:59")
                .scheduleType(ScheduleType.ONCE)
                .resourceId("user123")
                .scheduleName("Task")
                .payload("{}")
                .build();

        ScheduleEntry created = service.createSchedule(entry);
        service.deleteSchedule(created.getScheduleId());

        assertThrows(CronConductorException.class,
                () -> service.getSchedule(created.getScheduleId()));
    }

    @Test
    public void getSchedulesByResourceTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        ScheduleService service = new ScheduleService(repo, cronConductor);

        ScheduleEntry entry1 = ScheduleEntry.builder()
                .namespace("default")
                .timezone("UTC")
                .runAt("2024-12-31T23:59")
                .scheduleType(ScheduleType.ONCE)
                .resourceId("user123")
                .scheduleName("Task1")
                .payload("{}")
                .build();

        ScheduleEntry entry2 = ScheduleEntry.builder()
                .namespace("default")
                .timezone("UTC")
                .runAt("2025-01-01T00:00")
                .scheduleType(ScheduleType.ONCE)
                .resourceId("user123")
                .scheduleName("Task2")
                .payload("{}")
                .build();

        service.createSchedule(entry1);
        service.createSchedule(entry2);

        List<ScheduleEntry> results = service.getSchedulesByResource("default", "user123");
        assertEquals(2, results.size());
    }

    /**
     * Simple in-memory implementation for testing
     */
    private static class InMemoryScheduleRepository implements ScheduleRepository {

        final Map<String, ScheduleEntry> store = new HashMap<>();

        @Override
        public void save(ScheduleEntry entry) {
            store.put(entry.getScheduleId(), entry);
        }

        @Override
        public ScheduleEntry findById(String scheduleId) {
            return store.get(scheduleId);
        }

        @Override
        public List<ScheduleEntry> findByNamespaceAndResourceId(String namespace,
                String resourceId) {
            List<ScheduleEntry> result = new ArrayList<>();
            for (ScheduleEntry entry : store.values()) {
                if (namespace.equals(entry.getNamespace())
                        && resourceId.equals(entry.getResourceId())) {
                    result.add(entry);
                }
            }
            return result;
        }

        @Override
        public void deleteById(String scheduleId) {
            store.remove(scheduleId);
        }

        @Override
        public List<ScheduleEntry> findDueSchedules(String namespace, String runAtBefore) {
            List<ScheduleEntry> result = new ArrayList<>();
            for (ScheduleEntry entry : store.values()) {
                if (namespace.equals(entry.getNamespace()) && entry.getRunAt() != null
                        && entry.getRunAt().compareTo(runAtBefore) <= 0) {
                    result.add(entry);
                }
            }
            return result;
        }
    }
}
