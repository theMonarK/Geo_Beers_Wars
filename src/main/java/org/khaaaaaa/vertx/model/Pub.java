package org.khaaaaaa.vertx.model;

/**
 * Created by anthony on 07/06/2016.
 */
public class Pub {

    private int id;

    public int getId() {
        return id;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getIcon() {
        return icon;
    }

    public Pub(int id, float latitude, float longitude, String icon) {

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.icon = icon;
    }

    private float latitude;
    private float longitude;
    private String icon;
}
