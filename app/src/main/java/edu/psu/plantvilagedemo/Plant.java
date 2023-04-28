package edu.psu.plantvilagedemo;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plants")
public class Plant {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "plant_name")
    private String plantName;

    @ColumnInfo(name = "plant_description")
    private String plantDescription;

    @ColumnInfo(name = "plant_prescription")
    private String plantPrescription;

    public Plant(@NonNull String plantName, @NonNull String plantDescription, @NonNull String plantPrescription) {
        this.plantName = plantName;
        this.plantDescription = plantDescription;
        this.plantPrescription = plantPrescription;
    }

    //need set and get for dialog pop up
    @NonNull
    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(@NonNull String plantName) {
        this.plantName = plantName;
    }

    public String getPlantDescription() {
        return plantDescription;
    }

    public void setPlantDescription(String plantDescription) {
        this.plantDescription = plantDescription;
    }

    public String getPlantPrescription() {
        return plantPrescription;
    }

    public void setPlantPrescription(String plantPrescription) {
        this.plantPrescription = plantPrescription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
