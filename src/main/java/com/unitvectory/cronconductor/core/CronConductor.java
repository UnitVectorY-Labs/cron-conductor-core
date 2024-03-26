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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * The CronConductor
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class CronConductor {

    /**
     * Set of all of the timezones supported by Java
     */
    private static final Set<String> ALL_TIMEZONES = Collections
            .unmodifiableSet(new TreeSet<String>(Arrays.asList(TimeZone.getAvailableIDs())));

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
