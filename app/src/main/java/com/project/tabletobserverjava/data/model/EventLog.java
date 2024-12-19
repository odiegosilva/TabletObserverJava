package com.project.tabletobserverjava.data.model;

/**
 Entidade que representará os dados armazenados
 */

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "event_logs")
public class EventLog {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long timestamp;
    private String eventType;
    private String description;
    /**
     * Construtor público vazio para o Room.
     * Necessário para operações internas do framework.
     */
    public EventLog() {
    }
    @Ignore
    public EventLog(long timestamp, String eventType, String description) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.description = description;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
