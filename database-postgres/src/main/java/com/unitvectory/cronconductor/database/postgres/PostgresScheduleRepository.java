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
package com.unitvectory.cronconductor.database.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import com.unitvectory.cronconductor.common.exception.CronConductorException;
import com.unitvectory.cronconductor.common.model.ScheduleEntry;
import com.unitvectory.cronconductor.common.model.ScheduleType;
import com.unitvectory.cronconductor.common.repository.ScheduleRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL implementation of the {@link ScheduleRepository} interface.
 * 
 * Uses JDBC with a provided {@link DataSource} for database operations.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@RequiredArgsConstructor
public class PostgresScheduleRepository implements ScheduleRepository {

    /**
     * The SQL table name for schedule entries
     */
    private static final String TABLE_NAME = "schedule_entries";

    @NonNull
    private final DataSource dataSource;

    @Override
    public void save(ScheduleEntry entry) {
        String sql = "INSERT INTO " + TABLE_NAME
                + " (schedule_id, namespace, timezone, cron, run_at, schedule_type, resource_id, schedule_name, payload)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                + " ON CONFLICT (schedule_id) DO UPDATE SET"
                + " namespace = EXCLUDED.namespace,"
                + " timezone = EXCLUDED.timezone,"
                + " cron = EXCLUDED.cron,"
                + " run_at = EXCLUDED.run_at,"
                + " schedule_type = EXCLUDED.schedule_type,"
                + " resource_id = EXCLUDED.resource_id,"
                + " schedule_name = EXCLUDED.schedule_name,"
                + " payload = EXCLUDED.payload";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entry.getScheduleId());
            stmt.setString(2, entry.getNamespace());
            stmt.setString(3, entry.getTimezone());
            stmt.setString(4, entry.getCron());
            stmt.setString(5, entry.getRunAt());
            stmt.setString(6, entry.getScheduleType() != null ? entry.getScheduleType().name() : null);
            stmt.setString(7, entry.getResourceId());
            stmt.setString(8, entry.getScheduleName());
            stmt.setString(9, entry.getPayload());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CronConductorException("Failed to save schedule entry", e);
        }
    }

    @Override
    public ScheduleEntry findById(String scheduleId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE schedule_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, scheduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new CronConductorException("Failed to find schedule entry by id", e);
        }
    }

    @Override
    public List<ScheduleEntry> findByNamespaceAndResourceId(String namespace, String resourceId) {
        String sql =
                "SELECT * FROM " + TABLE_NAME + " WHERE namespace = ? AND resource_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, namespace);
            stmt.setString(2, resourceId);
            return mapRows(stmt);
        } catch (SQLException e) {
            throw new CronConductorException("Failed to find schedule entries by resource", e);
        }
    }

    @Override
    public void deleteById(String scheduleId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE schedule_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, scheduleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CronConductorException("Failed to delete schedule entry", e);
        }
    }

    @Override
    public List<ScheduleEntry> findDueSchedules(String namespace, String runAtBefore) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE namespace = ? AND run_at <= ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, namespace);
            stmt.setString(2, runAtBefore);
            return mapRows(stmt);
        } catch (SQLException e) {
            throw new CronConductorException("Failed to find due schedule entries", e);
        }
    }

    /**
     * Maps all rows from the result set of a prepared statement to a list of schedule entries.
     * 
     * @param stmt the prepared statement
     * @return list of schedule entries
     * @throws SQLException if a database access error occurs
     */
    private List<ScheduleEntry> mapRows(PreparedStatement stmt) throws SQLException {
        List<ScheduleEntry> entries = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                entries.add(mapRow(rs));
            }
        }
        return entries;
    }

    /**
     * Maps a single result set row to a ScheduleEntry.
     * 
     * @param rs the result set positioned at the current row
     * @return the schedule entry
     * @throws SQLException if a database access error occurs
     */
    private ScheduleEntry mapRow(ResultSet rs) throws SQLException {
        return ScheduleEntry.builder()
                .scheduleId(rs.getString("schedule_id"))
                .namespace(rs.getString("namespace"))
                .timezone(rs.getString("timezone"))
                .cron(rs.getString("cron"))
                .runAt(rs.getString("run_at"))
                .scheduleType(rs.getString("schedule_type") != null
                        ? ScheduleType.valueOf(rs.getString("schedule_type"))
                        : null)
                .resourceId(rs.getString("resource_id"))
                .scheduleName(rs.getString("schedule_name"))
                .payload(rs.getString("payload"))
                .build();
    }
}
