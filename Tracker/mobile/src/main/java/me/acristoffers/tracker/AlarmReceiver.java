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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import me.acristoffers.tracker.activities.PackageDetailsActivity;

public class AlarmReceiver extends BroadcastReceiver implements Package.StatusReady {

    private final HashMap<String, Integer> countSteps = new HashMap<>();
    private WeakReference<Context> context;

    public static void setAlarm(final Context context) {
        try {
            final Intent intent = new Intent(context, AlarmReceiver.class);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            final long fiveMinutes = 5 * 60 * 1000;

            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            final String minutes = sharedPref.getString("sync_interval", "15");

            long repeatMinutes;
            try {
                repeatMinutes = Long.parseLong(minutes) * 60 * 1000;
            } catch (NumberFormatException e) {
                repeatMinutes = 15 * 60 * 1000;
            }

            final boolean active = sharedPref.getBoolean("autosync", true);

            if (active) {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, fiveMinutes, repeatMinutes, pendingIntent);
            } else {
                alarmManager.cancel(pendingIntent);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (action != null && action.equals("android.intent.action.BOOT_COMPLETED")) {
            setAlarm(context);
        }

        this.context = new WeakReference<>(context);

        BackupAgent.restoreIfNotBackingUp(context);

        final ArrayList<Package> packages = Package.allPackages(context);
        for (final Package pkg : packages) {
            if (!pkg.isActive()) {
                continue;
            }

            final int count = pkg.getSteps().size();
            final String code = pkg.getCod();

            countSteps.put(code, count);

            new Package(code, context, this).checkForStatusUpdates();
        }
    }

    @Override
    public void statusUpdated(final Package pkg) {
        final String code = pkg.getCod();
        final int count = pkg.getSteps().size();

        if (countSteps.get(code) < count) {
            pkg.save();

            final NotificationManager notificationManager = (NotificationManager) context.get().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                final String title = context.get().getString(R.string.notification_package_updated_title, pkg.getName());
                final String message = context.get().getString(R.string.notification_package_updated_body, pkg.getName());

                final Intent intent = new Intent(context.get(), PackageDetailsActivity.class);
                intent.putExtra(PackageDetailsActivity.PACKAGE_CODE, code);
                final PendingIntent pendingIntent = PendingIntent.getActivity(context.get(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                final Bitmap icon = BitmapFactory.decodeResource(context.get().getResources(), R.mipmap.ic_launcher);

                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context.get());
                builder.setTicker(title)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(icon)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true);

                final Notification notification = builder.build();

                final NotificationManager nm = (NotificationManager) context.get().getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(pkg.getId(), notification);
            }
        }
    }

}
