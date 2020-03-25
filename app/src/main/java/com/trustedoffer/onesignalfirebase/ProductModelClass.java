package com.trustedoffer.onesignalfirebase;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

public class ProductModelClass {
    private String title;
    private int price;
    private String image;
    private String key;


    public ProductModelClass() {

    }

    public ProductModelClass(String key,String title, int price,String image) {
        this.key=key;
        this.title = title;
        this.price = price;
        this.image=image;
    }
    public String getKey() {
        return key;
    }
    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }
    public String getImage() {
        return image;
    }

}
