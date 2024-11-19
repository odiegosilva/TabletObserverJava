package com.project.tabletobserverjava.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceLogDao {
    @Insert
    void insertLog(DeviceLog log);

    @Query("SELECT * FROM device_log ORDER BY timestamp DESC")
    List<DeviceLog> getAllLogs();
}

