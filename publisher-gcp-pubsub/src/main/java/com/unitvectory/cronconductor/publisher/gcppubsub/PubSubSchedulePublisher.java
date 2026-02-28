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
package com.unitvectory.cronconductor.publisher.gcppubsub;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unitvectory.cronconductor.common.exception.CronConductorException;
import com.unitvectory.cronconductor.common.model.ScheduleEntry;
import com.unitvectory.cronconductor.common.publisher.SchedulePublisher;
import lombok.NonNull;

/**
 * GCP Pub/Sub implementation of the {@link SchedulePublisher} interface.
 * 
 * Publishes schedule execution events as JSON messages to a Google Cloud Pub/Sub topic.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class PubSubSchedulePublisher implements SchedulePublisher {

    private static final Gson GSON = new GsonBuilder().create();

    private final Publisher publisher;

    /**
     * Creates a new PubSubSchedulePublisher.
     * 
     * @param projectId the GCP project ID
     * @param topicId the Pub/Sub topic ID
     */
    public PubSubSchedulePublisher(@NonNull String projectId, @NonNull String topicId) {
        try {
            TopicName topicName = TopicName.of(projectId, topicId);
            this.publisher = Publisher.newBuilder(topicName).build();
        } catch (IOException e) {
            throw new CronConductorException("Failed to create Pub/Sub publisher", e);
        }
    }

    /**
     * Creates a new PubSubSchedulePublisher with an existing Publisher instance.
     * 
     * @param publisher the Pub/Sub publisher
     */
    PubSubSchedulePublisher(@NonNull Publisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(ScheduleEntry entry) {
        String json = GSON.toJson(entry);

        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(json))
                .putAttributes("scheduleId", entry.getScheduleId())
                .putAttributes("namespace", entry.getNamespace())
                .putAttributes("scheduleType", entry.getScheduleType().name())
                .build();

        try {
            ApiFuture<String> future = publisher.publish(message);
            future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new CronConductorException("Failed to publish schedule event to Pub/Sub", e);
        }
    }

    /**
     * Shuts down the Pub/Sub publisher.
     */
    public void shutdown() {
        if (publisher != null) {
            publisher.shutdown();
        }
    }
}
