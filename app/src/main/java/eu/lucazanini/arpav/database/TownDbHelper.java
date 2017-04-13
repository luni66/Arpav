package eu.lucazanini.arpav.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import eu.lucazanini.arpav.database.TownContract.TownEntry;
import eu.lucazanini.arpav.location.TownList;

public class TownDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "location.db";
    private Context context;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TownEntry.TABLE_NAME + " (" +
                    TownEntry._ID + " INTEGER PRIMARY KEY," +
                    TownEntry.COL_NAME + " TEXT," +
                    TownEntry.COL_PROVINCE + " TEXT," +
                    TownEntry.COL_ZONE + " TEXT," +
                    TownEntry.COL_LONGITUDE + " REAL," +
                    TownEntry.COL_LATITUDE + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TownEntry.TABLE_NAME;

    public TownDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);

        TownList towns = TownList.getInstance(context);
        towns.save();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public Cursor getTowns(){

        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {TownEntry.COL_NAME};

        String sortOrder = TownEntry.COL_NAME + " ASC";

        Cursor cursor = db.query(
                TownEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return cursor;
    }
}
