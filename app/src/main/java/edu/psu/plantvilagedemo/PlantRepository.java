package edu.psu.plantvilagedemo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlantRepository {
    private PlantDao plantDao;
    private ExecutorService executorService;
    private LiveData<List<Plant>> allPlants;

    public PlantRepository(Context context) {
        PlantDatabase plantDatabase = PlantDatabase.getInstance(context);
        plantDao = plantDatabase.plantDao();
        executorService = Executors.newSingleThreadExecutor();

        allPlants = plantDao.getAllPlants();
    }

    public LiveData<Plant> getPlantByName(String plantName) {
        return plantDao.getPlantByName(plantName);
    }
    public LiveData<Plant> getPlantDescription(String plantDesc) {
        return plantDao.getPlantByName(plantDesc);
    }
    public LiveData<Plant> getPlantPres(String plantPres) {
        return plantDao.getPlantByName(plantPres);
    }

    public LiveData<List<Plant>> getAllPlants() {
        return allPlants;
    }

    public Plant getPlantByNameDirect(String plantName) {
        for (int i = 0; i < DefaultPlants.PLANT_NAMES.length; i++) {
            if (DefaultPlants.PLANT_NAMES[i].equals(plantName)) {
                return new Plant(DefaultPlants.PLANT_NAMES[i], DefaultPlants.PLANT_DESCRIPTIONS[i], DefaultPlants.PLANT_PRESCRIPTIONS[i]);
            }
        }
        return null;
    }
}
