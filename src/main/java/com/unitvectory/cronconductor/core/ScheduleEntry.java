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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Schedule Entry
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntry {

    /**
     * The UUID for the schedule
     */
    private String scheduleId;

    /**
     * The namespace divides schdules into independent collections
     */
    private String namespace;

    /**
     * The timezone the schedule
     */
    private String timezone;

    /**
     * THe cron for the recurring schedule
     */
    private String cron;

    /**
     * The time the schedule will run at
     * 
     * For once schedules this is when the schedule will execute.
     * 
     * For cron schedules this is automatically populated with the next time it will run.
     */
    private String runAt;

    /**
     * The schedule type
     */
    private ScheduleType scheduleType;

    /**
     * The resourceId is the high cardinality identifier for a schedule such as a user
     */
    private String resourceId;

    /**
     * The scheduleName is the low cardinality identifier that belongs to a resourceId
     */
    private String scheduleName;

    /**
     * The JSON payload for the schedule encoded as a string.
     */
    private String payload;

}
