/*
 * Copyright (C) 2011 Artur Suilin
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.metrikawidget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import ru.metrika4j.*;
import ru.metrika4j.error.AuthException;
import ru.metrika4j.error.NoDataException;
import ru.metrika4j.error.TransportException;


import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Сервис, занимающийся обновлением виджетов. Каждый виджет обновляется асинхронно в своей задаче {@link UpdateTask}
 *
 * @author Artur Suilin
 */
public class UpdateService extends Service {
    // Идентификаторы виджетов, которые не смогли обновиться в силу отсутствия Интернетов
    private Set<Integer> offlineWidgets = new HashSet<Integer>();
    private static final String TAG = "UpdateService";

    private final static int[] colors = new int[] {Color.rgb(189, 115, 226), Color.rgb(134, 147, 239),
            Color.rgb(47, 173, 236), Color.rgb(11, 186, 219), Color.rgb(10, 193, 151), Color.rgb(9, 189, 70),
            Color.rgb(99, 199, 10), Color.rgb(186, 211, 11), Color.rgb(242, 206, 12), Color.rgb(242, 190, 12)};

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // В Intent-e должен приехать массив идентификаторов виджетов, которые надо обновить. Если приехал пустой массив,
        // значит надо обновить оффлайновые виджеты.
        int[] ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        if (ids == null) { // Приехал пустой Intent, ничего не делаем
            Log.e(TAG, "No ids in onStartCommand()");
        } else {
            if (ids.length == 0) { // обновляем оффлайновые виджеты
                Log.d(TAG, "Got updateOffline request");
                int count = 0;
                ids = new int[offlineWidgets.size()];
                for (Integer offlineWidget : offlineWidgets) {
                    ids[count++] = offlineWidget;
                }
            }
            // Обновляем каждый виджет в своей асинхронной задаче
            for (int id : ids) {
                new UpdateTask(id, this).execute();
            }
        }
        return START_NOT_STICKY; // После обработки команды сервис можно остановить
    }


    /** Асинхронное получение данных через Metrika API и обновление виджета. */
    private class UpdateTask extends AsyncTask<Void, Void, Result> {
        private final int widgetId;
        private final Context context;
        private final RemoteViews views;
        private final AppWidgetManager manager;
        private final SharedPreferences prefs;

        public UpdateTask(int widgetId, Context context) {
            this.widgetId = widgetId;
            this.context = context;
            manager = AppWidgetManager.getInstance(context);
            int[] installedids = manager.getAppWidgetIds(new ComponentName(context, MetrikaWidgetProvider.class));
            Log.d(TAG, "ids: " + installedids);
            views = new RemoteViews(context.getPackageName(), R.layout.widget);
            prefs = context.getSharedPreferences(Globals.PREF_FILE, 0);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Рисуем состояние "обновляюсь"
            views.setTextViewText(R.id.widget_label, context.getResources().getText(R.string.widgetRefreshing));
            views.setImageViewResource(R.id.widget_picture, R.drawable.dia_inv);
            // Немедленно обновляем виджет
            manager.updateAppWidget(widgetId, views);
        }

        @Override
        protected Result doInBackground(Void... voids) {
            try {
                // Получаем из настроек идентификатор счетчика для обрабатываемого виджета
                int counterId = prefs.getInt(MetrikaWidgetProvider.COUNTER_ID_KEY + widgetId, 0);
                if (counterId == 0) {
                    Log.w(TAG, "No counterId for widget " + widgetId);
                } else {
                    MetrikaApi api = Globals.getApi(context);
                    // Собственно запрос к Metrika API
                    Report report = api.makeReportBuilder(Reports.trafficSummary, counterId)
                            .withDateFrom(new MetrikaDate().plusDays(-9))
                            .withDateTo(new MetrikaDate())
                            .build();
                    return new Result(report,"visits");
                    //return new Result(report.getTotals().getInt("visits"));}
                }
            } catch (NoDataException e) {
                return Result.EMPTY;
            } catch (TransportException e) {
                return Result.OFFLINE;
            } catch (AuthException e) {
                Globals.resetAPI();
            } catch (Throwable e) {
                Log.e(TAG, "Error getting data: " + e.getMessage(), e);
            }
            return Result.UNKNOWN;
        }

        @Override
        protected void onPostExecute(Result visits) {
            if (visits.isOffline()) {
                offlineWidgets.add(widgetId);
            } else {
                offlineWidgets.remove(widgetId);
            }
            // Задаем содержимое надписей и картинок в виджете
            String text = visits.toString();
            views.setTextViewText(R.id.widget_text, text);
            if (text.length() > 5) {
                // Небольшой хак: уменьшаем размер текста, чтобы влезла шестизначная цифра.
                views.setFloat(R.id.widget_text, "setTextSize", 20);
            } else {
                // Исходный размер - 24
                views.setFloat(R.id.widget_text, "setTextSize", 24);
            }
            views.setTextViewText(R.id.widget_label, context.getResources().getText(R.string.widgetVisits));
            //views.setImageViewResource(R.id.widget_picture,
            //        visits.isOffline() ? R.drawable.dia_offline : R.drawable.dia);
            if (visits.isOffline) {
                views.setImageViewResource(R.id.widget_picture, R.drawable.dia_offline);
            } else {
                if (visits.values == null) {
                    views.setImageViewResource(R.id.widget_picture, R.drawable.dia);
                } else {
                    // Узнаем высоту и ширину диаграммы (загружаем дефолтную диаграмму и смотрим на нее)
                    Bitmap templateBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dia);
                    int bitmapWidth = templateBitmap.getWidth();
                    int bitmapHeight = templateBitmap.getHeight();
                    templateBitmap.recycle();
                    Bitmap diaBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                    Canvas dia = new Canvas(diaBitmap);
                    Paint p = new Paint();
                    p.setAntiAlias(false);
                    int pixelWidth = bitmapWidth / visits.values.length;
                    float heightFactor = bitmapHeight / (float) visits.maxValue;
                    for (int i = 0; i < visits.values.length; i++) {
                        int v = visits.values[i];
                        p.setColor(colors[i]);
                        dia.drawRect(new Rect(i * pixelWidth, bitmapHeight - Math.round(v * heightFactor), (i + 1) * pixelWidth, bitmapHeight), p);
                    }
                    views.setImageViewBitmap(R.id.widget_picture, diaBitmap);
                }
            }


            views.setTextViewText(R.id.widget_site_name, prefs.getString(MetrikaWidgetProvider.SITE_NAME_KEY + widgetId,
                    "site"));
            // Делаем обработчик onClick()
            Intent clickIntent = new Intent(context, MetrikaWidgetProvider.class);
            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
            //Смысл этой магии c Uri в том, чтобы Андроид считал Intent-ы для разных widgetId реально разными,
            // а не копиями одного и того же. Extras не принимают участия в Intent.filterEquals, поэтому дополнительно
            // выставляем data
            clickIntent.setData(Uri.fromParts("metwd", "widget", Integer.toString(widgetId)));

            // Вешаем обработчик клика на все составляющие widget-а
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, actionPendingIntent);
            views.setOnClickPendingIntent(R.id.widget_text, actionPendingIntent);
            views.setOnClickPendingIntent(R.id.widget_label, actionPendingIntent);

            // обновляем виджет
            manager.updateAppWidget(widgetId, views);
            Log.d(TAG, "Done update for " + widgetId);
        }
    }

    /**
     * Результат запроса значения через MetrikaApi. Кроме штатного состояния "результат известен", есть еще два:
     * "сделали запрос, но результат неизвестен" (value == null) и "находимся в оффлайне" (isOffline==true)
     */
    final static class Result {
        private Integer currentValue;
        private boolean isOffline;
        private int[] values;
        private int maxValue;

        static Result UNKNOWN = new Result(null);
        static Result OFFLINE = new Result(true);
        static Result EMPTY = new Result(0);


        private Result(boolean offline) {
            isOffline = offline;
        }

        private Result(Integer currentValue) {
            this.currentValue = currentValue;
        }


        Result(Report report, String field) {
            ReportItem[] items = report.getData();
            MetrikaDate from = report.getDateFrom();
            MetrikaDate to = report.getDateTo();
            int numberOfDays = (int)from.diffDayPeriods(to);
            values = new int[numberOfDays + 1];
            long startDate = from.getUnixDay();

            for (ReportItem item : items) {
                MetrikaDate rowDate = new MetrikaDate(item.getString("date"));
                int index = (int)(rowDate.getUnixDay() - startDate);
                values[index] = item.getInt(field);
            }
            for (int value : values) {
                if (value > maxValue) {
                    maxValue = value;
                }
            }
            currentValue = values[values.length - 1];

        }

        public boolean isOffline() {
            return isOffline;
        }

        public String toString() {
            return currentValue == null ? "?" : currentValue.toString();
        }
    }


}
