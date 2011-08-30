package ru.yandex.metrika.mobile;

import android.app.Activity;
import android.os.Bundle;

/**
 * Информер не является standalone приложением, поэтому главная активность имеет минимумом функционала: отображает
 * информацию, как пошльзоваться виджетом, и проверяет, получен ли OAuth токен.
 *
 * @author Artur Suilin
 */
public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Globals.checkOAuthTokenExists(this);
    }
}
