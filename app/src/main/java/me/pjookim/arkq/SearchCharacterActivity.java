package me.pjookim.arkq;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchCharacterActivity extends AppCompatActivity {

    List<Integer> queues = new ArrayList<>();
    List<String> timestamp = new ArrayList<>();
    SimpleDateFormat sdf;
    Date resultTime;
    TimePicker timePicker;
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
    private DatabaseReference serverRef;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private ArrayList<AbilityItem> basicAbility = new ArrayList<>();
    private ArrayList<AbilityItem> battleAbility = new ArrayList<>();

    CardView characterProfileCard;
    CardView adCard;
    CardView noCharacterCard;
    CardView rankingCard;

    Retrofit retrofit;
    CharacterRank apiService;

    SwipeRefreshLayout swipeRefreshLayout;

    TickerView rank1;
    TickerView rank2;

    ImageView aboutRank;
    ImageView rankImage;

    int cnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_character);

        rankText = (TextView) findViewById(R.id.rank_name);
        rankImage = (ImageView) findViewById(R.id.rank_image);

        aboutRank = (ImageView) findViewById(R.id.about_rank);
        aboutRank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AboutRankDialog customDialog = new AboutRankDialog(SearchCharacterActivity.this);
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

        basicAbilityView = (RecyclerView) findViewById(R.id.ability_basic);


        battleAbilityView = (RecyclerView) findViewById(R.id.ability_battle);

        characterProfileCard = (CardView) findViewById(R.id.character_profile);
        adCard = (CardView) findViewById(R.id.ad_card);

        noCharacterCard = (CardView) findViewById(R.id.no_character);

        profileImage = (ImageView) findViewById(R.id.profile_image);
        nicknameText = (TextView) findViewById(R.id.nickname);

        itemText = (TextView) findViewById(R.id.item);
        expeditionText = (TextView) findViewById(R.id.expedition);
        pvpText = (TextView) findViewById(R.id.pvp);

        character = getIntent().getStringExtra("character");

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

        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Ark Q");

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(CharacterRank.BASEURL)
                .build();
        apiService = retrofit.create(CharacterRank.class);
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        sdf = new SimpleDateFormat("yyyyMMdd HHmmss");

        final String getTime = sdf.format(date);

        final EditText searchCharacter = (EditText) findViewById(R.id.search_char);
        ImageView searchButton = (ImageView) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchCharacterActivity.this, SearchCharacterActivity.class);
                if (searchCharacter.getText().length() > 0) {
                    intent.putExtra("character", searchCharacter.getText().toString());
                    Log.i("a", searchCharacter.getText().toString());
                    startActivity(intent);
                    finish();
                }
            }
        });

        searchCharacter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        Intent intent = new Intent(SearchCharacterActivity.this, SearchCharacterActivity.class);
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

        new Character().execute();

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adCard.setVisibility(View.VISIBLE);
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

    private class Character extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            basicAbility.clear();
            battleAbility.clear();
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

                    Elements ulBasicAbility = doc.select("div[class=profile-ability-basic]>ul").select("li");

                    for (Element elem : ulBasicAbility) {
                        if (!elem.select("span").isEmpty()) {
                            String ability = elem.select("span").get(0).text();
                            String value = elem.select("span").get(1).text();
                            Elements ulBasicAbilityTooltip = elem.select("div[class=profile-ability-tooltip]>ul").select("li");

                            List<String> temp = new ArrayList<String>();
                            for (Element tooltip : ulBasicAbilityTooltip) {
                                if (!tooltip.text().isEmpty()) {
                                    temp.add(tooltip.text());
                                }
                            }
                            basicAbility.add(new AbilityItem(ability, value, temp));
                        }

                    }

                    Elements ulBattleAbility = doc.select("div[class=profile-ability-battle]>ul").select("li");

                    for (Element elem : ulBattleAbility) {
                        if (!elem.select("span").isEmpty()) {
                            String ability = elem.select("span").get(0).text();
                            String value = elem.select("span").get(1).text();
                            Elements ulBattleAbilityTooltip = elem.select("div[class=profile-ability-tooltip]>ul").select("li");

                            List<String> temp = new ArrayList<String>();
                            for (Element tooltip : ulBattleAbilityTooltip) {
                                if (!tooltip.text().isEmpty()) {
                                    temp.add(tooltip.text());
                                }
                            }
                            battleAbility.add(new AbilityItem(ability, value, temp));
                        }
                    }
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
                cnt=0;
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
                BasicAdapter basicAdapter = new BasicAdapter(basicAbility);
                basicAbilityView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                basicAbilityView.setAdapter(basicAdapter);

                BattleAdapter battleAdapter = new BattleAdapter(battleAbility);
                battleAbilityView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                battleAbilityView.setAdapter(battleAdapter);

                    Log.i("랭킹랭킹", "ㅇㅇ");

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

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView ability, value;
        private ConstraintLayout listItem;

        public ViewHolder(View itemView) {
            super(itemView);

            ability = (TextView) itemView.findViewById(R.id.ability);
            value = (TextView) itemView.findViewById(R.id.value);
            listItem = (ConstraintLayout) itemView.findViewById(R.id.list_item);
        }
    }

    class BasicAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final ArrayList<AbilityItem> mList;

        public BasicAdapter(ArrayList<AbilityItem> list) {
            this.mList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ability_row_data, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.ability.setText(basicAbility.get(position).getAbility());
            holder.value.setText(basicAbility.get(position).getValue());
            holder.listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AbilityDetailDialog customDialog = new AbilityDetailDialog(SearchCharacterActivity.this);

                    // 커스텀 다이얼로그를 호출한다.
                    // 커스텀 다이얼로그의 결과를 출력할 TextView를 매개변수로 같이 넘겨준다.

                    customDialog.callFunction(basicAbility.get(position).getAbility(), basicAbility.get(position).getDescription());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }

    class BattleAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final ArrayList<AbilityItem> mList;

        public BattleAdapter(ArrayList<AbilityItem> list) {
            this.mList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ability_row_data, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.ability.setText(battleAbility.get(position).getAbility());
            holder.value.setText(battleAbility.get(position).getValue());
            holder.listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AbilityDetailDialog customDialog = new AbilityDetailDialog(SearchCharacterActivity.this);

                    // 커스텀 다이얼로그를 호출한다.
                    // 커스텀 다이얼로그의 결과를 출력할 TextView를 매개변수로 같이 넘겨준다.

                    customDialog.callFunction(battleAbility.get(position).getAbility(), battleAbility.get(position).getDescription());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}