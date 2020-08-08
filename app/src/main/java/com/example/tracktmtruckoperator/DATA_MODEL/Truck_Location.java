package com.example.tracktmtruckoperator.DATA_MODEL;

import com.google.firebase.firestore.GeoPoint;

public class Truck_Location {

    public GeoPoint geoPoint;

    public String plant_id;

    public String vehicle_number;

    public Truck_Location(){

    }

    public Truck_Location(GeoPoint var_geoPoint, String var_plant_id, String var_vehicle_number) {
        this.geoPoint = var_geoPoint;
        this.plant_id = var_plant_id;
        this.vehicle_number = var_vehicle_number;
    }


}
