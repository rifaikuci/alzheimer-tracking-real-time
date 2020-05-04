package com.rifaikuci.alzheimer_tracking;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

public class Kisi_ekle extends AppCompatActivity {

    ImageView image, imageTrash;
    TextView txtAdSoyad, txtAciklama, txtMail, txtTelefon;
    Button btnGonder;
    String adSoyad, aciklama, mail, telefon, resim, adsoyadElement, aciklamaElement, resimElement, telefonElement, mailElement;
    RelativeLayout relativeAdsoyad, relativeAciklama, relativeMail, relativeTelefon, relativeImage;
    LinearLayout linearBack;
    int idElement, id;

    Uri resultUri;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kisi_ekle);
        transparanEkran();
        variableDesc();

        int tur = intent.getIntExtra("tur", 0);

        if (tur == 1) {
            duzenleActivity();
        } else {
            btnGonder.setText("Ekle");
            imageTrash.setVisibility(View.INVISIBLE);
        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelectClick();
            }
        });

        btnGonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnGonderClick();
            }
        });

        linearBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearBackClick();
            }
        });
    }

    private void duzenleActivity() {

        imageTrash.setVisibility(View.VISIBLE);
        btnGonder.setText("Güncelle");

        try {

            id = intent.getIntExtra("id", 0);
            SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

            Cursor cursor = database.rawQuery("Select *from tblKisiler where id='" + id + "'", null);

            int idIx = cursor.getColumnIndex("id");
            int adSoyadIx = cursor.getColumnIndex("adSoyad");
            int aciklamaIx = cursor.getColumnIndex("aciklama");
            int telefonIx = cursor.getColumnIndex("telefon");
            int mailIx = cursor.getColumnIndex("mail");
            int resim = cursor.getColumnIndex("resim");


            while (cursor.moveToNext()) {
                idElement = cursor.getInt(idIx);
                adsoyadElement = cursor.getString(adSoyadIx);
                aciklamaElement = cursor.getString(aciklamaIx);
                resimElement = cursor.getString(resim);
                telefonElement = cursor.getString(telefonIx);
                mailElement = cursor.getString(mailIx);
            }

            cursor.close();
            database.close();

            txtAdSoyad.setText(adsoyadElement);
            txtAciklama.setText(aciklamaElement);
            txtMail.setText(mailElement);
            txtTelefon.setText(telefonElement);
            image.setImageURI(Uri.parse(resimElement));

            imageTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageTrashClick();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void imageTrashClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Kisi_ekle.this);

        builder.setTitle("Uyarı");
        builder.setMessage(adsoyadElement + " Kişisini silmek istediğinizden emin misiniz? ");

        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SQLiteDatabase database = openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

                File dir = new File(Uri.parse(resimElement).getPath());
                dir.delete();

                String sqlTable = "Delete From tblKisiler where id='" + id + "'";
                database.execSQL(sqlTable);

                database.close();

                linearBackClick();
            }
        });

        builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Kişi silme işlemi iptal edildi", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void linearBackClick() {
        Intent intent = new Intent(getApplicationContext(), Kisiler.class);
        startActivity(intent);
    }

    private void btnGonderClick() {

        relativeDefault();

        adSoyad = txtAdSoyad.getText().toString().trim();
        aciklama = txtAciklama.getText().toString().trim();
        mail = txtMail.getText().toString().trim();
        telefon = txtTelefon.getText().toString().trim();

        try {
            resim = resultUri.toString();
        } catch (Exception e) {
            resim = "";
        }


        if (adSoyad.isEmpty() == true) {
            txtAdSoyad.setError("Ad ve soyad bilgisini giriniz!");
            relativeAdsoyad.setBackgroundColor(Color.RED);

        } else if (aciklama.isEmpty() == true) {
            txtAciklama.setError("Açıklama giriniz!");
            relativeAciklama.setBackgroundColor(Color.RED);

        } else if (mail.isEmpty() == true) {
            txtMail.setError("Mail boş geçilemez!");
            relativeMail.setBackgroundColor(Color.RED);

        } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches() == false) {
            txtMail.setError("Mail formatında değildir!");
            relativeMail.setBackgroundColor(Color.RED);

        } else if (telefon.isEmpty() == true) {
            txtTelefon.setError("Telefon boş geçilemez!");
            relativeTelefon.setBackgroundColor(Color.RED);

        } else {
            if (telefon.charAt(0) != '0') {
                telefon = "0" + telefon;
            }

            if (btnGonder.getText().equals("Ekle")) {

                if (resim.isEmpty() == true) {
                    Toast.makeText(getApplicationContext(), "Resim boş geçilemez", Toast.LENGTH_SHORT).show();
                    relativeImage.setBackgroundColor(Color.RED);

                } else {
                    databaseKaydet(adSoyad, aciklama, mail, telefon, resim);

                    linearBackClick();
                    temizle();
                }
            } else {
                SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

                String sqlGuncele = "";
                if (resim.isEmpty()) {
                    sqlGuncele = "UPDATE tblKisiler SET " +
                            "adSoyad = '" + adSoyad + "',aciklama='" + aciklama + "',mail='" + mail + "',telefon='" + telefon + "' where id='" + id + "'";
                    linearBackClick();
                    temizle();

                } else {
                    File dir = new File(Uri.parse(resimElement).getPath());
                    dir.delete();
                    sqlGuncele = "UPDATE tblKisiler SET " +
                            "adSoyad = '" + adSoyad + "',aciklama='" + aciklama + "',mail='" + mail + "',telefon='" + telefon + "',resim ='" + resim + "' where id='" + id + "'";
                    linearBackClick();
                    temizle();
                }

                database.execSQL(sqlGuncele);
                Toast.makeText(getApplicationContext(), adSoyad + " Kişisi Güncellendi.", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void databaseKaydet(String adSoyad, String aciklama, String mail, String telefon, String resim) {
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

            String sqlTable = "CREATE TABLE IF NOT EXISTS tblKisiler (id INTEGER PRIMARY KEY AUTOINCREMENT,adSoyad TEXT, aciklama TEXT, mail TEXT, telefon TEXT, resim TEXT)";
            database.execSQL(sqlTable);

            String sqlInsert = "INSERT INTO tblKisiler(adSoyad,aciklama,mail,telefon,resim) VALUES('" + adSoyad + "','" + aciklama + "','" + mail + "','" + telefon + "','" + resim + "')";
            database.execSQL(sqlInsert);

            database.close();

            Toast.makeText(getApplicationContext(), adSoyad + " Listeye eklendi.", Toast.LENGTH_LONG).show();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void imageSelectClick() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this);
    }


    private void variableDesc() {

        SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

        image = (ImageView) findViewById(R.id.image);
        imageTrash = (ImageView) findViewById(R.id.imageTrash);

        txtAdSoyad = (TextView) findViewById(R.id.txtAdsoyad);
        txtAciklama = (TextView) findViewById(R.id.txtAciklama);
        txtMail = (TextView) findViewById(R.id.txtMail);
        txtTelefon = (TextView) findViewById(R.id.txtTelefon);

        linearBack = (LinearLayout) findViewById(R.id.linearBack);

        btnGonder = (Button) findViewById(R.id.btnGonder);

        relativeAdsoyad = (RelativeLayout) findViewById(R.id.relativeAdSoyad);
        relativeAciklama = (RelativeLayout) findViewById(R.id.relativeAciklama);
        relativeMail = (RelativeLayout) findViewById(R.id.relativeMail);
        relativeTelefon = (RelativeLayout) findViewById(R.id.relativeTelefon);
        relativeImage = (RelativeLayout) findViewById(R.id.relativeImage);

        intent = getIntent();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                image.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void transparanEkran() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public void relativeDefault() {
        relativeAdsoyad.setBackgroundColor(Color.BLACK);
        relativeAciklama.setBackgroundColor(Color.BLACK);
        relativeMail.setBackgroundColor(Color.BLACK);
        relativeTelefon.setBackgroundColor(Color.BLACK);
        relativeImage.setBackgroundColor(Color.BLACK);

    }

    public void temizle() {
        txtTelefon.setText("");
        txtAdSoyad.setText("");
        txtMail.setText("");
        txtAciklama.setText("");
        image.setImageResource(R.drawable.selection);
    }

}
