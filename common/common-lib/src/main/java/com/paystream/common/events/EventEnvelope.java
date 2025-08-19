package com.paystream.common.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class EventEnvelope {


    public UUID id;
    public String type;
    public Instant occuredAt;
    public Map<String, Object> headers;
    public Object payload;

    public static EventEnvelope of(String type, Instant occuredAt, Map<String, Object> headers, Object payload) {
        EventEnvelope e = new EventEnvelope();
        e.id = UUID.randomUUID();
        e.type = type;
        e.occuredAt = Instant.now();
        e.headers = headers;
        e.payload = payload;
        return e;
    }
}
