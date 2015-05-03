/*
 * Copyright (c) 2014 Álan Crístoffer
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package me.acristoffers.tracker;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Store extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "packageTracker";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_COD = "cod";
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_TIME_CREATED = "creation_time";
    public static final String KEY_TIME_UPDATED = "update_time";
    public static final String KEY_DATE = "date";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_LOCAL = "local";
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_STEPS = "steps";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_PACKAGES = "packages";
    private static final String TABLE_STEPS = "steps";
    private Context context;

    public Store(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public synchronized List<String> allCodes() {
        List<String> codes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        if (db != null) {
            Cursor cursor = db.query(TABLE_PACKAGES, new String[]{KEY_COD}, null, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    codes.add(cursor.getString(cursor.getColumnIndex(KEY_COD)));
                }
            }

            db.close();
        }

        return codes;
    }

    @Override
    public synchronized void onCreate(SQLiteDatabase db) {
        final String CREATE_PACKAGES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PACKAGES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_COD + " TEXT,"
                + KEY_ACTIVE + " INT)";
        db.execSQL(CREATE_PACKAGES_TABLE);

        final String CREATE_STEPS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STEPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_DATE + " INTEGER,"
                + KEY_TITLE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_LOCAL + " TEXT,"
                + KEY_PACKAGE + " TEXT)";
        db.execSQL(CREATE_STEPS_TABLE);

        onUpgrade(db, 1, DATABASE_VERSION);
    }

    @Override
    public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql;
        ContentValues values;

        switch (oldVersion) {
            case 1:
                sql = "ALTER TABLE " + TABLE_PACKAGES + " ADD COLUMN " + KEY_TIME_CREATED + " INT";
                db.execSQL(sql);
                sql = "ALTER TABLE " + TABLE_PACKAGES + " ADD COLUMN " + KEY_TIME_UPDATED + " INT";
                db.execSQL(sql);
                values = new ContentValues();
                values.put(KEY_TIME_CREATED, new Date().getTime());
                values.put(KEY_TIME_UPDATED, new Date().getTime());
                db.update(TABLE_PACKAGES, values, null, null);
        }
    }

    public synchronized Map<String, Object> getPackage(String cod) {
        Map<String, Object> pkg = new HashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            int id = -1;
            String name = "";
            boolean active = false;
            Date timeCreated = new Date();
            Date timeUpdated = new Date();

            Cursor cursor = db.query(TABLE_PACKAGES, new String[]{KEY_ID}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
            }

            cursor = db.query(TABLE_PACKAGES, new String[]{KEY_NAME}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
            }

            cursor = db.query(TABLE_PACKAGES, new String[]{KEY_ACTIVE}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                active = cursor.getInt(cursor.getColumnIndex(KEY_ACTIVE)) == 1;
            }

            cursor = db.query(TABLE_PACKAGES, new String[]{KEY_TIME_CREATED}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                timeCreated = new Date(cursor.getInt(cursor.getColumnIndex(KEY_TIME_CREATED)));
            }

            cursor = db.query(TABLE_PACKAGES, new String[]{KEY_TIME_UPDATED}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                timeUpdated = new Date(cursor.getInt(cursor.getColumnIndex(KEY_TIME_UPDATED)));
            }

            pkg.put(KEY_ID, id);
            pkg.put(KEY_NAME, name);
            pkg.put(KEY_ACTIVE, active);
            pkg.put(KEY_TIME_CREATED, timeCreated);
            pkg.put(KEY_TIME_UPDATED, timeUpdated);

            db.close();
        }

        return pkg;
    }

    public synchronized List<Correios.Step> getSteps(String cod) {
        List<Correios.Step> steps = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            Cursor cursor = db.query(TABLE_STEPS, new String[]{KEY_DATE, KEY_TITLE, KEY_DESCRIPTION, KEY_LOCAL}, KEY_PACKAGE + "=?", new String[]{cod}, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Correios.Step step = new Correios.Step();

                    step.title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
                    step.description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
                    step.local = cursor.getString(cursor.getColumnIndex(KEY_LOCAL));
                    step.date = new Date(cursor.getLong(cursor.getColumnIndex(KEY_DATE)));

                    steps.add(step);
                } while (cursor.moveToNext());
            }

            db.close();
        }

        return steps;
    }

    public synchronized void updatePackage(Package pkg) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (pkg.getName().isEmpty() || pkg.getCod().isEmpty()) {
            return;
        }

        if (db != null) {
            Cursor cursor = db.query(TABLE_PACKAGES, new String[]{KEY_ID}, KEY_COD + "=?", new String[]{pkg.getCod()}, null, null, null, null);
            if (cursor.getCount() == 0) {
                db.close();
                insertPackage(pkg);
                return;
            }

            String cod = pkg.getCod();
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, pkg.getName());
            values.put(KEY_ACTIVE, pkg.isActive());
            values.put(KEY_TIME_UPDATED, new Date().getTime());

            db.update(TABLE_PACKAGES, values, KEY_COD + "=?", new String[]{cod});
            db.delete(TABLE_STEPS, KEY_PACKAGE + "=?", new String[]{cod});

            List<Correios.Step> steps = pkg.getSteps();
            for (Correios.Step step : steps) {
                values = new ContentValues();

                values.put(KEY_TITLE, step.title);
                values.put(KEY_DESCRIPTION, step.description);
                values.put(KEY_LOCAL, step.local);
                values.put(KEY_DATE, step.date.getTime());
                values.put(KEY_PACKAGE, pkg.getCod());

                db.insert(TABLE_STEPS, null, values);
            }

            db.close();
        }
    }

    public synchronized void insertPackage(Package pkg) {
        List<Correios.Step> steps = pkg.getSteps();
        SQLiteDatabase db = this.getWritableDatabase();

        if (db != null) {
            ContentValues values = new ContentValues();

            values.put(KEY_NAME, pkg.getName());
            values.put(KEY_COD, pkg.getCod());
            values.put(KEY_ACTIVE, pkg.isActive());
            values.put(KEY_TIME_CREATED, new Date().getTime());
            values.put(KEY_TIME_UPDATED, new Date().getTime());

            db.insert(TABLE_PACKAGES, null, values);

            for (Correios.Step step : steps) {
                values = new ContentValues();

                values.put(KEY_TITLE, step.title);
                values.put(KEY_DESCRIPTION, step.description);
                values.put(KEY_LOCAL, step.local);
                values.put(KEY_DATE, step.date.getTime());
                values.put(KEY_PACKAGE, pkg.getCod());

                db.insert(TABLE_STEPS, null, values);
            }

            db.close();
        }

        if (!pkg.getName().isEmpty()) {
            BackupManager bm = new BackupManager(context);
            bm.dataChanged();
        }
    }

    public synchronized void removePackage(Package pkg) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (db != null) {
            String cod = pkg.getCod();

            db.delete(TABLE_STEPS, KEY_PACKAGE + "=?", new String[]{cod});
            db.delete(TABLE_PACKAGES, KEY_COD + "=?", new String[]{cod});

            db.close();
        }
    }

}
