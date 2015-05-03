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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.acristoffers.tracker.activities.PackageDetails;

public class AlarmReceiver extends BroadcastReceiver implements Package.StatusReady {

    private final Map<String, Integer> countSteps = new HashMap<>();
    private Context context;

    public static void setAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null && action.equals("android.intent.action.BOOT_COMPLETED")) {
            setAlarm(context);
            return;
        }

        this.context = context;

        List<Package> packages = Package.allPackages(context);
        for (Package pkg : packages) {
            if (!pkg.isActive()) {
                continue;
            }

            int count = pkg.getSteps().size();
            String code = pkg.getCod();

            countSteps.put(code, count);

            pkg.setListener(this);
            pkg.checkForStatusUpdates();
        }
    }

    @Override
    public void statusUpdated(Package pkg) {
        String code = pkg.getCod();
        int count = pkg.getSteps().size();

        if (countSteps.get(code) < count) {
            pkg.save();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                String title = context.getString(R.string.notification_package_updated_title, pkg.getName());
                String message = context.getString(R.string.notification_package_updated_body, pkg.getName());

                Intent intent = new Intent(context, PackageDetails.class);
                intent.putExtra(PackageDetails.PACKAGE_CODE, code);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setTicker(title)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(icon)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true);

                Notification notification = builder.build();

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(pkg.getId(), notification);
            }
        }
    }

}