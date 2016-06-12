package org.khaaaaaa.vertx.model;

/**
 * Created by anthony on 07/06/2016.
 */
public class Pub {

    private int id;
    private float latitude;
    private float longitude;
    private String icon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
