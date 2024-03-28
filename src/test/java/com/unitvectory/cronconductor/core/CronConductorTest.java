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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * The CronConductor test
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class CronConductorTest {

    private static final CronConductor cronConductor = CronConductor.builder().build();

    @Test
    public void isValidTimezoneTest() {
        assertTrue(CronConductor.isValidTimezone("America/New_York"));
        assertFalse(CronConductor.isValidTimezone("America/New_York/INVALID"));
    }

    @Test
    public void isValidCronExpressionTest() {
        assertTrue(cronConductor.isValidCronExpression("0 17 * * 2"));
        assertTrue(cronConductor.isValidCronExpression("@weekly"));
        assertFalse(cronConductor.isValidCronExpression("invalid"));
    }

    @Test
    public void getNextExecutionTimeTest() {
        assertEquals("2024-04-02T17:00",
                cronConductor.getNextExecutionTime("0 17 * * 2", "UTC", "2024-03-27T04:00"));
        assertEquals("2024-04-09T17:00",
                cronConductor.getNextExecutionTime("0 17 * * 2", "UTC", "2024-04-02T17:00"));
        assertEquals("2025-01-01T00:00",
                cronConductor.getNextExecutionTime("@yearly", "UTC", "2024-04-02T17:00"));
    }
}
