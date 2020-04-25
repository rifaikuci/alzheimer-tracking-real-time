package com.rifaikuci.alzheimer_tracking;

public class ModelKisiler {

    private String adSoyad;
    private String desc;
    private int image;

    public ModelKisiler(String adSoyad, String desc, int image) {
        this.adSoyad = adSoyad;
        this.desc = desc;
        this.image = image;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    public void setAdSoyad(String adSoyad) {
        this.adSoyad = adSoyad;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
