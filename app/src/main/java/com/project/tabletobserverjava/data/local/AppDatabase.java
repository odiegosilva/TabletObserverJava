package com.project.tabletobserverjava.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DeviceLog.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceLogDao deviceLogDao();
}
