package edu.psu.plantvilagedemo;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlantDao {

    @Query("SELECT * FROM plants")
    LiveData<List<Plant>> getAllPlants();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Plant plant);

    @Update
    void update(Plant plant);

    @Delete
    void delete(Plant plant);

    @Query("SELECT * FROM plants WHERE plant_name = :plantName")
    LiveData<Plant> getPlantByName(String plantName);

}