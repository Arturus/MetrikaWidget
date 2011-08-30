package ru.yandex.metrika.mobile;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ru.yandex.metrika.api.entity.Counter;
import ru.yandex.metrika.api.error.AuthException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Вызывается при настройке виджета. Загружает и отображает список счетчиков.
 *
 * @author Artur Suilin
 */
public class WidgetSetupActivity extends ListActivity {
    private List<Counter> counters = new ArrayList<Counter>();
    private CounterListAdapter listAdapter;


    private int getWidgetId() {
        Intent intent = getIntent();
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            throw new IllegalStateException("No widgetId provided");
        }
        return appWidgetId;
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        int appWidgetId = getWidgetId();

        // Записываем в prefernces идентификатор счетчика и имя сайта для данного виджета
        Counter counter = counters.get(position);
        SharedPreferences.Editor prefs = getSharedPreferences(Globals.PREF_FILE, 0).edit();
        prefs.putInt(MetrikaWidgetProvider.COUNTER_ID_KEY + appWidgetId, counter.getId());
        prefs.putString(MetrikaWidgetProvider.SITE_NAME_KEY + appWidgetId, counter.getSite());
        prefs.commit();

        // Возвращаем результат widget manager-у
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();

        // Обновляем виджет, чтобы он показал реальное значение
        MetrikaWidgetProvider.update(this, new int[]{appWidgetId});
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED); // Если не дойдем до выбора счетчика, считаем активность отменённой
        listAdapter = new CounterListAdapter();
        setListAdapter(listAdapter);
        setContentView(R.layout.widget_setup);

        if (Globals.checkOAuthTokenExists(
                this)) { // Загружаем счетчики только если уже авторизовались, иначе отправляем на авторизацию
            new CountersLoadTask().execute();
        }
    }

    /** Асинхронная загрузка списка счетчиков */
    private class CountersLoadTask extends AsyncTask<Void, Void, Counter[]> {
        private Exception error;
        private boolean needAuth = false;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(WidgetSetupActivity.this, null,
                    getResources().getString(R.string.loading_progress), true);
        }

        @Override
        protected void onPostExecute(Counter[] counters) {
            progressDialog.dismiss();
            if (needAuth) {
                Globals.requestAuth(WidgetSetupActivity.this);
            } else if (error != null) {
                new AlertDialog.Builder(WidgetSetupActivity.this).setTitle(R.string.errorTitle)
                        .setIcon(R.drawable.emo_im_wtf)
                        .setMessage(error.getMessage())
                        .setPositiveButton(R.string.OK, null)
                        .show();
            } else {
                WidgetSetupActivity.this.counters.addAll(Arrays.asList(counters));
                listAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected Counter[] doInBackground(Void... voids) {
            try {
                return Globals.getApi(WidgetSetupActivity.this).getCounters();
            } catch (AuthException e) {
                needAuth = true;
            } catch (Exception e) {
                error = e;
            }
            // Если словили Exception, возвращаем пустой список
            return new Counter[0];
        }
    }

    /** Адаптер для отображения Counter в элементе списка счетчиков */
    private class CounterListAdapter extends ArrayAdapter<Counter> {
        public CounterListAdapter() {
            super(WidgetSetupActivity.this, R.layout.site_list_row, R.id.site_list_name, counters);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView name = (TextView) view.findViewById(R.id.site_list_name);
            name.setText(counters.get(position).getName());
            TextView site = (TextView) view.findViewById(R.id.site_list_site);
            site.setText(counters.get(position).getSite());
            return view;
        }
    }


}
