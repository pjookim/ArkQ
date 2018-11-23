package me.pjookim.arkq;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ServerDetailActivity extends AppCompatActivity {

    private DatabaseReference serverRef;
    List<Integer> queues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_detail);

        final String serverId = getIntent().getStringExtra("serverId");
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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        serverRef = database.getReference("server");
        // Read from the database
        database.goOnline();

        ChildEventListener childEventListener = new ChildEventListener() {
            int cnt=0;
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.i("a", dataSnapshot.getKey());
                Queue queue = dataSnapshot.getValue(Queue.class);
                switch (serverId) {
                    case "1" :
                        queues.add(cnt, Integer.parseInt(queue.getS1()));
                    case "2" :
                        queues.add(cnt, Integer.parseInt(queue.getS2()));
                    case "3" :
                        queues.add(cnt, Integer.parseInt(queue.getS3()));
                    case "4" :
                        queues.add(cnt, Integer.parseInt(queue.getS4()));
                    case "5" :
                        queues.add(cnt, Integer.parseInt(queue.getS5()));
                    case "6" :
                        queues.add(cnt, Integer.parseInt(queue.getS6()));
                    case "7" :
                        queues.add(cnt, Integer.parseInt(queue.getS7()));
                    case "8" :
                        queues.add(cnt, Integer.parseInt(queue.getS8()));
                    case "9" :
                        queues.add(cnt, Integer.parseInt(queue.getS9()));
                }
                cnt++;
                if(cnt>100) {
                    drawChart(chart);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        serverRef.orderByKey().addChildEventListener(childEventListener);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                int j = 5;
////                    Log.i("하 제발", dayOfWeek.get(0));
////                    if(dayOfWeek.get(0).equals("화"))
////                        j=7;
////                    else if(dayOfWeek.get(0).equals("수"))
////                        j=7;
//                return dayOfWeek.get(j - (int) value);
//            }
//        });

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        Description description = chart.getDescription();
        description.setEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

    }
    void drawChart(LineChart drawingChart) {
        Log.i("quesize", String.valueOf(queues.size()));
        List<Entry> entries = new ArrayList<>();
        for(int i = 0; i <= queues.size()-1; i++) {
            entries.add(new Entry(i, queues.get(i)));
            if(i>=queues.size()-1) {
                Log.i("그리라고", "이 멍청아" + i + "   " + queues.get(i));
                LineDataSet set = new LineDataSet(entries, "LineDataSet");
                Log.i(entries.get(i).toString(), entries.get(i).toString());
                LineData data = new LineData(set);
                drawingChart.setData(data);
                drawingChart.invalidate();
            }
        }
    }
}