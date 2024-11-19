package com.project.tabletobserverjava.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "device_log")
public class DeviceLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long timestamp;
    private String type; // Exemplo: "Conexão", "Desempenho"
    private String message;

    public DeviceLog(long timestamp, String type, String message) {
        this.timestamp = timestamp;
        this.type = type;
        this.message = message;
    }

    // Getters e Setters
    public int getId()
    { return id; }
    public void setId(int id)
    { this.id = id; }
    public long getTimestamp()
    { return timestamp; }
    public void setTimestamp(long timestamp)
    { this.timestamp = timestamp;}
    public String getType()
    { return type; }
    public void setType(String type)
    { this.type = type; }
    public String getMessage()
    { return message; }
    public void setMessage(String message)
    { this.message = message; }
}
