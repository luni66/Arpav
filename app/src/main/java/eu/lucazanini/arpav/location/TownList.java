package eu.lucazanini.arpav.location;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import eu.lucazanini.arpav.database.TownDbHelper;
import eu.lucazanini.arpav.database.TownContract.TownEntry;

public class TownList {

    private static TownList instance;
    private Context context;
    private List<Town> towns;

    protected TownList() {}

    protected TownList(Context context) {
        this.context = context;
        loadTowns();
    }

    public synchronized static TownList getInstance(Context context) {
        if (instance == null) {
            instance = new TownList(context);
        }
        return instance;
    }

    public List<Town> getTowns() {
        return towns;
    }

    public Town getTown(String name) {
        for (Town town : towns) {
            if (town.getName().equals(name))
                return town;
        }
        return null;
    }

    public String[] getNames() {
        String[] names = new String[towns.size()];

        int i = 0;
        for (Town town : towns) {
            names[i++] = town.getName();
        }

        return names;
    }

    public List<Town> loadTowns() {

        towns = new ArrayList();

        String[] paths = new String[]{"belluno.json", "padova.json", "rovigo.json", "treviso.json", "venezia.json", "verona.json", "vicenza.json"};

        String json = null;
        try {

            for (String path : paths
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

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return towns;
    }

    public Town getTown(double latitude, double longitude){
        towns.sort(new Town.GpsDistanceComparator(latitude, longitude));
        return towns.get(0);
    }

//    public void save(){
//        TownDbHelper townDbHelper = new TownDbHelper(context);
//        townDbHelper.open();
//        townDbHelper.save(towns);
//    }

}
