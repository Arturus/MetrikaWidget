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


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Имплементация AppWidgetProvider, заведующая виджетами информера (обновление, удаление, etc)
 *
 * @author Artur Suilin
 */
public class MetrikaWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = "MetrikaWidgetProvider";

    // Имена настроек в SharedPreferences
    public static final String COUNTER_ID_KEY = "yaMetrikaCounterId";
    public static final String SITE_NAME_KEY = "yaMetrikaSiteName";


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        // При удалении виджета удаляем все настройки, связанные с ним
        SharedPreferences.Editor prefs = context.getSharedPreferences(Globals.PREF_FILE, 0).edit();
        for (int appWidgetId : appWidgetIds) {
            prefs.remove(COUNTER_ID_KEY + appWidgetId);
            prefs.remove(SITE_NAME_KEY + appWidgetId);
        }
        prefs.commit();
    }

    public static void update(Context context, int[] appWidgetIds) {
        // Чтобы не словить ANR, все обновления выполняются через отдельный сервис
        Intent intent = new Intent(context, UpdateService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.startService(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate()");
        update(context, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive: " + intent.getAction());
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            // Изменилось состояние подключения к Internet
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null && info.isConnected() && !info.isRoaming()) {
                // Если появилась связь, обновляем виджеты, которые могли быть в состоянии "offline"
                Log.d(LOG_TAG, "Got connectivity restored event");
                update(context, new int[]{});
            }
        } else {
            super.onReceive(context, intent);
        }
    }

}
