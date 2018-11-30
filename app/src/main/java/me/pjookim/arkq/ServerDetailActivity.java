package me.pjookim.arkq;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ServerDetailActivity extends AppCompatActivity {

    List<Integer> queues = new ArrayList<>();
    List<String> timestamp = new ArrayList<>();
    SimpleDateFormat sdf;
    Date resultTime;
    TimePicker timePicker;
    String serverId;
    TextView realTimeText;
    Retrofit retrofit;
    ServerDetail apiService;
    private DatabaseReference serverRef;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_detail);

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-8885000207073232/2682907502");
        // TODO: Add adView to your view hierarchy.

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        MobileAds.initialize(this,
                "ca-app-pub-8885000207073232~9538840339");

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8885000207073232/5350271162");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        serverId = getIntent().getStringExtra("serverId");
        String serverName = getIntent().getStringExtra("serverName");

        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(serverName);
        toolbarTitle.setTextSize(20);

        Log.i("serverName", serverName);

        final LineChart chart;
        chart = (LineChart) findViewById(R.id.chart);
        chart.setDragEnabled(false);
        chart.setTouchEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ServerDetail.BASEURL)
                .build();
        apiService = retrofit.create(ServerDetail.class);
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        sdf = new SimpleDateFormat("yyyyMMdd HHmmss");

        realTimeText = (TextView) findViewById(R.id.realtime_time);

        final String getTime = sdf.format(date);

        Call<JsonArray> call = apiService.getGraph("graph", getTime);
        Log.i("getTime", getTime);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    JsonArray object = response.body();
                    if (object != null) {
                        Log.i("object", String.valueOf(object.size()));
                        Log.i("오 드디어", object.get(0).getAsJsonObject().get("timestamp").toString().substring(0, 14) + "    " + getTime.substring(0, 13));
                        for (int i = 0; i < object.size() - 1; i++) {
                            queues.add(i, Integer.parseInt(object.get(i).getAsJsonObject().get("s" + serverId).toString()));
                            Date date = null;
                            try {
                                Log.i("date 그 전", object.get(i).getAsJsonObject().get("timestamp").toString().replace("\"", ""));
                                date = (Date) sdf.parse(object.get(i).getAsJsonObject().get("timestamp").toString().replace("\"", ""));
                                Log.i("date", date.toString());
                                timestamp.add(i, String.valueOf(date.getTime()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        drawChart(chart);
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String getTime = sdf.format(date);
                        realTimeText.setText(getTime + " 기준");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                Log.i("아 제발ㅠㅠ", t.toString());
            }
        });

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Log.i("포매티드밸류", timestamp.get((int) value));
                Date date = new Date(Long.parseLong(timestamp.get((int) value)));
                sdf = new SimpleDateFormat("aa hh:mm");

                String getTime = sdf.format(date);
                return getTime;
            }
        });

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        Description description = chart.getDescription();
        description.setEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        Retrofit timerService = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ServerTimer.BASEURL)
                .build();
        final ServerTimer serverTimerService = timerService.create(ServerTimer.class);

        final TextView timerResult = (TextView) findViewById(R.id.result_time);

        final LinearLayout resultLayout = (LinearLayout) findViewById(R.id.result_layout);

        final Button getResult = (Button) findViewById(R.id.calculate);

        timePicker = (TimePicker) findViewById(R.id.timepicker);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(final TimePicker timePicker, int i, int i1) {
                resultLayout.setVisibility(GONE);
                getResult.setVisibility(VISIBLE);
            }
        });

        final Button setAlarm = (Button) findViewById(R.id.alarm);
        setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = 0;
                int i1 = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    i = timePicker.getHour();
                    i1 = timePicker.getMinute();
                }
                alarm_on(resultTime);
            }
        });

        getResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }

                int i = 0;
                int i1 = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    i = timePicker.getHour();
                    i1 = timePicker.getMinute();
                }

                long currentTimeMillis = System.currentTimeMillis();
                Date date = new Date(currentTimeMillis);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

                final String getTime = simpleDateFormat.format(date);

                Call<JsonObject> call = serverTimerService.getTimer("timer", getTime + " " + String.valueOf(i) + String.valueOf(i1) + "00", "s" + serverId);
                Log.i("getTime", getTime + " " + String.valueOf(i) + String.valueOf(i1) + "00");
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            JsonObject object = response.body();
                            if (object.get("time") != null) {
                                try {
                                    sdf = new SimpleDateFormat("yyyyMMdd HHmmss");
                                    Date timestamp = (Date) sdf.parse(object.get("time").getAsString());
                                    resultTime = new Date(String.valueOf(timestamp));
                                    sdf = new SimpleDateFormat("aa hh:mm");
                                    timerResult.setTextSize(48);
                                    timerResult.setText(sdf.format(resultTime));
                                    resultLayout.setVisibility(View.VISIBLE);
                                    getResult.setVisibility(GONE);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                timerResult.setTextSize(24);
                                timerResult.setText("데이터가 존재하지 않습니다.");
                                resultLayout.setVisibility(View.VISIBLE);
                                getResult.setVisibility(GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Log.i("아 제발ㅠㅠ", t.toString());
                    }
                });
            }
        });
    }

    void drawChart(LineChart drawingChart) {
        Log.i("quesize", String.valueOf(queues.size()));
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i <= queues.size() - 1; i++) {
            entries.add(new Entry(i, queues.get(i)));
            if (i >= queues.size() - 1) {
                Log.i("그리라고", "이 멍청아" + i + "   " + queues.get(i));
                LineDataSet set = new LineDataSet(entries, "LineDataSet");
                Log.i(entries.get(i).toString(), entries.get(i).toString());
                LineData data = new LineData(set);
                drawingChart.setData(data);
                drawingChart.invalidate();
            }
        }
    }

    public void alarm_on(Date timestamp) {
        // 알람 등록하기
        Log.i("alarm", "setAlarm");
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ServerDetailActivity.this, AlarmReceiver.class);   //AlarmReceiver.class이클레스는 따로 만들꺼임 알람이 발동될때 동작하는 클레이스임

        PendingIntent sender = PendingIntent.getBroadcast(ServerDetailActivity.this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        //알람시간 calendar에 set해주기

        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), timestamp.getHours(), timestamp.getMinutes());//시간을 10시 01분으로 일단 set했음
        calendar.set(Calendar.SECOND, 0);

        //알람 예약
        //am.set(AlarmManager.RTC, calendar.getTimeInMillis(), sender);//이건 한번 알람
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, sender);//이건 여러번 알람 24*60*60*1000 이건 하루에한번 계속 알람한다는 뜻.
        Toast.makeText(ServerDetailActivity.this, "알람 설정:" + Integer.toString(calendar.get(calendar.HOUR_OF_DAY)) + ":" + Integer.toString(calendar.get(calendar.MINUTE)), Toast.LENGTH_LONG).show();
    }

    String getTimerResult() {
        Retrofit timerService = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ServerTimer.BASEURL)
                .build();
        ServerTimer serverTimerService = timerService.create(ServerTimer.class);
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        final String getTime = simpleDateFormat.format(date);

        int i = 0;
        int i1 = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            i = timePicker.getHour();
            i1 = timePicker.getMinute();
        }

        Call<JsonObject> call = serverTimerService.getTimer("timer", getTime + " " + String.valueOf(i) + String.valueOf(i1) + "00", "s" + serverId);
        Log.i("getTime", getTime + " " + String.valueOf(i) + String.valueOf(i1) + "00");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    if (object.get("time") != null) {
                        try {
                            sdf = new SimpleDateFormat("yyyyMMdd HHmmss");
                            Date timestamp = (Date) sdf.parse(object.get("time").getAsString());
                            resultTime = new Date(String.valueOf(timestamp));
                            sdf = new SimpleDateFormat("aa hh:mm");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.i("아 제발ㅠㅠ", t.toString());
            }
        });
        return sdf.format(resultTime);
    }
}