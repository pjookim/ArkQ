package me.pjookim.arkq;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DatabaseReference;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchCharacter extends AppCompatActivity {

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
    TextView nicknameText;
    TextView itemText;
    TextView expeditionText;
    TextView pvpText;
    ImageView profileImage;
    RecyclerView basicAbilityView;
    RecyclerView battleAbilityView;
    private DatabaseReference serverRef;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private ArrayList<AbilityItem> basicAbility = new ArrayList<>();
    private ArrayList<AbilityItem> battleAbility = new ArrayList<>();

    Boolean isUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_character);

        basicAbilityView = (RecyclerView) findViewById(R.id.ability_basic);
        battleAbilityView = (RecyclerView) findViewById(R.id.ability_battle);

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

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ServerDetail.BASEURL)
                .build();
        ServerDetail apiService = retrofit.create(ServerDetail.class);
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        sdf = new SimpleDateFormat("yyyyMMdd HHmmss");

        final String getTime = sdf.format(date);

        final EditText searchCharacter = (EditText) findViewById(R.id.search_char);
        ImageView searchButton = (ImageView) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchCharacter.this, SearchCharacter.class);
                if(searchCharacter.getText().length()>0) {
                    intent.putExtra("character", searchCharacter.getText().toString());
                    Log.i("a", searchCharacter.getText().toString());
                    startActivity(intent);
                    finish();
                }
            }
        });

        new Character().execute();
    }

    private class Character extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(SearchCharacter.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("잠시 기다려 주세요.");
            progressDialog.show();

            basicAbility.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("http://lostark.game.onstove.com/Profile/Character/" + character).get();
                Elements profileCharacter = doc.select("div[class=profile-character]");
                if(!profileCharacter.isEmpty()) {
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
                            basicAbility.add(new AbilityItem(ability, value));
                        }
                    }

                    Elements ulBattleAbility = doc.select("div[class=profile-ability-battle]>ul").select("li");

                    for (Element elem : ulBattleAbility) {
                        if (!elem.select("span").isEmpty()) {
                            String ability = elem.select("span").get(0).text();
                            String value = elem.select("span").get(1).text();
                            battleAbility.add(new AbilityItem(ability, value));
                        }
                    }
                    Log.d("profileCharacter", profileCharacter.toString());
                } else {
                    Log.d("이건 없음", "ㅇㅇㅠㅠ");
                    isUser=false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(isUser) {
                BasicAdapter basicAdapter = new BasicAdapter(basicAbility);
                basicAbilityView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                basicAbilityView.setAdapter(basicAdapter);

                BattleAdapter battleAdapter = new BattleAdapter(battleAbility);
                battleAbilityView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                battleAbilityView.setAdapter(battleAdapter);

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
            } else {
                Log.i("이건 없음", "ㅇㅇ");
            }
            //ArraList를 인자로 해서 어답터와 연결한다.
//            MyAdapter myAdapter = new MyAdapter(list);
//            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
//            recyclerView.setLayoutManager(layoutManager);
//            recyclerView.setAdapter(myAdapter);
//
            progressDialog.dismiss();
            //Glide.with(SearchCharacter.this).load("http://arkq.cafe24app.com/icons/" + charClass + ".png").into(profileImage);
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
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}