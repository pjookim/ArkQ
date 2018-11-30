package me.pjookim.arkq;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class AbilityDetailDialog {

    private Context context;
    private StringBuffer notice;

    public AbilityDetailDialog(Context context) {
        this.context = context;
    }

    public void callFunction(String title, List<String> description) {

        final Dialog dlg = new Dialog(context);

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(R.layout.ability_detail_dialog);

        dlg.show();
        Window window = dlg.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        final TextView titleText = (TextView) dlg.findViewById(R.id.title);
        final TextView message = (TextView) dlg.findViewById(R.id.mesgase);
        final Button okButton = (Button) dlg.findViewById(R.id.okButton);

        notice = new StringBuffer("");
        titleText.setText(title);

        for (String temp : description) {
            if (!description.isEmpty()) {
                notice.append("• ");
                notice.append(temp);
                notice.append("\n");
            }
        }

        message.setText(notice);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.dismiss();
            }
        });
    }
}