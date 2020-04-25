package com.rifaikuci.alzheimer_tracking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    Button btnBilgilerim, btnKisilerim, btnHarita, btnHakkimda;

    TextView txtTarih;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        transparanEkran();
        variableDesc();


        txtTarih.setText(getCurrentTime().toString());

        btnBilgilerim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Bilgilerim", Toast.LENGTH_SHORT).show();
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
