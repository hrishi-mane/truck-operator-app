package com.example.tracktmtruckoperator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class UserInfoDb extends SQLiteOpenHelper {

    static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "user_info_table";

    private static final String DB_NAME = "USER_INFO_DB";

    private static final String ID = "USER_ID";

    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + ID
            + " VARCHAR);";

    public UserInfoDb(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insert(String user_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, user_id);
        db.insert(TABLE_NAME, null, contentValues);
    }

    public Cursor retrieve() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("Select " + ID +" from " + TABLE_NAME, null);
        return res;
    }

    public void delete(String user_doc_id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ID + "= ?", new String[] {user_doc_id});
    }

}
