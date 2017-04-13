package eu.lucazanini.arpav.database;

import android.provider.BaseColumns;

/**
 * SQL schema for town
 */

public final class TownContract {

    private TownContract(){}

    public static class TownEntry implements BaseColumns{
        public static final String TABLE_NAME = "towns";
        public static final String COL_NAME = "name";
        public static final String COL_ZONE = "zone";
        public static final String COL_PROVINCE = "province";
        public static final String COL_LATITUDE = "latitude";
        public static final String COL_LONGITUDE = "longitude";
    }

}
