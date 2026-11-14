package com.ctrlkeys;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(android.os.Bundle state) {
        super.onCreate(state);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        TextView text = new TextView(this);
        text.setText("CtrlKeys\n\n1. Aktifkan CtrlKeys di pengaturan keyboard.\n"
                + "2. Kembali ke sini, lalu pilih CtrlKeys sebagai keyboard aktif.");
        text.setTextSize(18);
        layout.addView(text);

        Button settings = new Button(this);
        settings.setText("Buka pengaturan keyboard");
        settings.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        layout.addView(settings);

        Button picker = new Button(this);
        picker.setText("Pilih CtrlKeys");
        picker.setOnClickListener(v -> {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showInputMethodPicker();
        });
        layout.addView(picker);

        setContentView(layout);
    }
}
