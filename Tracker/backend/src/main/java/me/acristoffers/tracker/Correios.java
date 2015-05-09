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

import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Correios {

    private final String code;
    private final SyncDone listener;
    private final Thread thread;
    private final ArrayList<Step> steps = new ArrayList<>();

    public Correios(final String cod, final SyncDone listener) {
        this.code = cod.toUpperCase(Locale.US);
        this.listener = listener;

        final Runnable run = new Runnable() {
            @Override
            public void run() {
                String body = fetchAPIBody();
                boolean r = processAPIbody(body);
                Correios.this.notifyOnMainThread(r);
            }
        };

        thread = new Thread(run);
    }

    public void sync() {
        thread.start();
    }

    private String fetchAPIBody() {
        String body = null;

        if (getCod() == null || getCod().isEmpty()) {
            return null;
        }

        try {
            final URL url = new URL("http://developers.agenciaideias.com.br/correios/rastreamento/json/" + getCod());
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            final InputStream is = connection.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            connection.connect();

            body = "";
            String buffer;

            while ((buffer = reader.readLine()) != null) {
                body += buffer + "\n";
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return body;
    }

    private synchronized boolean processAPIbody(final String body) {
        steps.clear();

        if (body == null || body.isEmpty()) {
            return false;
        }

        final StringReader stringReader = new StringReader(body);
        final JsonReader jsonReader = new JsonReader(stringReader);

        try {
            jsonReader.beginArray();

            while (jsonReader.hasNext()) {
                final Step step = new Step();

                jsonReader.beginObject();

                while (jsonReader.hasNext()) {
                    String name = jsonReader.nextName();

                    switch (name) {
                        case "acao":
                            step.title = jsonReader.nextString();
                            break;
                        case "data":
                            final String dateString = jsonReader.nextString();
                            final String[] dateAndTime = dateString.split(" ");
                            final String[] dateParts = dateAndTime[0].split("/");
                            final String[] timeParts = dateAndTime[1].split(":");

                            final int year = Integer.parseInt(dateParts[2]);
                            final int month = Integer.parseInt(dateParts[1]) - 1;
                            final int day = Integer.parseInt(dateParts[0]);

                            final int hour = Integer.parseInt(timeParts[0]);
                            final int minute = Integer.parseInt(timeParts[1]);
                            final int second = 0;

                            final TimeZone timeZone = TimeZone.getTimeZone("GMT-03:00");
                            final Calendar calendar = Calendar.getInstance(timeZone);

                            calendar.set(year, month, day, hour, minute, second);

                            step.date = calendar.getTime();
                            break;
                        case "detalhes":
                            step.description = jsonReader.nextString();
                            break;
                        case "local":
                            step.local = jsonReader.nextString();
                            break;
                    }
                }

                jsonReader.endObject();

                steps.add(step);
            }

            jsonReader.endArray();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private void notifyOnMainThread(final boolean b) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.finishedSyncing(b, Correios.this);
                }
            }
        };
        mainHandler.post(myRunnable);
    }

    public synchronized String getCod() {
        return code;
    }

    public synchronized ArrayList<Step> getSteps() {
        return steps;
    }

    public interface SyncDone {
        void finishedSyncing(final boolean success, Correios correios);
    }

    public static class Step {
        public Date date = new Date();
        public String title = "";
        public String description = "";
        public String local = "";
    }

}
