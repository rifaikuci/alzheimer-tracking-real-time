package com.rifaikuci.alzheimer_tracking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMapLongClickListener {

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static GoogleApiClient mGoogleApiClient;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 3;

    private GoogleMap mMap;
    Geocoder geocoder;
    Circle circle;
    LocationListener locationListener;
    LocationManager locationManager;

    String[] address = {""};
    int satirSayisi = 0, gonderilmeDurumu;
    double enlemElement, boylamElement;
    List<Address> addressList;
    ArrayList<ModelKisiler> models = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        transparanEkran();

        gonderilmeDurumu = 0;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        satirSayisi = databaseKontrol();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mMap.setMyLocationEnabled(true);
                float[] distance = new float[2];

                try {
                    String adSoyad = "";
                    Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                            circle.getCenter().latitude, circle.getCenter().longitude, distance);
                    if (distance[0] > circle.getRadius()) {
                        //Eğer çemberin dışında ise

                        if (adSoyadDondur() != "") {
                            // adsoyad = "Hasta bilgisi adısoyadı alındı
                            adSoyad = adSoyadDondur();

                        }

                        models = kisilerDondur();
                        if (!models.isEmpty()) {
                            try {
                                for (int i = 0; i < models.size(); i++) {

                                    String mesaj = "";

                                    if (gonderilmeDurumu == 0) {

                                        try {
                                            mesaj = mesaj + "Merhaba " + models.get(i).getAdSoyad() + " Ben " + adSoyad + " Şu anki konumu mu enlem ve boylam olarak buradan görebilirsin";
                                            mesaj += " Boylam: " + location.getLatitude() + " Enlem: " + location.getLongitude() + " ";
                                            mesaj += "Enlem Boylam bilgisini google da kopyalayıp bulabilirsiniz:" + location.getLatitude() + "," + location.getLongitude();

                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage(models.get(i).getTelefon(), null, mesaj, null, null);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    gonderilmeDurumu = 1;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    } else {
                        //Eğer çemberin içinde yer alacaksa işlem yapılacaksa burada yapılacaktır.
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 9000, 0, locationListener);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.clear();

        //google izinleri
        initGoogleAPIClient();
        checkPermissions();
        //bu ikisi alundığında izinlerin çağrılma işlemleri tamamlanacak

        try {
            mMap.setMyLocationEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (circle != null) {
            circle.remove();
        }

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
                        address[0] = "";

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

                mMap.addMarker(new MarkerOptions().position(new LatLng(enlemElement, boylamElement))
                        .title(address[0])).setIcon(BitmapDescriptorFactory.fromBitmap(convertMarker()));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(enlemElement, boylamElement), 10));
                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(enlemElement, boylamElement))
                        .radius(1500)
                        .strokeColor(Color.WHITE)
                        .strokeWidth(6f)
                        .fillColor(Color.argb(70, 48, 213, 200)));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                            address[0] = "";
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

                    mMap.addMarker(new MarkerOptions().position(latLng)
                            .title(address[0])).setIcon(BitmapDescriptorFactory.fromBitmap(convertMarker()));
                    if (circle != null) {
                        circle.remove();
                    }
                    circle = mMap.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(1500)
                            .strokeColor(Color.WHITE)
                            .strokeWidth(6f)
                            .fillColor(Color.argb(70, 48, 213, 200)));

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

    private ArrayList<ModelKisiler> kisilerDondur() {
        ArrayList<ModelKisiler> kisiler = new ArrayList<>();

        try {
            SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

            String sqlTable = "CREATE TABLE IF NOT EXISTS tblKisiler (id INTEGER PRIMARY KEY AUTOINCREMENT,adSoyad TEXT, aciklama TEXT, mail TEXT, telefon TEXT, resim TEXT)";
            database.execSQL(sqlTable);

            Cursor cursor = database.rawQuery("Select * From tblKisiler ORDER BY id DESC LIMIT 3", null);

            int adSoyadIx = cursor.getColumnIndex("adSoyad");
            int telefonIx = cursor.getColumnIndex("telefon");
            int mailIx = cursor.getColumnIndex("mail");

            while (cursor.moveToNext()) {
                kisiler.add(new ModelKisiler(
                        cursor.getString(adSoyadIx),
                        cursor.getString(telefonIx),
                        cursor.getString(mailIx)));
            }

            cursor.close();
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kisiler;
    }

    private String adSoyadDondur() {
        String adsoyad = "";
        String adsoyadElement = "";
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

            String sqlTable = "CREATE TABLE IF NOT EXISTS tblBilgilerim (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "adsoyad TEXT, aciklama TEXT, mail TEXT, telefon TEXT, resim TEXT, enlem TEXT, boylam TEXT, cinsiyet TEXT)";
            database.execSQL(sqlTable);

            String countQuery = "SELECT  * FROM  tblBilgilerim";
            Cursor cursor = database.rawQuery(countQuery, null);
            int satirSayisi = cursor.getCount();
            int adSoyadIx = cursor.getColumnIndex("adsoyad");

            while (cursor.moveToNext()) {
                adsoyadElement = cursor.getString(adSoyadIx);
            }

            if (satirSayisi == 0) {
                adsoyad = "";
            } else {
                adsoyad = adsoyadElement.toString();

            }
            cursor.close();
            database.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return adsoyad;
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


    //  Konum izni için User Permission

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

    private void initGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
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
                        // istekler burda
                        //updateGPSStatus("GPS is Enabled in your device");
                        Log.d("locationEnable", "SUCCESS");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Konum ayarları etkin değil fakat dialog gösterip konum açılmasını sağlıyor isek buraya
                        // Dialog göster
                        try {
                            // startResolutionForResult(), çağırıp kontrol edilir
                            Log.d("locationEnable", "RESOLUTION_REQUIRED");
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Konum eğer açılamıyor ise buraya düşer
                        Log.d("locationEnable", "SETTINGS_CHANGE_UNAVAILABLE");
                        break;
                }
            }
        });
    }
    // Konum izinleri için tüm işlevler bitiş
}

