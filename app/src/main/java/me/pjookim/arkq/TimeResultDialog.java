package me.pjookim.arkq;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;

public class TimeResultDialog extends Dialog implements View.OnClickListener{
    private static final int LAYOUT = R.layout.time_result_dialog;

    private Context context;

    private TextInputEditText nameEt;
    private TextInputEditText emailEt;

    private TextView cancelTv;
    private TextView searchTv;

    private String name;

    public TimeResultDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public TimeResultDialog(Context context,String name){
        super(context);
        this.context = context;
        this.name = name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            case R.id.findPwDialogCancelTv:
//                cancel();
//                break;
//            case R.id.findPwDialogFindTv:
//                break;
        }
    }
}