package me.pjookim.arkq;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    AlertDialog.Builder builder;
    String[] serverName = {"루페온", "이그하람", "기에나", "시리우스", "크라테르", "프로키온", "알데바란", "아크투르스", "안타레스", "베아트리스", "에버그레이스"};
    CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private ArrayList<ItemObject> list = new ArrayList();
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseReference mDatabase;
    private AdView mAdView;
    private TextView realTimeText;
    private DatabaseReference androidRef;
    private Boolean backKeyPressed = false;

    CardView noSetCharacter;
    CardView characterProfileCard;
    CardView adCard;
    CardView noCharacterCard;
    CardView rankingCard;

    String character;
    String nickname;
    String charClass;
    String itemLevel;
    String expeditionLevel;
    String pvpLevel;
    TextView rankText;
    TextView nicknameText;
    TextView itemText;
    TextView expeditionText;
    TextView pvpText;
    String resultRank;
    ImageView profileImage;
    RecyclerView basicAbilityView;
    RecyclerView battleAbilityView;
    Boolean isUser;
    ImageView rankImage;

    ImageView aboutRank;
    TickerView rank1;
    TickerView rank2;

    Retrofit retrofit;
    CharacterRank apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rankText = (TextView) findViewById(R.id.rank_name);
        rankImage = (ImageView) findViewById(R.id.rank_image);

        aboutRank = (ImageView) findViewById(R.id.about_rank);
        aboutRank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AboutRankDialog customDialog = new AboutRankDialog(MainActivity.this);
                customDialog.callFunction();
            }
        });

        rankingCard = (CardView) findViewById(R.id.ranking);

        rank1 = findViewById(R.id.rank1);
        rank1.setCharacterLists(TickerUtils.provideNumberList());

        rank1.setText("0");
        rank2 = findViewById(R.id.rank2);
        rank2.setCharacterLists(TickerUtils.provideNumberList());
        rank2.setText("%");

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Character().execute();
            }
        });

        characterProfileCard = (CardView) findViewById(R.id.character_profile);

        profileImage = (ImageView) findViewById(R.id.profile_image);
        nicknameText = (TextView) findViewById(R.id.nickname);

        itemText = (TextView) findViewById(R.id.item);
        expeditionText = (TextView) findViewById(R.id.expedition);
        pvpText = (TextView) findViewById(R.id.pvp);



        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        noSetCharacter = (CardView) findViewById(R.id.no_set_character);

        characterProfileCard = (CardView) findViewById(R.id.character_profile);
        rankingCard = (CardView) findViewById(R.id.ranking);

        noSetCharacter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(MainActivity.this, SetMajorCharacterActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences pref = getSharedPreferences("majorCharacter", MODE_PRIVATE);
        character = pref.getString("characterName", null);

        if(character != null) {
            new Character().execute();
            noSetCharacter.setVisibility(View.GONE);
        } else {
        }

        final Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_more_vert_black_24dp);

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
                getServerList();
            }
        });
        getServerList();

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(CharacterRank.BASEURL)
                .build();
        apiService = retrofit.create(CharacterRank.class);

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

        final EditText searchCharacter = (EditText) findViewById(R.id.search_char);
        ImageView searchButton = (ImageView) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchCharacterActivity.class);
                if (searchCharacter.getText().length() > 0) {
                    intent.putExtra("character", searchCharacter.getText().toString());
                    Log.i("a", searchCharacter.getText().toString());
                    startActivity(intent);
                }
            }
        });

        searchCharacter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        Intent intent = new Intent(MainActivity.this, SearchCharacterActivity.class);
                        if (searchCharacter.getText().length() > 0) {
                            intent.putExtra("character", searchCharacter.getText().toString());
                            Log.i("a", searchCharacter.getText().toString());
                            startActivity(intent);
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void getServerList() {
        list.clear();
        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ServerQueue.BASEURL)
                .build();
        ServerQueue apiService = retrofit.create(ServerQueue.class);
        Call<JsonObject> call = apiService.getQueue("get");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    if (object != null) {
                        for (int i = 1; i < 12; i++) {
                            list.add(new ItemObject(serverName[i - 1], Integer.parseInt(object.get("s" + String.valueOf(i)).toString()), i));
                        }
                        MyAdapter myAdapter = new MyAdapter(list);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(myAdapter);

                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String getTime = sdf.format(date);
                        realTimeText.setText(getTime + " 기준");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backKeyPressed) {
            finish(); // finish activity
        } else {
            Snackbar.make(coordinatorLayout, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Snackbar.LENGTH_LONG).show();
            backKeyPressed = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backKeyPressed = false;
                }
            }, 3 * 1000);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.queue_row_data, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.serverTitle.setText(String.valueOf(mList.get(position).getServer()));
            DecimalFormat formatter = new DecimalFormat("#,###,###");
            String formattedQueue = formatter.format(mList.get(position).getQueue());
            holder.queue.setText(formattedQueue);
            holder.listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ServerDetailActivity.class);
                    intent.putExtra("serverId", String.valueOf(mList.get(position).getId()));
                    intent.putExtra("serverName", String.valueOf(mList.get(position).getServer()));
                    Log.d("serverId", String.valueOf(mList.get(position).getId()));
                    startActivity(intent);
                }
            });
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

    private class Character extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("http://lostark.game.onstove.com/Profile/Character/" + character).get();
                Elements profileCharacter = doc.select("div[class=profile-character]");
                if (!profileCharacter.isEmpty()) {
                    isUser = true;
                    nickname = profileCharacter.select("h3").text();
                    Log.i("nickname", nickname);
                    charClass = profileCharacter.select("div[class=game-info__class] > span").get(1).text();
                    Log.i("class", charClass);
                    itemLevel = profileCharacter.select("div[class=level-info__item] > span").get(1).text();
                    Log.i("itemLevel", itemLevel);
                    expeditionLevel = profileCharacter.select("div[class=level-info__expedition] > span").get(1).text();
                    Log.i("expeditionLevel", expeditionLevel);
                    pvpLevel = profileCharacter.select("div[class=level-info__pvp] > span").get(1).text();
                    Log.d("profileCharacter", profileCharacter.toString());
                } else {
                    Log.d("이건 없음", "ㅇㅇㅠㅠ");
                    isUser = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isUser) {
                Call<JsonObject> call = apiService.getRanking(nickname, itemLevel.substring(3));
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            JsonObject object = response.body();
                            if (object != null) {
                                resultRank = object.get("rank").toString().replaceAll("\"", "");
                                if(!resultRank.equals("-1")) {
                                    String[] data = resultRank.split("\\.");
                                    rank1.setText(data[0]);
                                    rank2.setText("." + data[1] + "%");
                                    if (Integer.parseInt(data[0]) < 2) {
                                        rankText.setText("다이아");
                                        rankImage.setImageDrawable(getResources().getDrawable(R.drawable.dia));
                                    } else if (Integer.parseInt(data[0]) < 8) {
                                        rankText.setText("플래티넘");
                                        rankImage.setImageDrawable(getResources().getDrawable(R.drawable.platinum));
                                    } else if (Integer.parseInt(data[0]) < 30) {
                                        rankText.setText("골드");
                                        rankImage.setImageDrawable(getResources().getDrawable(R.drawable.gold));
                                    } else if (Integer.parseInt(data[0]) < 60) {
                                        rankText.setText("실버");
                                        rankImage.setImageDrawable(getResources().getDrawable(R.drawable.silver));
                                    } else {
                                        rankText.setText("브론즈");
                                        rankImage.setImageDrawable(getResources().getDrawable(R.drawable.bronze));
                                    }
                                    rankingCard.setVisibility(View.VISIBLE);
                                }
                                swipeRefreshLayout.setRefreshing(false);

                            }
                            Log.i("rank", resultRank);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Log.i("아 제발ㅠㅠ", t.toString());
                    }
                });

                nicknameText.setText(nickname);
                itemText.setText(itemLevel);
                expeditionText.setText(expeditionLevel);
                pvpText.setText(pvpLevel);
                switch (charClass) {
                    case "기공사": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c1));
                        break;
                    }
                    case "데빌헌터": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c2));
                        break;
                    }
                    case "디스트로이어": {
                        Log.i("디스트로이어", "ㅇㅇㅇ");
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c3));
                        break;
                    }
                    case "바드": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c4));
                        break;
                    }
                    case "배틀마스터": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c5));
                        break;
                    }
                    case "버서커": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c6));
                        break;
                    }
                    case "블래스터": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c7));
                        break;
                    }
                    case "서머너": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c8));
                        break;
                    }
                    case "아르카나": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c9));
                        break;
                    }
                    case "워로드": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c10));
                        break;
                    }
                    case "인파이터": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c11));
                        break;
                    }
                    case "호크아이": {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.c12));
                        break;
                    }
                    default: {
                        Log.i("띠용", "엥 이러면 안돼ㅠㅠ");
                    }
                }
                characterProfileCard.setVisibility(View.VISIBLE);
            } else {
                noCharacterCard.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
            //ArraList를 인자로 해서 어답터와 연결한다.
//            MyAdapter myAdapter = new MyAdapter(list);
//            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
//            recyclerView.setLayoutManager(layoutManager);
//            recyclerView.setAdapter(myAdapter);
//
            //Glide.with(SearchCharacterActivity.this).load("http://arkq.cafe24app.com/icons/" + charClass + ".png").into(profileImage);
//
//            long now = System.currentTimeMillis();
//            Date date = new Date(now);
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String getTime = sdf.format(date);
//            realTimeText.setText(getTime + " 기준");
        }

    }

}
