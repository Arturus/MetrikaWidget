package ru.metrikawidget;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.metrikawidget.api.ByTimeResponse;
import ru.metrikawidget.api.CounterListResponse;

public interface MetrikaApi {

    String BASE_URL = "https://api-metrika.yandex.ru/";

    String STAT_URL       = "stat/v1/";
    String MANAGEMENT_URL = "management/v1/";

    @GET(MANAGEMENT_URL + "counters")
    Call<CounterListResponse> getCounters();

    @GET(STAT_URL + "data/bytime")
    Call<ByTimeResponse> byTime(@Query("ids") List<Integer> ids,
                                @Query("metrics") String metrics,
                                @Query("dimensions") String dimensions,
                                @Query("date1") String date1,
                                @Query("date2") String date2,
                                @Query("group") String group);

}
