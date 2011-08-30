package ru.yandex.metrika.mobile;

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
 * @author Artur
 * @version $Id$
 */
public class AuthTokenActivity extends Activity {

    private String extractParam(String param) {
        String fragment = getIntent().getData().getFragment();
        String[] parts = fragment.split("\\&");
        for (String part : parts) {
            if (part.startsWith(param)) {
                return part.substring(param.length(), part.length());
            }
        }
        // Не совсем очевидно, что здесь надо делать. Лучшие идеи?
        throw new IllegalArgumentException("Can't extract " + param + " from query string");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String token = extractParam("access_token=");

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
