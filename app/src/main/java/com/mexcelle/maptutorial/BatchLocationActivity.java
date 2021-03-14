package com.mexcelle.maptutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class BatchLocationActivity extends AppCompatActivity {

    TextView updateLocationTv;
    Button locationRequestButton;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_location);
        updateLocationTv = findViewById(R.id.location_txt);
        locationRequestButton = findViewById(R.id.request_btn);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult == null) {
                    Log.e("location","location Error");
                    return;
                }

                List<Location> locations=locationResult.getLocations();

                //here use to call all update  location
                LocationResultHelper helper=new LocationResultHelper(BatchLocationActivity.this,locations);
                helper.showNotification();

                Location location = locationResult.getLastLocation();
                updateLocationTv.setText(helper.getLocationText());
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Toast.makeText(BatchLocationActivity.this, "location received" +locations.size(), Toast.LENGTH_SHORT).show();
            }
        };


        locationRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationRequest locationResult = LocationRequest.create();
                locationResult.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationResult.setInterval(5000);
                locationResult.setFastestInterval(2000);
                locationResult.setMaxWaitTime(60 * 1000);

                if (ContextCompat.checkSelfPermission(BatchLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(BatchLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }
                fusedLocationProviderClient.requestLocationUpdates(locationResult, locationCallback,null);

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        //use to remove from background
        //fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}