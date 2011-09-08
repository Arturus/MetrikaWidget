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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * Активность-невидимка. Принимает из браузера oauth токен, записывает его в prefernces и говорит пользователю, что
 * сейчас он на самом деле может добавить виджет.
 *
 * @author Artur Suilin
 */
public class AuthTokenActivity extends Activity {

    private String extractParam(String param) {
        String fragment = getIntent().getData().getFragment();
        if (fragment != null) {
            String[] parts = fragment.split("\\&");
            for (String part : parts) {
                if (part.startsWith(param)) {
                    return part.substring(param.length(), part.length());
                }
            }
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String token = extractParam("access_token=");
        if (token == null) {
        // Не совсем очевидно, что здесь надо делать. Лучшие идеи?
            new AlertDialog.Builder(this).setTitle(R.string.authNonCompleteTile)
                .setMessage(R.string.authNonCompleteText)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
            return;
        }
        // Запоминаем полученный токен в preferences
        SharedPreferences prefs = getSharedPreferences(Globals.PREF_FILE, 0);
        prefs.edit().putString(Globals.PREF_TOKEN, token).commit();

        // Показываем success - сообщение
        new AlertDialog.Builder(this).setTitle(R.string.authCompleteTile)
                .setMessage(R.string.authCompleteText)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Отображаем home screen, чтобы пользователь мог сразу же добавить виджет
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startActivity(startMain);
                        finish();
                    }
                })
                .show();
    }
}
