package com.rifaikuci.alzheimer_tracking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    Button btnBilgilerim, btnKisilerim, btnHarita, btnHakkimda;

    TextView txtTarih, bilgiAdsoyad;
    CircleImageView profile_image;
    String adsoyadElement, resimElement;
    int satirSayisi = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        transparanEkran();
        variableDesc();
        databaseKontrol();

        txtTarih.setText(getCurrentTime().toString());

        btnBilgilerim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Bilgilerim.class);
                startActivity(intent);
            }
        });

        btnKisilerim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Kisiler.class);
                startActivity(intent);
            }
        });

        btnHarita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Harita", Toast.LENGTH_SHORT).show();
            }
        });

        btnHakkimda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Hakkımda", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void databaseKontrol() {
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

            String sqlTable = "CREATE TABLE IF NOT EXISTS tblBilgilerim (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "adsoyad TEXT, aciklama TEXT, mail TEXT, telefon TEXT, resim TEXT, enlem TEXT, boylam TEXT, cinsiyet TEXT)";
            database.execSQL(sqlTable);

            String countQuery = "SELECT  * FROM  tblBilgilerim";
            Cursor cursor = database.rawQuery(countQuery, null);
            satirSayisi = cursor.getCount();
            int adSoyadIx = cursor.getColumnIndex("adsoyad");
            int resim = cursor.getColumnIndex("resim");

            while (cursor.moveToNext()) {
                adsoyadElement = cursor.getString(adSoyadIx);
                resimElement = cursor.getString(resim);
            }

            if (satirSayisi == 0) {
                profile_image.setVisibility(View.INVISIBLE);
                bilgiAdsoyad.setVisibility(View.INVISIBLE);
            } else {
                profile_image.setVisibility(View.VISIBLE);
                bilgiAdsoyad.setVisibility(View.VISIBLE);
                bilgiAdsoyad.setText(adsoyadElement.toString());
                profile_image.setImageURI(Uri.parse(resimElement));

            }
            cursor.close();
            database.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //ekranı transpan yapar
    public void transparanEkran() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public void variableDesc() {

        btnBilgilerim = (Button) findViewById(R.id.btnBilgilerim);
        btnKisilerim = (Button) findViewById(R.id.btnKisilerim);
        btnHarita = (Button) findViewById(R.id.btnHarita);
        btnHakkimda = (Button) findViewById(R.id.btnHakkimda);

        txtTarih = (TextView) findViewById(R.id.txtTarih);
        bilgiAdsoyad = (TextView) findViewById(R.id.bilgiAdsoyad);

        profile_image = (CircleImageView) findViewById(R.id.profile_image);
    }

    public static final String DATE_FORMAT_1 = "dd.MM.yyyy - EEEE";
    // Tarih yazdırma
    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_1, new Locale("TR"));
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }


}
