package com.rifaikuci.alzheimer_tracking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMapLongClickListener {

    private static final int REQUEST_CHECK_SETTINGS = 5;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 50;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Geocoder geocoder;
    String[] address = {""};
    int satirSayisi = 0;
    double enlemElement, boylamElement;
    List<Address> addressList;

    final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        transparanEkran();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
        satirSayisi = databaseKontrol();


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        checkPermissions();//konum izni al
        mMap.clear();
        if (satirSayisi > 0) {

            try {
                SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

                String sqlTable = "CREATE TABLE IF NOT EXISTS tblBilgilerim (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "adsoyad TEXT, aciklama TEXT, mail TEXT, telefon TEXT, resim TEXT, enlem TEXT, boylam TEXT, cinsiyet TEXT)";
                database.execSQL(sqlTable);

                String countQuery = "SELECT  enlem,boylam FROM  tblBilgilerim";
                Cursor cursor = database.rawQuery(countQuery, null);
                satirSayisi = cursor.getCount();
                int enlemIx = cursor.getColumnIndex("enlem");
                int boylamIx = cursor.getColumnIndex("boylam");


                while (cursor.moveToNext()) {
                    enlemElement = Double.parseDouble(cursor.getString(enlemIx));
                    boylamElement = Double.parseDouble(cursor.getString(boylamIx));
                }

                cursor.close();
                database.close();
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    addressList = geocoder.getFromLocation(enlemElement, boylamElement, 1);
                    if (addressList != null && addressList.size() > 0) {
                        address[0] ="";
                        if (addressList.get(0).getThoroughfare() != null) {
                            address[0] += addressList.get(0).getThoroughfare();

                            if (addressList.get(0).getSubThoroughfare() != null) {
                                address[0] += addressList.get(0).getSubThoroughfare();
                            }
                        }


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (address[0].matches("")) {
                    address[0] = "Yer Bilinmiyor";
                }

                mMap.addMarker(new MarkerOptions().position(new LatLng(enlemElement,boylamElement)).title(address[0])).setIcon(BitmapDescriptorFactory.fromBitmap(convertMarker()));





                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        yerGuncellemek(latLng);
                    }
                });

            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

    }


    private void yerGuncellemek(final LatLng latLng) {


    }

    private int databaseKontrol() {
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

            String sqlTable = "CREATE TABLE IF NOT EXISTS tblBilgilerim (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "adsoyad TEXT, aciklama TEXT, mail TEXT, telefon TEXT, resim TEXT, enlem TEXT, boylam TEXT, cinsiyet TEXT)";
            database.execSQL(sqlTable);

            String countQuery = "SELECT  * FROM  tblBilgilerim";
            Cursor cursor = database.rawQuery(countQuery, null);
            satirSayisi = cursor.getCount();

            cursor.close();
            database.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return satirSayisi;
    }

    private Bitmap convertMarker() {
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.marker);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        return smallMarker;
    }

    private void transparanEkran() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    // Konum izinleri için tüm işlevler başlangıç
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {

            if (resultCode == RESULT_OK) {

                Toast.makeText(MapsActivity.this, "GPS Aktif", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(MapsActivity.this, "GPS Pasif", Toast.LENGTH_LONG).show();
            }

        }
    }

    /*  Konum izni için User Permission  */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_INTENT_ID);

        } else {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_INTENT_ID);
        }
    }

    // izin cevapları
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_FINE_LOCATION_INTENT_ID) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MapsActivity.this, "İzin verildi", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MapsActivity.this, "İzin verilmedi", Toast.LENGTH_LONG).show();
            }

        }
    }

    /* Versiyon Kontroller */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MapsActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
                requestLocationPermission();
            else
                showLocationState();
        } else
            showLocationState();

    }

    private void showLocationState() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Setting priotity of Location request to high
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        //Konum ayarları etkin ise buraya
                        Log.d("locationEnable", "SUCCESS");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        try {
                            // startResolutionForResult(), çağırıp kontrol edilir
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d("locationEnable", "SETTINGS_CHANGE_UNAVAILABLE");
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        if (satirSayisi == 0) {
            Intent intent = new Intent(getApplicationContext(), Bilgilerim.class);
            startActivity(intent);
        } else {


            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Uyarı");

            builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {

                        mMap.clear();

                        addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                        if (addressList != null && addressList.size() > 0) {
                            address[0] ="";
                            if (addressList.get(0).getThoroughfare() != null) {
                                address[0] += addressList.get(0).getThoroughfare();

                                if (addressList.get(0).getSubThoroughfare() != null) {
                                    address[0] += addressList.get(0).getSubThoroughfare();
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (address[0].matches("")) {
                        address[0] = "Yer Bilinmiyor";
                    }

                    mMap.addMarker(new MarkerOptions().position(latLng).title(address[0])).setIcon(BitmapDescriptorFactory.fromBitmap(convertMarker()));


                    SQLiteDatabase database = openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);
                    String sqlTable = "CREATE TABLE IF NOT EXISTS tblBilgilerim (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "adsoyad TEXT, aciklama TEXT, mail TEXT, telefon TEXT, resim TEXT, enlem TEXT, boylam TEXT, cinsiyet TEXT)";
                    database.execSQL(sqlTable);

                    String sqlGuncele = "UPDATE tblBilgilerim SET " +
                            "enlem = '" + String.valueOf(latLng.latitude) + "',boylam='" + String.valueOf(latLng.longitude) + "'";

                    database.execSQL(sqlGuncele);
                    database.close();
                }
            });

            builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "Konum adresinizi güncelleme işlemi iptal edildi", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setMessage(address[0] + " Konumunuzu değiştirmek istediğinizden emin misiniz? ");

            AlertDialog alert = builder.create();
            alert.show();
        }
    }
    // Konum izinleri için tüm işlevler bitiş
}

