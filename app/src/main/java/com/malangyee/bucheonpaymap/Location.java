package com.malangyee.bucheonpaymap;

public class Location {
    private String name, dong, address;
    private double lng, lat;
    private int _no;

    public Location() {
    }

    public Location(int _no, String name, String dong, String address, double lng, double lat) {
        this._no = _no;
        this.name = name;
        this.dong = dong;
        this.address = address;
        this.lng = lng;
        this.lat = lat;
    }

    public int get_no() {
        return _no;
    }

    public void set_no(int _no) {
        this._no = _no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDong() {
        return dong;
    }

    public void setDong(String dong) {
        this.dong = dong;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
