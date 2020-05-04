package com.rifaikuci.alzheimer_tracking;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.File;

public class Bilgilerim extends AppCompatActivity {

    TextView txtAdsoyad, txtAciklama, txtKonum, txtMail, txtTelefon;
    ImageView image;
    Button btnGonder;
    RadioButton radioButton, radioErkek, radioKadin;
    RadioGroup groupCinsiyet;
    RelativeLayout relativeAdsoyad, relativeImage, relativeAciklama, relativeKonum, relativeMail, relativeTelefon, relativeCinsiyet;
    String radioCinsiyet, adsoyad, resim, aciklama, mail, telefon, adsoyadElement, aciklamaElement, resimElement, telefonElement, mailElement, cinsiyetElement;
    int satirSayisi, radioId, idElement;
    LinearLayout linearBack;
    Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bilgilerim);

        transparanEkran();
        variableDesc();
        databaseKontrol();

        linearBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearBackClick();
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelectClick();
            }
        });

        if (btnGonder.getText().equals("Güncelle")) {
            try {

                SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

                Cursor cursor = database.rawQuery("Select *from tblBilgilerim", null);

                int idIx = cursor.getColumnIndex("id");
                int adSoyadIx = cursor.getColumnIndex("adsoyad");
                int aciklamaIx = cursor.getColumnIndex("aciklama");
                int telefonIx = cursor.getColumnIndex("telefon");
                int mailIx = cursor.getColumnIndex("mail");
                int resim = cursor.getColumnIndex("resim");
                int cinsiyetIx = cursor.getColumnIndex("cinsiyet");


                while (cursor.moveToNext()) {
                    idElement = cursor.getInt(idIx);
                    adsoyadElement = cursor.getString(adSoyadIx);
                    aciklamaElement = cursor.getString(aciklamaIx);
                    resimElement = cursor.getString(resim);
                    telefonElement = cursor.getString(telefonIx);
                    mailElement = cursor.getString(mailIx);
                    cinsiyetElement = cursor.getString(cinsiyetIx);
                }

                cursor.close();
                database.close();

                txtAdsoyad.setText(adsoyadElement);
                txtAciklama.setText(aciklamaElement);
                txtMail.setText(mailElement);
                txtTelefon.setText(telefonElement);
                image.setImageURI(Uri.parse(resimElement));

                if (cinsiyetElement.equals("Erkek")) {
                    radioErkek.setChecked(true);
                } else {
                    radioKadin.setChecked(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        btnGonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnGonderClick();
            }
        });
    }

    private void imageSelectClick() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this);
    }

    private void btnGonderClick() {
        relativeDefault();

        adsoyad = txtAdsoyad.getText().toString().trim();
        aciklama = txtAciklama.getText().toString().trim();
        mail = txtMail.getText().toString().trim();
        telefon = txtTelefon.getText().toString().trim();


        try {
            resim = resultUri.toString();
        } catch (Exception e) {
            resim = "";
        }

        if (adsoyad.isEmpty() == true) {
            txtAdsoyad.setError("Ad ve soyad bilgisini giriniz!");
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

        } else if (radioCinsiyet.toString().isEmpty() == true) {
            Toast.makeText(getApplicationContext(), "Cinsiyeti seçiniz", Toast.LENGTH_LONG).show();
            relativeCinsiyet.setBackgroundColor(Color.RED);
        } else {

            if (telefon.charAt(0) != '0') {
                telefon = "0" + telefon;
            }
            if (btnGonder.getText().equals("Oluştur")) {

                if (resim.isEmpty() == true) {
                    Toast.makeText(getApplicationContext(), "Resim boş geçilemez", Toast.LENGTH_SHORT).show();
                    relativeImage.setBackgroundColor(Color.RED);

                } else {
                    veriKaydet(adsoyad, aciklama, mail, telefon, resim, radioCinsiyet);
                }
            } else {
                veriGuncelle(adsoyad, aciklama, mail, telefon, resim, radioCinsiyet);

            }
        }
    }

    private void veriGuncelle(String adsoyad, String aciklama, String mail, String telefon, String resim, String radioCinsiyet) {
        SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

        String sqlGuncele = "";
        if (this.resim.isEmpty()) {
            sqlGuncele = "UPDATE tblBilgilerim SET " +
                    "adSoyad = '" + adsoyad + "',aciklama='" + this.aciklama + "',mail='" + this.mail + "',telefon='" + this.telefon + "', cinsiyet='" + this.radioCinsiyet + "'";
            linearBackClick();
            temizle();

        } else {
            File dir = new File(Uri.parse(resimElement).getPath());
            dir.delete();
            sqlGuncele = "UPDATE tblBilgilerim SET " +
                    "adsoyad = '" + adsoyad + "',aciklama='" +
                    this.aciklama + "',mail='" + this.mail + "',telefon='" + this.telefon + "',resim ='" + this.resim + "',cinsiyet='" + radioCinsiyet + "'";
            linearBackClick();
            temizle();
        }

        database.execSQL(sqlGuncele);
        Toast.makeText(getApplicationContext(), adsoyad + " Kişisi Güncellendi.", Toast.LENGTH_LONG).show();
    }

    private void veriKaydet(String adsoyad, String aciklama, String mail, String telefon, String resim, String radioCinsiyet) {

        try {
            SQLiteDatabase database = this.openOrCreateDatabase("alzheimer", MODE_PRIVATE, null);

            String sqlTable = "CREATE TABLE IF NOT EXISTS tblBilgilerim (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "adsoyad TEXT, aciklama TEXT, mail TEXT, telefon TEXT, resim TEXT, enlem TEXT, boylam TEXT, cinsiyet TEXT)";
            database.execSQL(sqlTable);

            String sqlInsert = "INSERT INTO tblBilgilerim(adsoyad,aciklama,mail,telefon,resim,cinsiyet)" +
                    " VALUES('" + adsoyad + "','" + aciklama + "','" + mail + "','" + telefon + "','" + resim + "','" + radioCinsiyet + "')";
            database.execSQL(sqlInsert);

            database.close();

            Toast.makeText(getApplicationContext(), adsoyad + " Bilgileri oluşturuldu.", Toast.LENGTH_LONG).show();

            linearBackClick();
            temizle();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkButton(View v) {

        radioId = groupCinsiyet.getCheckedRadioButtonId();
        radioButton = findViewById(radioId);

        try {
            radioCinsiyet = radioButton.getText().toString();
        } catch (Exception e) {
            radioCinsiyet = "";
        }
    }

    private void linearBackClick() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void variableDesc() {

        txtAdsoyad = (TextView) findViewById(R.id.txtAdsoyad);
        txtAciklama = (TextView) findViewById(R.id.txtAciklama);
        txtKonum = (TextView) findViewById(R.id.txtKonum);
        txtMail = (TextView) findViewById(R.id.txtMail);
        txtTelefon = (TextView) findViewById(R.id.txtTelefon);

        image = (ImageView) findViewById(R.id.image);

        groupCinsiyet = (RadioGroup) findViewById(R.id.groupCinsiyet);
        radioErkek = (RadioButton) findViewById(R.id.radioErkek);
        radioKadin = (RadioButton) findViewById(R.id.radioKadin);

        relativeAdsoyad = (RelativeLayout) findViewById(R.id.relativeAdsoyad);
        relativeImage = (RelativeLayout) findViewById(R.id.relativeImage);
        relativeAciklama = (RelativeLayout) findViewById(R.id.relativeAciklama);
        relativeKonum = (RelativeLayout) findViewById(R.id.relativeKonum);
        relativeMail = (RelativeLayout) findViewById(R.id.relativeMail);
        relativeTelefon = (RelativeLayout) findViewById(R.id.relativeTelefon);
        relativeCinsiyet = (RelativeLayout) findViewById(R.id.relativeCinsiyet);

        linearBack = (LinearLayout) findViewById(R.id.linearBack);

        btnGonder = (Button) findViewById(R.id.btnGonder);

        radioCinsiyet = "";
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
            cursor.close();

            if (satirSayisi == 0) {
                btnGonder.setText("Oluştur");
            } else {
                btnGonder.setText("Güncelle");
            }

            database.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void transparanEkran() {
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
        relativeCinsiyet.setBackgroundColor(Color.BLACK);

    }

    public void temizle() {
        txtTelefon.setText("");
        txtAdsoyad.setText("");
        txtMail.setText("");
        txtAciklama.setText("");
        image.setImageResource(R.drawable.selection);
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
}
