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
package com.unitvectory.cronconductor.common.publisher;

import com.unitvectory.cronconductor.common.model.ScheduleEntry;

/**
 * Interface for publishing schedule execution events.
 * 
 * Implementations provide the messaging-specific logic for notifying downstream systems that a
 * schedule is due for execution.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public interface SchedulePublisher {

    /**
     * Publishes a schedule execution event indicating that the given schedule entry is due.
     * 
     * @param entry the schedule entry that is due for execution
     */
    void publish(ScheduleEntry entry);
}
