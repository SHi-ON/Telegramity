package org.gramity.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.telegram.messenger.FileLog;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "favourites";

    private static final String TABLE_FAVS = "tbl_favs";
    private static final String TABLE_BTNS = "tbl_btns";

    private static final String KEY_ID = "id";
    private static final String KEY_CHAT_ID = "chat_id";

    private static final String CREATE_FAVS_TABLE = "CREATE TABLE " + TABLE_FAVS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_CHAT_ID + " INTEGER" + ")";

    private static final String CREATE_BTNS_TABLE = "CREATE TABLE " + TABLE_BTNS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_CHAT_ID + " INTEGER" + ")";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FAVS_TABLE);
        db.execSQL(CREATE_BTNS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BTNS);
        onCreate(db);
    }

    public void addFavourite(Favourite favourite) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CHAT_ID, favourite.getChatID());
        db.insert(TABLE_FAVS, null, values);
        db.close();
    }

    public void addBeitreten(Beitreten beitreten) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CHAT_ID, beitreten.getChatID());
        db.insert(TABLE_BTNS, null, values);
        db.close();
    }

    public Favourite getFavouriteByChatId(long chat_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String[] projection = {KEY_ID, KEY_CHAT_ID};
            String whereClause = KEY_CHAT_ID + "=?";
            String[] whereArgs = {String.valueOf(chat_id)};

            cursor = db.query(TABLE_FAVS, projection, whereClause, whereArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return new Favourite(cursor.getLong(1));
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            FileLog.e("tmessages", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public Beitreten getBeitretenByChatId(long chat_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String[] projection = {KEY_ID, KEY_CHAT_ID};
            String whereClause = KEY_CHAT_ID + "=?";
            String[] whereArgs = {String.valueOf(chat_id)};

            cursor = db.query(TABLE_BTNS, projection, whereClause, whereArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return new Beitreten(cursor.getLong(1));
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            FileLog.e("tmessages", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public void deleteFavourite(Long chat_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVS, KEY_CHAT_ID + " = ?", new String[]{String.valueOf(chat_id)});
        db.close();
    }

    public void deleteBeitreten(Long chat_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BTNS, KEY_CHAT_ID + " = ?", new String[]{String.valueOf(chat_id)});
        db.close();
    }

    /*public List<Favourite> getAllFavourites() {
        List<Favourite> favsList = new ArrayList<Favourite>();

        String selectQuery = "SELECT  * FROM " + TABLE_FAVS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Favourite favourite = new Favourite();
                favourite.setID(Integer.parseInt(cursor.getString(0)));
                favourite.setChatID(cursor.getLong(1));

                favsList.add(favourite);
            } while (cursor.moveToNext());
        }

        return favsList;
    }

    public int getFavouritesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_FAVS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }*/

    // Database Manager
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
    //
}
