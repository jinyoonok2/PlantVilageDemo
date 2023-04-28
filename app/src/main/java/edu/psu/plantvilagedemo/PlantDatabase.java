package edu.psu.plantvilagedemo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Plant.class}, version = 1, exportSchema = false)
public abstract class PlantDatabase extends RoomDatabase {

    private static PlantDatabase INSTANCE;

    public abstract PlantDao plantDao();

    public static synchronized PlantDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PlantDatabase.class, "plant_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            new Thread(() -> populateInitialData(context)).start();
                        }
                    })
                    .build();
        }
        return INSTANCE;
    }
    private static void populateInitialData(Context context) {
        PlantDao plantDao = getInstance(context).plantDao();
        // Get the default plant data
        String[] plantNames = DefaultPlants.PLANT_NAMES;
        String[] plantDescriptions = DefaultPlants.PLANT_DESCRIPTIONS;
        String[] plantPrescriptions = DefaultPlants.PLANT_PRESCRIPTIONS;

        // Check if lengths of arrays are equal
        if (plantNames.length != plantDescriptions.length || plantNames.length != plantPrescriptions.length) {
            throw new IllegalStateException("Plant data arrays must have the same length");
        }

        // Iterate over the data and insert plants into the database
        for (int i = 0; i < plantNames.length; i++) {
            Plant plant = new Plant(plantNames[i], plantDescriptions[i], plantPrescriptions[i]);
            plantDao.insert(plant);
        }
    }
}
