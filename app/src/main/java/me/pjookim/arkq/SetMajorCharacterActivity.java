package me.pjookim.arkq;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetMajorCharacterActivity extends AppCompatActivity {

    EditText input;
    ProgressDialog progressDialog;
    Boolean isUser;

    String character;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_major_character);

        input = (EditText) findViewById(R.id.input);

        Button confirm = (Button) findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!input.getText().equals("")) {
                    character = input.getText().toString();
                    new Character().execute();
                }
            }
        });
    }

    private class Character extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SetMajorCharacterActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("잠시 기다려 주세요.");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("http://lostark.game.onstove.com/Profile/Character/" + character).get();
                Elements profileCharacter = doc.select("div[class=profile-character]");
                if (!profileCharacter.isEmpty()) {
                    isUser = true;
                } else {
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
                SharedPreferences characterPre = getSharedPreferences("majorCharacter", MODE_PRIVATE);
                SharedPreferences.Editor editor = characterPre.edit();
                editor.putString("characterName", character);
                editor.commit();
                progressDialog.dismiss();
                finish();
            } else {
                Toast.makeText(SetMajorCharacterActivity.this, "없는 캐릭터라고 경고 띄우자", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
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
