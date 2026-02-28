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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.unitvectory.cronconductor.common.model.ScheduleEntry;
import com.unitvectory.cronconductor.common.model.ScheduleType;
import com.unitvectory.cronconductor.common.publisher.SchedulePublisher;
import com.unitvectory.cronconductor.common.repository.ScheduleRepository;
import com.unitvectory.cronconductor.common.service.CronConductor;

/**
 * The ScheduleRunner test
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ScheduleRunnerTest {

    private final CronConductor cronConductor = CronConductor.builder().build();

    @Test
    public void processDueCronScheduleTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        InMemorySchedulePublisher publisher = new InMemorySchedulePublisher();
        ScheduleRunner runner = new ScheduleRunner(repo, publisher, cronConductor);

        ScheduleEntry entry = ScheduleEntry.builder()
                .scheduleId("cron-1")
                .namespace("default")
                .timezone("UTC")
                .cron("0 17 * * 2")
                .runAt("2024-03-26T17:00")
                .scheduleType(ScheduleType.CRON)
                .resourceId("user123")
                .scheduleName("WeeklyTask")
                .payload("{\"task\":\"test\"}")
                .build();
        repo.save(entry);

        runner.processDueSchedules("default", "2024-03-26T17:00");

        // Should have published the entry
        assertEquals(1, publisher.published.size());
        assertEquals("cron-1", publisher.published.get(0).getScheduleId());

        // Should have updated runAt to next execution time
        ScheduleEntry updated = repo.findById("cron-1");
        assertNotEquals("2024-03-26T17:00", updated.getRunAt());
        assertEquals("2024-04-02T17:00", updated.getRunAt());
    }

    @Test
    public void processDueOnceScheduleTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        InMemorySchedulePublisher publisher = new InMemorySchedulePublisher();
        ScheduleRunner runner = new ScheduleRunner(repo, publisher, cronConductor);

        ScheduleEntry entry = ScheduleEntry.builder()
                .scheduleId("once-1")
                .namespace("default")
                .timezone("UTC")
                .runAt("2024-12-31T23:59")
                .scheduleType(ScheduleType.ONCE)
                .resourceId("server456")
                .scheduleName("EndOfYear")
                .payload("{\"report\":\"yearly\"}")
                .build();
        repo.save(entry);

        runner.processDueSchedules("default", "2024-12-31T23:59");

        // Should have published the entry
        assertEquals(1, publisher.published.size());
        assertEquals("once-1", publisher.published.get(0).getScheduleId());

        // Should have been deleted
        assertTrue(repo.store.isEmpty());
    }

    @Test
    public void processNoDueSchedulesTest() {
        InMemoryScheduleRepository repo = new InMemoryScheduleRepository();
        InMemorySchedulePublisher publisher = new InMemorySchedulePublisher();
        ScheduleRunner runner = new ScheduleRunner(repo, publisher, cronConductor);

        ScheduleEntry entry = ScheduleEntry.builder()
                .scheduleId("future-1")
                .namespace("default")
                .timezone("UTC")
                .runAt("2030-12-31T23:59")
                .scheduleType(ScheduleType.ONCE)
                .resourceId("user789")
                .scheduleName("FutureTask")
                .payload("{}")
                .build();
        repo.save(entry);

        runner.processDueSchedules("default", "2024-01-01T00:00");

        // Nothing should have been published
        assertTrue(publisher.published.isEmpty());

        // Entry should still exist
        assertEquals(1, repo.store.size());
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

    /**
     * Simple in-memory publisher for testing
     */
    private static class InMemorySchedulePublisher implements SchedulePublisher {

        final List<ScheduleEntry> published = new ArrayList<>();

        @Override
        public void publish(ScheduleEntry entry) {
            published.add(entry);
        }
    }
}
