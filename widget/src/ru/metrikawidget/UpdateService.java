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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import ru.metrika4j.MetrikaApi;
import ru.metrika4j.MetrikaDate;
import ru.metrika4j.Report;
import ru.metrika4j.Reports;
import ru.metrika4j.error.NoDataException;
import ru.metrika4j.error.TransportException;


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
                            .withDateFrom(new MetrikaDate())
                            .withDateTo(new MetrikaDate())
                            .build();
                    return new Result(report.getTotals().getInt("visits"));
                }
            } catch (NoDataException e) {
                return Result.EMPTY;
            } catch (TransportException e) {
                return Result.OFFLINE;
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
            views.setImageViewResource(R.id.widget_picture,
                    visits.isOffline() ? R.drawable.dia_offline : R.drawable.dia);
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
    static class Result {
        private Integer value;
        private boolean isOffline;

        static Result UNKNOWN = new Result(null);
        static Result OFFLINE = new Result(true);
        static Result EMPTY = new Result(0);

        private Result(boolean offline) {
            isOffline = offline;
        }

        private Result(Integer value) {
            this.value = value;
        }

        public boolean isOffline() {
            return isOffline;
        }

        public String toString() {
            return value == null ? "?" : value.toString();
        }
    }


}
