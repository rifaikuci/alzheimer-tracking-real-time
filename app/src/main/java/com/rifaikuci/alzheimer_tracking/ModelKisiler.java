package com.rifaikuci.alzheimer_tracking;

public class ModelKisiler {

    private String adSoyad;
    private String aciklama;
    private String telefon;
    private String mail;
    private String resim;
    private int id;

    public ModelKisiler(String adSoyad, String telefon, String mail) {
        this.adSoyad = adSoyad;
        this.telefon = telefon;
        this.mail = mail;
    }

    public ModelKisiler(int id, String adSoyad, String aciklama, String resim) {
        this.adSoyad = adSoyad;
        this.aciklama = aciklama;
        this.resim = resim;
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    public void setAdSoyad(String adSoyad) {
        this.adSoyad = adSoyad;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getResim() {
        return resim;
    }

    public void setResim(String resim) {
        this.resim = resim;
    }
}
