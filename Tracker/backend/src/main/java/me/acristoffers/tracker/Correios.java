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

    private final ArrayList<Step> steps = new ArrayList<>();
    private String pack;
    private SyncDone listener = null;
    private Thread thread = null;

    public Correios(String cod, SyncDone listener) {
        this.pack = cod.toUpperCase(Locale.US);
        this.listener = listener;

        Runnable run = new Runnable() {
            @Override
            public void run() {
                String body = fetchAPIbody();
                boolean r = processAPIbody(body);
                Correios.this.notifyOnMainThread(r);
            }
        };

        thread = new Thread(run);
    }

    public void sync() {
        thread.start();
    }

    private String fetchAPIbody() {
        String body = null;

        if (getCod() == null || getCod().isEmpty()) {
            return null;
        }

        try {
            URL url = new URL("http://developers.agenciaideias.com.br/correios/rastreamento/json/" + getCod());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

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

    private synchronized boolean processAPIbody(String body) {
        steps.clear();

        if (body == null || body.isEmpty()) {
            return false;
        }

        StringReader stringReader = new StringReader(body);
        JsonReader jsonReader = new JsonReader(stringReader);

        try {
            jsonReader.beginArray();

            while (jsonReader.hasNext()) {
                Step step = new Step();

                jsonReader.beginObject();

                while (jsonReader.hasNext()) {
                    String name = jsonReader.nextName();

                    switch (name) {
                        case "acao":
                            step.title = jsonReader.nextString();
                            break;
                        case "data":
                            String dateString = jsonReader.nextString();
                            String[] dateAndTime = dateString.split(" ");
                            String[] dateParts = dateAndTime[0].split("/");
                            String[] timeParts = dateAndTime[1].split(":");

                            int year = Integer.parseInt(dateParts[2]);
                            int month = Integer.parseInt(dateParts[1]) - 1;
                            int day = Integer.parseInt(dateParts[0]);

                            int hour = Integer.parseInt(timeParts[0]);
                            int minute = Integer.parseInt(timeParts[1]);
                            int second = 0;

                            TimeZone timeZone = TimeZone.getTimeZone("GMT-03:00");
                            Calendar calendar = Calendar.getInstance(timeZone);

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
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                listener.finishedSyncing(b);
            }
        };

        mainHandler.post(myRunnable);
    }

    public synchronized String getCod() {
        return pack;
    }

    public synchronized ArrayList<Step> getSteps() {
        return steps;
    }

    public interface SyncDone {
        void finishedSyncing(boolean success);
    }

    public static class Step {
        public Date date = new Date();
        public String title = "";
        public String description = "";
        public String local = "";
    }

}
