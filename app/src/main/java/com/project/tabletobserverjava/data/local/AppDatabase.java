package com.project.tabletobserverjava.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.project.tabletobserverjava.data.dao.EventLogDao;
import com.project.tabletobserverjava.data.model.EventLog;

@Database(entities = {EventLog.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract EventLogDao eventLogDao();
}