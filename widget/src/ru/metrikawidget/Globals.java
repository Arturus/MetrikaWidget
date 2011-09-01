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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import ru.yandex.metrika.api.ApiFactory;
import ru.yandex.metrika.api.MetrikaApi;
import ru.yandex.metrika.api.error.AuthException;
import ru.yandex.metrika.api.json.light.OrgJsonMapper;

/** @author Artur Suilin */
public class Globals {
    public static final String PREF_FILE = "preferences";
    public static final String PREF_TOKEN = "token";
    private static MetrikaApi api;

    /**
     * Возвращает глобальный instance MetrikaApi, при необходимости создавая его. Eсли instance не существует и его невозможно создать
     * (нет сохранённого в настройках OAuth токена), бросает {@link AuthException}
     */
    public static synchronized MetrikaApi getApi(Context context) {
        if (api == null) {
            String token = getOAuthToken(context);
            if (token == null) {
                throw new AuthException();
            } else {
                api = ApiFactory.createMetrikaAPI(token, new OrgJsonMapper());
            }
        }
        return api;
    }

    private static String getOAuthToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Globals.PREF_FILE, Context.MODE_PRIVATE);
        return prefs.getString(Globals.PREF_TOKEN, null);
    }

    /**
     * Проверяет, существует ли сохранённый в настройках OAuth токен. Если не существует, иницирует процедуру получения OAuth токена.
     *
     * @param activity Активность, от имени которой будет происходить переход на получение OAuth токена
     * @return true, если OAuth токен существует, иначе false.
     */
    public static boolean checkOAuthTokenExists(final Activity activity) {
        if (getOAuthToken(activity) == null) {
            requestAuth(activity);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Иницирует процедуру получения OAuth токена. Когда токен будет получен, запустится {@link AuthTokenActivity}
     *
     * @param activity Активность, от имени которой будет происходить получение OAuth токена
     */
    public static void requestAuth(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.authDialogTitle)
                .setMessage(R.string.authFirstTime)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "https://oauth.yandex.ru/authorize?response_type=token&client_id=1359488e196b4bfa92615d0885b106d4"));
                        activity.startActivity(intent);
                        //finish();
                    }
                }).show();
    }


}
