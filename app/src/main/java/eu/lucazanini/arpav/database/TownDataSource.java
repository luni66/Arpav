package eu.lucazanini.arpav.database;

import android.content.Context;

import java.util.List;

public class TownDataSource {

    private TownDbHelper townDbHelper;
//    private SQLiteDatabase db;

    public TownDataSource(Context context) {
        townDbHelper = TownDbHelper.getInstance(context);
    }

    public void open() {
        townDbHelper.open();
    }

    public void close() {
        townDbHelper.close();
    }

    public List<String> getTownNames() {
        return townDbHelper.getTownNames();
    }

    public List<String> getTownNames(String like) {
        return townDbHelper.getTownNames(like);
    }

}
