package com.example.jgraham.kitabureg1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MySQLiteDbHelper extends SQLiteOpenHelper {
    // Database name string
    public static final String DATABASE_NAME = "Database_Kitabu";
    // Table schema, column names
    public static final String KEY_ROWID = "_id";    // RowID that is auto generated from the DB.
    public static final String KEY_ID = "id";       // ID generated from the web server.
    public static final String KEY_LINK = "link";   // URL.
    public static final String KEY_TITLE = "title"; // Title of the Link.
    public static final String KEY_TYPE = "type"; //public, private entries.
    public static final String KEY_TAGS = "tags"; //Tags based on the Link.
    public static final String KEY_PHNO = "phoneno";   // Phone number of the user.
    // Table name string. (Only one table)
    private static final String TABLE_NAME_ENTRIES = "Kitabu_Database_Table_Links";
    // Version code
    private static final int DATABASE_VERSION = 2;

    // SQL query to create the table for the first time
    // Data types are defined below
    private static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME_ENTRIES
            + " ("
            + KEY_ROWID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_ID
            + " INTEGER UNIQUE, "
            + KEY_LINK
            + " TEXT, "
            + KEY_TITLE
            + " TEXT, "
            + KEY_TYPE
            + " INTEGER, "
            + KEY_TAGS
            + " TEXT, "
            + KEY_PHNO
            + " INTEGER "
            + ");";
    private static final String[] mColumnList = new String[]{KEY_ROWID,
            KEY_ID, KEY_LINK, KEY_TITLE, KEY_TYPE,
            KEY_TAGS, KEY_PHNO};
    private static MySQLiteDbHelper sInstance;

    //Used on one object at any instance for database operations. Used Singleton Design pattern.
    public MySQLiteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DB created", "Database_Kitabu");
    }

    public static synchronized MySQLiteDbHelper getInstance(Context context) {
        // Using the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new MySQLiteDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create the table schema.
        db.execSQL(CREATE_TABLE_ENTRIES);
        Log.d("Kitabu Database", "Table Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade the database if the doesn't exist.
        Log.w(MySQLiteDbHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ENTRIES);
        onCreate(db);
    }

    // Insert a item given each column value
    public long insertEntry(KitabuEntry entry) {
        ContentValues value = new ContentValues();
        // put values in a ContentValues object
        value.put(KEY_ID, entry.getmId());
        value.put(KEY_LINK, entry.getmLink());
        value.put(KEY_TITLE, entry.getmTitle());
        value.put(KEY_TYPE, entry.getmType());
        value.put(KEY_TAGS, entry.getmTags());
        value.put(KEY_PHNO, entry.getmPhoneNo());
        // get a database object
        SQLiteDatabase dbObj = getWritableDatabase();
        long id;
        // insert the record
        id = dbObj.insert(TABLE_NAME_ENTRIES, null, value);
        dbObj.close();
        // return the primary key for the new record
        return id;
    }

    // Updating the MYSQL DB accordingly.
    public void updateEntry(int index) {
        SQLiteDatabase dbObj = getWritableDatabase();
        dbObj.rawQuery("update " + TABLE_NAME_ENTRIES + " " +
                "set type = '" + 2 + "' WHERE id = '" + index + "';", null);
        dbObj.close();
    }


    // Remove a entry by giving its index
    public void removeEntry(int rowIndex) {
        Log.d("Entered to delete", "Delete record");
        SQLiteDatabase dbObj = getWritableDatabase();
        int temp = dbObj.delete(TABLE_NAME_ENTRIES, KEY_ID + "=" + rowIndex, null);
        Log.d("deleted records", String.valueOf(temp));
        dbObj.close();
    }

    // Query a specific entry by ID that is generated from webserver. Return a cursor having each column.
    public KitabuEntry fetchEntryByIndex(int id) throws SQLException {
        SQLiteDatabase dbObj = getReadableDatabase();
        KitabuEntry entry = null;
        // do the query with the condition KEY_ROWID = rowId
        Cursor cursor = dbObj.query(true, TABLE_NAME_ENTRIES, mColumnList,
                KEY_ID + "=" + id, null, null, null, null, null);
        // move the cursor to the first record
        if (cursor.moveToFirst()) {
            // convert the cursor to an KitabuEntry object
            entry = cursorToEntry(cursor, true);
        }
        // close the cursor
        cursor.close();
        dbObj.close();
        return entry;
    }

    // Query the entire table, return all rows
    public ArrayList<KitabuEntry> fetchEntries() {
        SQLiteDatabase dbObj = getReadableDatabase();
        // store all the entries to an ArrayList
        ArrayList<KitabuEntry> entryList = new ArrayList<>();
        // do the query without any condition. it retrieves every record from
        // the database
        Cursor cursor = dbObj.query(TABLE_NAME_ENTRIES, mColumnList, null,
                null, null, null, null);
        // the cursor initially points the record PRIOR to the first record
        // use the while loop to read every record from the cursor
        while (cursor.moveToNext()) {
            KitabuEntry entry = cursorToEntry(cursor, false);
            entryList.add(entry);
        }
        cursor.close();
        dbObj.close();
        return entryList;
    }

    // Query the entire table, return all private rows
    public ArrayList<KitabuEntry> fetchPrivateEntries() {
        Log.d("Fetch Private Entries", "Try to do that");
        SQLiteDatabase dbObj = getReadableDatabase();
        int res = 1;
        // store all the entries to an ArrayList
        ArrayList<KitabuEntry> entryList = new ArrayList<>();
        // do the query without any condition. it retrieves all private record from
        // the database
        Cursor cursor = dbObj.query(TABLE_NAME_ENTRIES, mColumnList,
                KEY_TYPE + "=" + "0", null, null, null, KEY_ID + " DESC ", "20");

        // the cursor initially points the record PRIOR to the first record
        // use the while loop to read every record from the cursor
        while (cursor.moveToNext()) {
            res++;
            KitabuEntry entry = cursorToEntry(cursor, false);
            entryList.add(entry);
        }
        cursor.close();
        return entryList;
    }

    // Deleting all private entries
    public void deleteallprivate() {
        SQLiteDatabase dbObj = getWritableDatabase();
        dbObj.rawQuery("delete from " + TABLE_NAME_ENTRIES + " where type=0", null);
    }

    // Query the entire table, return all public rows only the top 20 rows.
    public ArrayList<KitabuEntry> fetchPublicEntries() {
        Log.d("Fetch Public Entries", "Try to do that");
        int res = 1;
        SQLiteDatabase dbObj = getReadableDatabase();
        // store all the entries to an ArrayList
        ArrayList<KitabuEntry> entryList = new ArrayList<>();
        // do the query without any condition. it retrieves every record from
        // the database
        Cursor cursor = dbObj.query(TABLE_NAME_ENTRIES, mColumnList,
                KEY_TYPE + "=" + "1", null, null, null, KEY_ID + " DESC ", "20");
        // the cursor initially points the record PRIOR to the first record
        // use the while loop to read every record from the cursor
        while (cursor.moveToNext()) {
            res++;
            KitabuEntry entry = cursorToEntry(cursor, false);
            entryList.add(entry);
            // Log.d("TAGG", "Got data");
        }
        cursor.close();
        return entryList;
    }

    // Query the entire table, return all notification rows but only the top 20.
    public ArrayList<KitabuEntry> fetchNotificationEntries() throws SQLException {
        Log.d("Fetch Notifications", "Try to do that");
        SQLiteDatabase dbObj = getReadableDatabase();
        int res = 1;
        // store all the entries to an ArrayList
        ArrayList<KitabuEntry> entryList = new ArrayList<>();
        // do the query without any condition. it retrieves every record from
        // the database
        Cursor cursor = dbObj.query(TABLE_NAME_ENTRIES, mColumnList,
                KEY_TYPE + "=" + "2", null, null, null, KEY_ID + " DESC ", "20");
        // the cursor initially points the record PRIOR to the first record
        // use the while loop to read every record from the cursor
        while (cursor.moveToNext()) {
            KitabuEntry entry = cursorToEntry(cursor, false);
            entryList.add(entry);
        }
        cursor.close();
        return entryList;
    }


    // convert the a row in the cursor to an KitabuEntry object
    private KitabuEntry cursorToEntry(Cursor cursor, boolean needGps) {
        KitabuEntry entry = new KitabuEntry();
        entry.setmRowID(cursor.getLong(cursor.getColumnIndex(KEY_ROWID)));
        entry.setmId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        entry.setmLink(cursor.getString(cursor.getColumnIndex(KEY_LINK)));
        entry.setmTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
        entry.setmType(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
        entry.setmTags(cursor.getString(cursor.getColumnIndex(KEY_TAGS)));
        entry.setmPhoneNo(cursor.getLong(cursor.getColumnIndex(KEY_PHNO)));
        return entry;
    }

    // Query a specific entry by its link. Return a cursor having each column
    // value
    public KitabuEntry fetchEntryByTitle(String title) throws SQLException {
        SQLiteDatabase dbObj = getReadableDatabase();
        KitabuEntry entry = null;
        // do the query with the condition KEY_ROWID = rowId
        Cursor cursor = dbObj.query(true, TABLE_NAME_ENTRIES, mColumnList,
                KEY_LINK + "= \"" + title + "\"", null, null, null, null, null);
        // move the cursor to the first record
        if (cursor.moveToFirst()) {
            // convert the cursor to an KitabuEntry object
            entry = cursorToEntry(cursor, true);
        }
        // close the cursor
        cursor.close();
        dbObj.close();

        return entry;
    }

    // Query the entire table, to return the last inserted id
    public int getLastId() throws SQLException {

        int temp = -1;
        SQLiteDatabase dbObj = getReadableDatabase();
        KitabuEntry entry = null;
        Cursor cursor = dbObj.query(true, TABLE_NAME_ENTRIES, mColumnList,
                null, null, null, null, KEY_ID + " DESC ", null);
        // move the cursor to the first record
        if (cursor.moveToFirst()) {
            entry = cursorToEntry(cursor, true);
            temp = entry.getmId();
//            Log.d("Entered into cursor",String.valueOf(temp));
            return temp;
        }
        cursor.close();
        dbObj.close();

        return temp;
    }

    //    Methd to return the last twenty records: will be used for debugging
    public List<KitabuEntry> getLastTwenty() throws SQLException {

        List<KitabuEntry> list = new ArrayList<>();
        int res = 1;
        SQLiteDatabase dbObj = getReadableDatabase();
        KitabuEntry entry = null;
        Cursor cursor = dbObj.query(true, TABLE_NAME_ENTRIES, mColumnList,
                KEY_TYPE + "=" + "1", null, null, null, KEY_ID + " DESC ", "20");
        // move the cursor to the first record
        while (cursor.moveToNext() && res <= 20) {
            res++;
            entry = cursorToEntry(cursor, true);
            list.add(entry);
        }
        cursor.close();
        dbObj.close();
        return list;
    }

}


