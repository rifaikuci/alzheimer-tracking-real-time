package com.rifaikuci.alzheimer_tracking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class Adapter extends PagerAdapter {

    private List<ModelKisiler> models;
    private LayoutInflater layoutInflater;
    private Context context;

    public Adapter(List<ModelKisiler> models, Context context) {
        this.models = models;
        this.context = context;
    }


    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item, container, false);

        ImageView image;
        TextView adSoyad, desc;

        image = (ImageView) view.findViewById(R.id.image);
        adSoyad = (TextView) view.findViewById(R.id.adSoyad);
        desc = (TextView) view.findViewById(R.id.desc);

        image.setImageResource(models.get(position).getImage());
        adSoyad.setText(models.get(position).getAdSoyad());
        desc.setText(models.get(position).getDesc());

        container.addView(view, 0);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
