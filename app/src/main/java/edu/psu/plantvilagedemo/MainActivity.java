package edu.psu.plantvilagedemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.psu.plantvilagedemo.ml.Pvdemo;


public class MainActivity extends AppCompatActivity {

    private PlantViewModel plantViewModel;
    private Interpreter tflite;
    private Button predictButton;
    private static final String THEME_PREFERENCE = "theme_preference";
    private static final String DARK_MODE_KEY = "dark_mode";
    private Uri selectedImageUri;

    Button predictBtn, captureBtn, viewPlantsButton;
    ImageView imageDisplay;
    Bitmap bitmap;
    Plant plant = new Plant("plantName", "plantInformation", "plantPrescription");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the theme based on the user's preference
        loadSavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageDisplay = findViewById(R.id.imageDisplay);
        predictButton = findViewById(R.id.predictButton);

        // Restore the image URI from the saved instance state
        if (savedInstanceState != null) {
            String imageUriString = savedInstanceState.getString("selected_image_uri");
            if (imageUriString != null) {
                selectedImageUri = Uri.parse(imageUriString);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageDisplay.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Set the default image for the imageDisplay ImageView
            imageDisplay.setImageResource(R.drawable.default_image);
        }

        plantViewModel = new ViewModelProvider(this).get(PlantViewModel.class);

        //permission
        getPermission();
        String[] labels = new String[1001];
        int cnt = 0;
        try{
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
            String line = bufferReader.readLine();
            while(line!=null){
                labels[cnt]=line;
                cnt++;
                line = bufferReader.readLine();
            }
        } catch (IOException e){
            e.printStackTrace();
        }


        predictBtn = findViewById(R.id.predictButton);
        captureBtn = findViewById(R.id.captureButton);
        imageDisplay = findViewById(R.id.imageDisplay);
        viewPlantsButton = findViewById(R.id.view_plants_button);

        imageDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
            }
        });

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 12);
            }
        });

        predictBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (bitmap == null) {
                    Toast.makeText(MainActivity.this, "Please set the image before you predict.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Pvdemo model = Pvdemo.newInstance(MainActivity.this);

                    // Creates inputs for reference.
                    // alt + enter to auto import libraries
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

                    // Preprocess the image
                    ImageProcessor imageProcessor = new ImageProcessor.Builder()
                            .add(new ResizeWithCropOrPadOp(bitmap.getWidth(), bitmap.getHeight()))
                            .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                            .add(new NormalizeOp(127.5f, 127.5f)) // Add normalization if used during training
                            .build();

                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                    tensorImage.load(bitmap);
                    tensorImage = imageProcessor.process(tensorImage);

                    // Load the preprocessed image into inputFeature0
                    inputFeature0.loadBuffer(tensorImage.getBuffer());

                    // Runs model inference and gets result.
                    Pvdemo.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    PlantRepository plantRepository = new PlantRepository(MainActivity.this);
                    String predictedLabel = labels[getMax(outputFeature0.getFloatArray())].trim();
                    Log.d("Predicted Plant Label", predictedLabel);
                    Plant plant = plantRepository.getPlantByNameDirect(predictedLabel);
                    if (plant != null) {
                        showPlantInfoDialog(plant.getPlantName(), plant.getPlantDescription(), plant.getPlantPrescription());
                    } else {
                        showPlantInfoDialog("Error: No plant information found.", "", "");
                    }


                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                }


            }
        });


        viewPlantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlantListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageDisplay.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == 12 && resultCode == RESULT_OK && data != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
//            imageDisplay.setImageBitmap(bitmap);
            selectedImageUri = getImageUri(this, bitmap);
        }
    }

    public void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 12);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 13);
            }
        }
    }

    int getMax(float[] arr){
        int max=0;
        for(int i=0; i<arr.length; i++){
            if(arr[i] > arr[max]) max=i;
        }
        return max;
    }

    private void showPlantInfoDialog(String plantName, String plantDescription, String plantPrescription) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View plantInfoView = inflater.inflate(R.layout.plant_info_dialog, null);

        TextView plantNameTextView = plantInfoView.findViewById(R.id.plant_name);
        TextView plantDescriptionTextView = plantInfoView.findViewById(R.id.plant_description);
        TextView plantPrescriptionTextView = plantInfoView.findViewById(R.id.plant_prescription);

        plantNameTextView.setText(plantName);
        plantDescriptionTextView.setText(plantDescription);
        plantPrescriptionTextView.setText(plantPrescription);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(plantInfoView)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_go_home) {
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
    public Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Captured_Image", null);
        return Uri.parse(path);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedImageUri != null) {
            outState.putString("selected_image_uri", selectedImageUri.toString());
        }
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String imageUriString = savedInstanceState.getString("selected_image_uri");
        if (imageUriString != null) {
            selectedImageUri = Uri.parse(imageUriString);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imageDisplay.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}