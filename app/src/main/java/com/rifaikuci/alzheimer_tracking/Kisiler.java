package com.rifaikuci.alzheimer_tracking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class Kisiler extends AppCompatActivity {

    ViewPager viewPager;
    Adapter adapter;
    List<ModelKisiler> models;
    FloatingActionButton btnKisiEkle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kisiler);
        btnKisiEkle = (FloatingActionButton) findViewById(R.id.btnKisiEkle);
        transparanEkran();

        models = new ArrayList<>();

        SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

        Cursor cursor = database.rawQuery("Select * From tblKisiler ORDER BY id DESC", null);
        int idIx = cursor.getColumnIndex("id");
        int adSoyadIx = cursor.getColumnIndex("adSoyad");
        int aciklamaIx = cursor.getColumnIndex("aciklama");
        int telefonIx = cursor.getColumnIndex("telefon");
        int mailIx = cursor.getColumnIndex("mail");
        int resim = cursor.getColumnIndex("resim");


        while (cursor.moveToNext()) {
            models.add(new ModelKisiler(cursor.getInt(idIx), cursor.getString(adSoyadIx), cursor.getString(aciklamaIx), cursor.getString(resim)));

        }
        cursor.close();

        database.close();

        adapter = new Adapter(models, this);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(110, 0, 110, 0);


        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btnKisiEkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Kisi_ekle.class);
                startActivity(intent);
            }
        });

    }


    public void transparanEkran() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


}
