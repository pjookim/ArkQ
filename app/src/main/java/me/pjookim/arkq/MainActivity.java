package me.pjookim.arkq;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    AlertDialog.Builder builder;
    private RecyclerView recyclerView;
    private ArrayList<ItemObject> list = new ArrayList();
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseReference mDatabase;
    private AdView mAdView;
    private TextView realTimeText;
    private DatabaseReference androidRef;
    //데이터 배열 선언
    private ArrayList<ItemObject> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_more_vert_black_24dp);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        realTimeText = (TextView) findViewById(R.id.realtime_time);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        androidRef = database.getReference("android");

        builder = new AlertDialog.Builder(this);

        // Read from the database
        androidRef.child("notice").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                String notice = dataSnapshot.getValue(String.class);
                if (notice != null) {
                    if (!notice.equals("")) {
                        builder.setMessage(dataSnapshot.getValue().toString())
                                .setCancelable(false)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        //Creating dialog box
                        AlertDialog alert = builder.create();
                        //Setting the title manually
                        alert.setTitle("공지");
                        alert.show();
                    }
                }
                database.goOffline();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        MobileAds.initialize(this, "ca-app-pub-8885000207073232~9538840339");

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-8885000207073232/2682907502");
        // TODO: Add adView to your view hierarchy.

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Description().execute();
            }
        });
        new Description().execute();

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class Description extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("잠시 기다려 주세요.");
            progressDialog.show();
            list.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("http://loaq.kr/").get();
                Elements mElementDataSize = doc.select("div[class=status]").select("dl");
                int mElementSize = mElementDataSize.size();
                int serverId = 1;

                for (Element elem : mElementDataSize) {
                    String server_title = elem.select("dt").text().replaceAll(" ", "");
                    String queue = elem.select("dd[class=cnt] > span:first-child").text().replaceAll(" ", "");
                    String status;
                    Log.d("test", server_title + " " + queue);
                    if (server_title.contains("서버")) {
                        // 갖다버려;
                        Log.d("test", "잘 갖다버렸는지 확인");
                    } else if (server_title.isEmpty() || queue.isEmpty()) {
                        // 갖다버려;
                        Log.d("test", "잘 갖다버렸는지 확인");
                    } else {
                        Double doubleQueue = (Double.parseDouble(queue) * 1.034);
                        int intQueue = (int) Math.round(doubleQueue);
                        list.add(new ItemObject(server_title, intQueue, serverId));
                        serverId++;
                    }
                }
                //Log.d("debug :", "List " + mElementDataSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //ArraList를 인자로 해서 어답터와 연결한다.
            MyAdapter myAdapter = new MyAdapter(list);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(myAdapter);

            progressDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);

            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String getTime = sdf.format(date);
            realTimeText.setText(getTime + " 기준");
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView_img;
        private TextView serverTitle, queue, status;
        private ConstraintLayout listItem;

        public ViewHolder(View itemView) {
            super(itemView);

            serverTitle = (TextView) itemView.findViewById(R.id.server_title);
            queue = (TextView) itemView.findViewById(R.id.queue);
            status = (TextView) itemView.findViewById(R.id.status);
            listItem = (ConstraintLayout) itemView.findViewById(R.id.list_item);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final ArrayList<ItemObject> mList;

        public MyAdapter(ArrayList<ItemObject> list) {
            this.mList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_data, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.serverTitle.setText(String.valueOf(mList.get(position).getServer()));
            DecimalFormat formatter = new DecimalFormat("#,###,###");
            String formattedQueue = formatter.format(mList.get(position).getQueue());
            holder.queue.setText(formattedQueue);
//            holder.listItem.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(MainActivity.this, ServerDetailActivity.class);
//                    intent.putExtra("serverId", String.valueOf(mList.get(position).getId()));
//                    intent.putExtra("serverName", String.valueOf(mList.get(position).getServer()));
//                    Log.d("serverId", String.valueOf(mList.get(position).getId()));
//                    startActivity(intent);
//                }
//            });
            int nQueue = mList.get(position).getQueue();
            if (nQueue > 3000) {
                holder.status.setText("혼잡");
                holder.status.setTextColor(Color.parseColor("#e74c3c"));
            } else if (nQueue > 1000) {
                holder.status.setText("앵간");
                holder.status.setTextColor(Color.parseColor("#f5c815"));
            } else {
                holder.status.setText("원활");
                holder.status.setTextColor(Color.parseColor("#27ae60"));
            }
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}
