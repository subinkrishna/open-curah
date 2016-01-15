package com.subinkrishna.curah.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by subin on 8/7/14.
 */
public class CurahContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selectionClause,
                        String[] selectionArgs,
                        String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return "";
    }

    @Override
    public Uri insert(Uri uri,
                      ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri,
                      String selectionClause,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri,
                      ContentValues contentValues,
                      String selectionClause,
                      String[] selectionArgs) {
        return 0;
    }

}
