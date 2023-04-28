package edu.psu.plantvilagedemo;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class PlantViewModel extends AndroidViewModel {
    private PlantRepository plantRepository;
    private LiveData<List<Plant>> allPlants;

    public PlantViewModel(@NonNull Application application) {
        super(application);
        plantRepository = new PlantRepository(application);
        allPlants = plantRepository.getAllPlants();
    }

    public LiveData<List<Plant>> getAllPlants() {
        return allPlants;
    }
}