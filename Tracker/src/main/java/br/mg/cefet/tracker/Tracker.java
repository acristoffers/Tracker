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

package br.mg.cefet.tracker;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tracker {

    private List<Step> steps = new ArrayList<>();
    private String pack;
    private TrackerDone listener = null;

    public Tracker(String cod) {
        pack = cod.toUpperCase();
    }

    public void sync() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                sync2();
            }
        };

        thread.start();
    }

    private void sync2() {
        String html = executePOST();

        if (html != null) {
            html = extractTableFromHTML(html);

            if (html != null) {
                parseTable(html);

                notifyOnMainThread(true);
                return;
            }
        }

        notifyOnMainThread(false);
    }

    private String executePOST() {
        String html;

        if (getCod() == null || getCod().isEmpty()) {
            return null;
        }

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://www2.correios.com.br/sistemas/rastreamento/resultado.cfm");

        List<NameValuePair> values = new ArrayList<>();
        values.add(new BasicNameValuePair("objetos", getCod()));

        try {
            post.setEntity(new UrlEncodedFormEntity(values));
            HttpResponse response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();
            html = EntityUtils.toString(responseEntity);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }

        return html;
    }

    private String extractTableFromHTML(String html) {
        String regex = "<table class=\"listEvent sro\">(.*)</table>";
        Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);

        return m.find() ? m.group(1) : null;
    }

    private void notifyOnMainThread(final boolean b) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                listener.trackerFinishedSyncing(b);
            }
        };

        mainHandler.post(myRunnable);
    }

    private void parseTable(String html) {
        html = html.replaceAll("[\\s]+", " ");
        String regex = "<tr>(.*?)</tr>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(html);

        steps = new ArrayList<>();

        while (m.find()) {
            String tr = m.group(1);
            parseRow(tr);
        }
    }

    private void parseRow(String html) {
        String regex = "<td.*?>(.*?)</td>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(html);

        Step step = new Step();

        if (m.find()) {
            String td = m.group(1);
            td = td.replaceAll("<br />", "|");
            td = td.replaceAll("<.*?>", "");
            td = Html.fromHtml(td).toString();
            final String[] split = td.split("\\|");

            if (split != null) {
                String date = "";
                String time = "";

                if (split[0] != null) {
                    date = split[0].trim();
                }

                if (split[1] != null) {
                    time = split[1].trim();
                }

                if (split[2] != null) {
                    step.city = split[2].trim();
                }

                DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                try {
                    step.date = df.parse(date + " " + time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (m.find()) {
            String td = m.group(1);
            td = td.replaceAll("<br />", "|");
            td = td.replaceAll("<.*?>", "");
            td = Html.fromHtml(td).toString();

            final String[] split = td.split("\\|");
            if (split != null) {
                if (split[0] != null) {
                    step.title = split[0].trim();
                }

                if (split[1] != null) {
                    step.description = split[1].trim();
                }
            }
        }

        step.title = step.title.replaceAll("[ ]*/$", "");
        step.description = step.description.replaceAll("[ ]*/$", "");
        step.city = step.city.replaceAll("[ ]*/$", "");

        steps.add(step);
    }

    public String getCod() {
        return pack;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setListener(TrackerDone listener) {
        this.listener = listener;
    }

    public interface TrackerDone {
        public void trackerFinishedSyncing(boolean success);
    }

    public class Step {
        public Date date = new Date();
        public String title = "";
        public String description = "";
        public String city = "";
    }

}
