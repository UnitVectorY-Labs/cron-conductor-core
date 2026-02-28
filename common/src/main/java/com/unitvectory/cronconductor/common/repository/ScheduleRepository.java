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
package com.unitvectory.cronconductor.common.repository;

import java.util.List;

import com.unitvectory.cronconductor.common.model.ScheduleEntry;

/**
 * Interface for schedule persistence operations.
 * 
 * Implementations provide the database-specific logic for storing and retrieving schedule entries.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public interface ScheduleRepository {

    /**
     * Saves a schedule entry. If an entry with the same scheduleId exists, it is updated.
     * 
     * @param entry the schedule entry to save
     */
    void save(ScheduleEntry entry);

    /**
     * Finds a schedule entry by its unique identifier.
     * 
     * @param scheduleId the schedule identifier
     * @return the schedule entry, or null if not found
     */
    ScheduleEntry findById(String scheduleId);

    /**
     * Finds all schedule entries for a given namespace and resource identifier.
     * 
     * @param namespace the namespace
     * @param resourceId the resource identifier
     * @return list of matching schedule entries
     */
    List<ScheduleEntry> findByNamespaceAndResourceId(String namespace, String resourceId);

    /**
     * Deletes a schedule entry by its unique identifier.
     * 
     * @param scheduleId the schedule identifier
     */
    void deleteById(String scheduleId);

    /**
     * Finds all schedule entries that are due for execution, meaning their runAt time is at or
     * before the specified time.
     * 
     * @param namespace the namespace
     * @param runAtBefore the cutoff time in 'yyyy-MM-dd'T'HH:mm' format
     * @return list of due schedule entries
     */
    List<ScheduleEntry> findDueSchedules(String namespace, String runAtBefore);
}
