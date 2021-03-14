package com.mexcelle.maptutorial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final int PERMISSION_REQUEST_CODE = 9001;
    public static final int PLAY_SERVICES_ERROE_CODE = 9001;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9005;


    private final double Lat = 33.690904;
    private final double Long = 73.051865;

    private boolean mLocationPermissionGranted;
    private final String TAG = String.valueOf(MainActivity.this);
    private GoogleMap googlemap;
    private MapView mapView;
    private EditText editText;
    private Button mbutton;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        editText = findViewById(R.id.editTextTextPersonName);
        mbutton = findViewById(R.id.button);
//        setSupportActionBar(toolbar);
        isServicesOK();
        initGoogleMap();


        //use to get device current location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                Toast.makeText(MainActivity.this, "" + location.getLatitude() + "\n" + location.getLongitude(), Toast.LENGTH_SHORT).show();
            }
        };
        getLocationUpdate();

        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geoLocate(view);
                reverseGeoLocate(view);
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (googlemap != null) {
                    googlemap.animateCamera(CameraUpdateFactory.zoomTo(3.0f));
                    LatLng latLng = new LatLng(36.690904, 77.051865);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                    googlemap.animateCamera(cameraUpdate, 3, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            Toast.makeText(MainActivity.this, "animation finished", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(MainActivity.this, "animation cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        if (mLocationPermissionGranted) {
            Toast.makeText(this, "Ready to Map!", Toast.LENGTH_SHORT).show();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                }
            }
        }

        mapView = findViewById(R.id.mapView);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("map", "map is ready");

        // googleMap.addMarker(new MarkerOptions().position(new LatLng(Long, Lat)).title("Marker"));
        googlemap = googleMap;
        //use for check device loacation
       googlemap.setMyLocationEnabled(true);

//enabling UI control of map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        gotLlocation(Lat, Long);

    }

    private void gotLlocation(double lat, double lang) {
        LatLng latLng = new LatLng(lat, lang);
        showMarelr(latLng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        googlemap.moveCamera(cameraUpdate);
    }


    private void showMarelr(LatLng lang) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(lang);
        googlemap.addMarker(markerOptions);

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//if it is not then this method will run
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        }
    }


    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, PLAY_SERVICES_ERROE_CODE);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initGoogleMap() {
        if (isServicesOK()) {

            if (isMapsEnabled()) {
                if (checkLocationPermission()) {
                    Toast.makeText(this, "Ready to map", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestLocationPermission();
            }

            {

            }

        }


    }

    private void geoLocate(View view) {
        //this method is use get latlong from address name
        hideSoftKeyboard(view);
        String locationname = editText.getText().toString();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationname, 3);
            if (addressList.size() > 0) {
                Address address = addressList.get(0);
                gotLlocation(address.getLatitude(), address.getLongitude());
                googlemap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())));

                Log.e("addresslocatopn", address.getAddressLine(address.getMaxAddressLineIndex()));
                Toast.makeText(this, "" + address.getLocality(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reverseGeoLocate(View view) {
        //this method is use get address name from  latlong
        hideSoftKeyboard(view);
        String locationname = editText.getText().toString();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(Lat, Long, 3);
            if (addressList.size() > 0) {
                Address address = addressList.get(0);
                gotLlocation(address.getLatitude(), address.getLongitude());
                googlemap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())));

                Log.e("addresslocatopn", address.getAddressLine(address.getMaxAddressLineIndex()));
                Toast.makeText(this, "" + address.getLocality(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_DENIED;
    }


    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }


    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();

        // closing of request

        if (locationCallback!=null)
        {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSIONS_REQUEST_ENABLE_GPS) ;
        {
            if (isMapsEnabled()) {
                Toast.makeText(this, "GPS is enable", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "GPS is not enable", Toast.LENGTH_SHORT).show();
            }
        }

    }


    @SuppressLint("MissingPermission")
    private void getLocationUpdate() {
        //method to get current location in timer

        LocationRequest locationResult = LocationRequest.create();
        locationResult.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationResult.setInterval(5000);
        locationResult.setFastestInterval(2000);
        fusedLocationProviderClient.requestLocationUpdates(locationResult,locationCallback, Looper.myLooper());
    }
}


