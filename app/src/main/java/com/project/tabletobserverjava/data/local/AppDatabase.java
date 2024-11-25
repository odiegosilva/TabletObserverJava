package com.project.tabletobserverjava.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.project.tabletobserverjava.data.dao.EventLogDao;
import com.project.tabletobserverjava.data.model.EventLog;

@Database(entities = {EventLog.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract EventLogDao eventLogDao();

    /**
     * Obtém a instância do banco de dados.
     *
     * @param context Contexto da aplicação.
     * @return Instância única do AppDatabase.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "event_logs_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
