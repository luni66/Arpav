package eu.lucazanini.arpav.location;

import android.content.Context;
import android.database.MatrixCursor;
import android.location.Location;
import android.support.v4.widget.SimpleCursorAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by luke on 02/12/16.
 */

public class Town implements TownLocation {

    protected String name;
    protected int zone;
    protected double latitude, longitude;
    protected Province province;

    public Town(String name, Province province, int zone, double latitude, double longitude) {
        this.name = name;
        this.zone = zone;
        this.province = province;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Town(String name, Province province, int zone) {
        this.name = name;
        this.province = province;
        this.zone = zone;
    }

    public Town(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public float distanceTo(TownLocation dest) {

        final double R = 6371000; // metres

        double phi1 = Math.toRadians(latitude);
        double phi2 = Math.toRadians(dest.getLatitude());
        double deltaPhi = Math.toRadians(dest.getLatitude() - latitude);
        double deltaLambda = Math.toRadians(dest.getLongitude() - longitude);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double d = R * c;

        return (float) d;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getZone() {
        return zone;
    }

    @Override
    public void setZone(int zone) {
        this.zone = zone;
    }

    @Override
    public Province getProvince() {
        return province;
    }

    @Override
    public void setProvince(Province province) {
        this.province = province;
    }

/*    public static String[] getNames(Context context) {
        List<Town> towns = loadTowns(context);
        String[] names = new String[towns.size()];

        int i=0;
        for (Town town: towns) {
            names[i++]=town.getName();
        }

        return names;
    }*/

/*    public static List<Town> loadTowns(Context context){

        List towns = new ArrayList();

        String[] paths = new String[]{"belluno.json", "padova.json", "rovigo.json", "venezia.json", "verona.json", "vicenza.json"};

        String json = null;
        try {

            for (String path:paths
                 ) {
                List townsPr = new ArrayList();
                InputStream is = context.getAssets().open(path);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");

                Gson gson = new Gson();
                Type fooType = new TypeToken<List<Town>>() {
                }.getType();

                townsPr = gson.fromJson(json, fooType);

                towns.addAll(townsPr);
            }

//            towns.sort(new AlphabeticComparator());

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }


        return towns;
    }*/

    public static class NameComparator implements Comparator<Town> {

        @Override
        public int compare(Town o1, Town o2) {
            int result;
            result = o1.getName().compareTo(o2.getName());
            return result;
        }
    }

    public static class DistanceComparator implements Comparator<Town> {

        private Town center;

        public DistanceComparator(Town center) {
            this.center = center;
        }

        @Override
        public int compare(Town o1, Town o2) {
            int result;
            result = (int) (center.distanceTo(o1) - center.distanceTo(o2));
            return result;
        }
    }

}
