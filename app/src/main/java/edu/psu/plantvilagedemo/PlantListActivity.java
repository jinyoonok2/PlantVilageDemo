package edu.psu.plantvilagedemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlantListActivity extends AppCompatActivity {

    private PlantViewModel plantViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadSavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_list);

        RecyclerView recyclerView = findViewById(R.id.plant_recycler_view);
        PlantListAdapter adapter = new PlantListAdapter();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        plantViewModel = new ViewModelProvider(this).get(PlantViewModel.class);
        plantViewModel.getAllPlants().observe(this, plants -> {
            // Update the cached copy of the plants in the adapter.
            adapter.setPlants(plants);
        });

        adapter.setOnItemClickListener(plant -> {
            // Show plant description and prescription when a plant is clicked
            showPlantInfoDialog(plant.getPlantName(), plant.getPlantDescription(), plant.getPlantPrescription());
        });
    }
    private void showPlantInfoDialog(String name, String description, String prescription) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.plant_info_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Set the TextViews content in the dialog
        TextView plantName = dialogView.findViewById(R.id.plant_name);
        TextView plantDescription = dialogView.findViewById(R.id.plant_description);
        TextView plantPrescription = dialogView.findViewById(R.id.plant_prescription);

        plantName.setText(name);
        plantDescription.setText(description);
        plantPrescription.setText(prescription);

        // Set the positive button
        builder.setPositiveButton("OK", (dialog, id) -> dialog.dismiss());

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.plant_list_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_go_home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void loadSavedTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedTheme = sharedPreferences.getString("saved_theme", "light");
        switch (savedTheme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }
}