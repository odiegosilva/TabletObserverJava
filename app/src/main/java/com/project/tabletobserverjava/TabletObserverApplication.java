package com.project.tabletobserverjava;

import android.app.Application;

import androidx.room.Room;

import com.project.tabletobserverjava.data.local.AppDatabase;

public class TabletObserverApplication extends Application {
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "tablet_observer_db"
        ).build();
    }

    public AppDatabase getDatabase() {
        return database;
    }
}