package eu.lucazanini.arpav.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import eu.lucazanini.arpav.database.TownContract.TownEntry;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.location.TownList;

public class TownDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "location.db";
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
    private final static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();
    private Context context;
    private volatile SQLiteDatabase db;

    public TownDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        writeLock.lock();
        try {
            db.execSQL(SQL_CREATE_ENTRIES);
            TownList towns = TownList.getInstance(context);
            save(db, towns.getTowns());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        writeLock.lock();
        try {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        } finally {
            writeLock.unlock();
        }
    }

    public void open() throws SQLException {
        if (db == null || !db.isOpen()) {
            writeLock.lock();
            try {
                db = getWritableDatabase();
            } finally {
                writeLock.unlock();
            }
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            writeLock.lock();
            try {
                db.close();
            } finally {
                writeLock.unlock();
            }
        }
    }

    private void save(SQLiteDatabase db, List<Town> towns) {
        for (Town town : towns) {
            ContentValues values = new ContentValues();
            values.put(TownEntry.COL_NAME, town.getName());
            values.put(TownEntry.COL_ZONE, town.getZone());
            values.put(TownEntry.COL_PROVINCE, town.getProvince().toString());
            values.put(TownEntry.COL_LONGITUDE, town.getLongitude());
            values.put(TownEntry.COL_LATITUDE, town.getLatitude());

            db.insert(TownEntry.TABLE_NAME, null, values);
        }
    }

    private Cursor getTowns() {

        Cursor cursor = null;

        readLock.lock();

        try {
            String[] projection = {TownEntry.COL_NAME};
            String sortOrder = TownEntry.COL_NAME + " ASC";
            cursor = db.query(TownEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);
        } finally {
            readLock.unlock();
        }
        return cursor;
    }

    public List<String> getTownNames() {
        Cursor cursor = getTowns();

        List<String> townNames = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            townNames.add(cursor.getString(cursor.getColumnIndex(TownEntry.COL_NAME)));
            cursor.moveToNext();
        }

        cursor.close();

        return townNames;
    }

    public List<String> getTownNames(String like) {
        Cursor cursor = getTowns(like);

        List<String> townNames = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            townNames.add(cursor.getString(cursor.getColumnIndex(TownEntry.COL_NAME)));
            cursor.moveToNext();
        }

        cursor.close();

        return townNames;
    }

    private Cursor getTowns(String like) {

        Cursor cursor = null;

        readLock.lock();

        try {

            String[] projection = {TownEntry.COL_NAME};

            String selection = TownEntry.COL_NAME + " LIKE ?";

            String[] selectionArgs = new String[]{like + "%"};

            String sortOrder = TownEntry.COL_NAME + " ASC";

            cursor = db.query(
                    TownEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        } finally {
            readLock.unlock();
        }
        return cursor;
    }
}
