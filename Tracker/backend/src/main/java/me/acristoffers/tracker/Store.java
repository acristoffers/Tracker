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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

class Store extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "packageTracker";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_TIME_CREATED = "creation_time";
    public static final String KEY_TIME_UPDATED = "update_time";
    private static final String KEY_COD = "cod";
    private static final String KEY_DATE = "date";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_LOCAL = "local";
    private static final String KEY_PACKAGE = "package";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_PACKAGES = "packages";
    private static final String TABLE_STEPS = "steps";
    private final WeakReference<Context> context;

    public Store(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = new WeakReference<>(context);
    }

    public synchronized ArrayList<String> allCodes() {
        final ArrayList<String> codes = new ArrayList<>();
        final SQLiteDatabase db = this.getReadableDatabase();

        if (db != null) {
            final Cursor cursor = db.query(TABLE_PACKAGES, new String[]{KEY_COD}, null, null, null, null, null, null);

            while (cursor.moveToNext()) {
                codes.add(cursor.getString(cursor.getColumnIndex(KEY_COD)));
            }

            cursor.close();
            db.close();
        }

        return codes;
    }

    @Override
    public synchronized void onCreate(final SQLiteDatabase db) {
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
    public synchronized void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        final ContentValues values;

        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE " + TABLE_PACKAGES + " ADD COLUMN " + KEY_TIME_CREATED + " INT");
                db.execSQL("ALTER TABLE " + TABLE_PACKAGES + " ADD COLUMN " + KEY_TIME_UPDATED + " INT");

                values = new ContentValues();
                values.put(KEY_TIME_CREATED, new Date().getTime());
                values.put(KEY_TIME_UPDATED, new Date().getTime());

                db.update(TABLE_PACKAGES, values, null, null);
        }
    }

    public synchronized HashMap<String, Object> getPackage(final String cod) {
        final HashMap<String, Object> pkg = new HashMap<>();

        final SQLiteDatabase db = this.getReadableDatabase();
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
            cursor.close();

            cursor = db.query(TABLE_PACKAGES, new String[]{KEY_NAME}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
            }
            cursor.close();

            cursor = db.query(TABLE_PACKAGES, new String[]{KEY_ACTIVE}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                active = cursor.getInt(cursor.getColumnIndex(KEY_ACTIVE)) == 1;
            }
            cursor.close();

            cursor = db.query(TABLE_PACKAGES, new String[]{KEY_TIME_CREATED}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                timeCreated = new Date(cursor.getInt(cursor.getColumnIndex(KEY_TIME_CREATED)));
            }
            cursor.close();

            cursor = db.query(TABLE_PACKAGES, new String[]{KEY_TIME_UPDATED}, KEY_COD + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                timeUpdated = new Date(cursor.getInt(cursor.getColumnIndex(KEY_TIME_UPDATED)));
            }
            cursor.close();

            pkg.put(KEY_ID, id);
            pkg.put(KEY_NAME, name);
            pkg.put(KEY_ACTIVE, active);
            pkg.put(KEY_TIME_CREATED, timeCreated);
            pkg.put(KEY_TIME_UPDATED, timeUpdated);

            db.close();
        }

        return pkg;
    }

    public synchronized ArrayList<Correios.Step> getSteps(final String cod) {
        final ArrayList<Correios.Step> steps = new ArrayList<>();

        final SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            final Cursor cursor = db.query(TABLE_STEPS, new String[]{KEY_DATE, KEY_TITLE, KEY_DESCRIPTION, KEY_LOCAL}, KEY_PACKAGE + "=?", new String[]{cod}, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    final Correios.Step step = new Correios.Step();

                    step.title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
                    step.description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
                    step.local = cursor.getString(cursor.getColumnIndex(KEY_LOCAL));
                    step.date = new Date(cursor.getLong(cursor.getColumnIndex(KEY_DATE)));

                    steps.add(step);
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
        }

        return steps;
    }

    public synchronized void updatePackage(final Package pkg) {
        if (pkg.getName().isEmpty() || pkg.getCod().isEmpty()) {
            return;
        }

        final SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            final Cursor cursor = db.query(TABLE_PACKAGES, new String[]{KEY_ID}, KEY_COD + "=?", new String[]{pkg.getCod()}, null, null, null, null);
            if (cursor.getCount() == 0) {
                cursor.close();
                db.close();
                insertPackage(pkg);
                return;
            }
            cursor.close();

            final String cod = pkg.getCod();
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, pkg.getName());
            values.put(KEY_ACTIVE, pkg.isActive());
            values.put(KEY_TIME_UPDATED, new Date().getTime());

            db.update(TABLE_PACKAGES, values, KEY_COD + "=?", new String[]{cod});
            db.delete(TABLE_STEPS, KEY_PACKAGE + "=?", new String[]{cod});

            final ArrayList<Correios.Step> steps = pkg.getSteps();
            for (final Correios.Step step : steps) {
                values = new ContentValues();

                values.put(KEY_TITLE, step.title);
                values.put(KEY_DESCRIPTION, step.description);
                values.put(KEY_LOCAL, step.local);
                values.put(KEY_DATE, step.date.getTime());
                values.put(KEY_PACKAGE, pkg.getCod());

                db.insert(TABLE_STEPS, null, values);
            }

            db.close();

            scheduleBackup();
        }
    }

    private synchronized void insertPackage(final Package pkg) {
        final ArrayList<Correios.Step> steps = pkg.getSteps();
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db != null) {
            ContentValues values = new ContentValues();

            values.put(KEY_NAME, pkg.getName());
            values.put(KEY_COD, pkg.getCod());
            values.put(KEY_ACTIVE, pkg.isActive());
            values.put(KEY_TIME_CREATED, new Date().getTime());
            values.put(KEY_TIME_UPDATED, new Date().getTime());

            db.insert(TABLE_PACKAGES, null, values);

            for (final Correios.Step step : steps) {
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
            scheduleBackup();
        }
    }

    public synchronized void removePackage(final Package pkg) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db != null) {
            final String cod = pkg.getCod();

            db.delete(TABLE_STEPS, KEY_PACKAGE + "=?", new String[]{cod});
            db.delete(TABLE_PACKAGES, KEY_COD + "=?", new String[]{cod});

            db.close();
            scheduleBackup();
        }
    }

    private void scheduleBackup() {
        final BackupManager bm = new BackupManager(context.get());
        bm.dataChanged();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.get());
        if (sharedPref != null) {
            final SharedPreferences.Editor editor = sharedPref.edit();
            if (editor != null) {
                editor.putBoolean("backup_booked", true);
                editor.apply();
            }
        }
    }
}
