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

package me.acristoffers.tracker.backend;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Store extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "packageTracker";

    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_PACKAGES = "packages";
    private static final String TABLE_STEPS = "steps";

    private static final String KEY_ID = "id";

    private static final String KEY_NAME = "name";
    private static final String KEY_COD = "cod";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_TIME_CREATED = "creation_time";
    private static final String KEY_TIME_UPDATED = "update_time";

    private static final String KEY_DATE = "date";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_LOCAL = "local";
    private static final String KEY_PACKAGE = "package";

    private Context context;

    public Store(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static List<String> getCodes(Context context) {
        Store store = new Store(context);
        return store.getCodes();
    }

    public List<String> getCodes() {
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
    public void onCreate(SQLiteDatabase db) {
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
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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

    public Date getTimeUpdated(String cod) {
        Date date = new Date();

        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            Cursor cursor = db.query(TABLE_PACKAGES, new String[]{KEY_TIME_UPDATED}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                date = new Date(cursor.getInt(cursor.getColumnIndex(KEY_TIME_UPDATED)));
            }

            db.close();
        }

        return date;
    }

    public Date getTimeCreated(String cod) {
        Date date = new Date();

        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            Cursor cursor = db.query(TABLE_PACKAGES, new String[]{KEY_TIME_CREATED}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                date = new Date(cursor.getInt(cursor.getColumnIndex(KEY_TIME_CREATED)));
            }

            db.close();
        }

        return date;
    }

    public boolean getActive(String cod) {
        boolean active = true;

        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            Cursor cursor = db.query(TABLE_PACKAGES, new String[]{KEY_ACTIVE}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                active = cursor.getInt(cursor.getColumnIndex(KEY_ACTIVE)) == 1;
            }

            db.close();
        }

        return active;
    }

    public int getId(String cod) {
        int id = -1;

        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            Cursor cursor = db.query(TABLE_PACKAGES, new String[]{KEY_ID}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
            }

            db.close();
        }

        return id;
    }

    public String getName(String cod) {
        String name = "";

        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            Cursor cursor = db.query(TABLE_PACKAGES, new String[]{KEY_NAME}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
            }

            db.close();
        }

        return name;
    }

    public void updatePackage(Package pkg) {
        SQLiteDatabase db = this.getWritableDatabase();

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

    public void insertPackage(Package pkg) {
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

    public void removePackage(Package pkg) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (db != null) {
            String cod = pkg.getCod();

            db.delete(TABLE_STEPS, KEY_PACKAGE + "=?", new String[]{cod});
            db.delete(TABLE_PACKAGES, KEY_COD + "=?", new String[]{cod});

            db.close();
        }
    }

    public List<Correios.Step> getSteps(String cod) {
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

}
