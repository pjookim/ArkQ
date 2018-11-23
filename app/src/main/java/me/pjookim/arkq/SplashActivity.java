package me.pjookim.arkq;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        view = (ConstraintLayout) findViewById(R.id.constraint);

        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            Snackbar.make(view, "네트워크에 연결되어 있지 않습니다.", Snackbar.LENGTH_LONG).setAction("확인", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            }).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    overridePendingTransition(0, android.R.anim.fade_in);
//                    ((MainActivity)MainActivity.mContext).loadComplete();
                    finish();
                }
            }, 4000);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    ((MainActivity)MainActivity.mContext).loadComplete();
                    finish();
                }
            }, 2000);
        }
    }

    @Override
    public void onBackPressed() {
    }
}
