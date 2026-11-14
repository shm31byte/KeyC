package com.ctrlkeys;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CtrlKeysIme extends InputMethodService {
    private LinearLayout keyboard;
    private boolean numbersOnTop = false;
    private boolean symbols = false;
    private boolean shift = false;
    private boolean ctrl = false;
    private Button ctrlButton;

    @Override
    public View onCreateInputView() {
        return buildKeyboard();
    }

    private LinearLayout buildKeyboard() {
        keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setPadding(dp(4), dp(4), dp(4), dp(4));
        keyboard.setBackgroundColor(Color.rgb(225, 229, 235));

        LinearLayout switchRow = row();
        addKey(switchRow, numbersOnTop ? "ABC" : "123",
                () -> {
                    numbersOnTop = !numbersOnTop;
                    refreshKeyboard();
                });
        keyboard.addView(switchRow);

        LinearLayout topRow = row();
        if (numbersOnTop) {
            for (char c : "1234567890".toCharArray()) {
                addKey(topRow, String.valueOf(c), () -> type(String.valueOf(c)));
            }
        } else {
            ctrlButton = addKey(topRow, ctrl ? "Ctrl ✓" : "Ctrl",
                    () -> {
                        ctrl = !ctrl;
                        refreshKeyboard();
                    });
            addKey(topRow, "Tab", () -> sendKey(KeyEvent.KEYCODE_TAB));
            addKey(topRow, "Esc", () -> sendKey(KeyEvent.KEYCODE_ESCAPE));
            addKey(topRow, "←", () -> sendKey(KeyEvent.KEYCODE_DPAD_LEFT));
            addKey(topRow, "→", () -> sendKey(KeyEvent.KEYCODE_DPAD_RIGHT));
            addKey(topRow, "↑", () -> sendKey(KeyEvent.KEYCODE_DPAD_UP));
            addKey(topRow, "↓", () -> sendKey(KeyEvent.KEYCODE_DPAD_DOWN));
        }
        keyboard.addView(topRow);

        if (!symbols) {
            addChars("qwertyuiop");
            addChars("asdfghjkl");

            LinearLayout third = row();
            addKey(third, "⇧", () -> {
                shift = !shift;
                refreshKeyboard();
            });
            for (char c : "zxcvbnm".toCharArray()) {
                addKey(third, String.valueOf(c), () -> typeLetter(c));
            }
            addKey(third, "⌫", () -> sendKey(KeyEvent.KEYCODE_DEL));
            keyboard.addView(third);
        } else {
            addChars("1234567890");
            addChars("@#$%&-*+=");
            addChars("()!?':;\"");
        }

        LinearLayout bottom = row();
        addKey(bottom, symbols ? "ABC" : "?123", () -> {
            symbols = !symbols;
            refreshKeyboard();
        });
        addKey(bottom, ",", () -> type(","));
        addKey(bottom, "spasi", () -> type(" "));
        addKey(bottom, ".", () -> type("."));
        addKey(bottom, "↵", this::enter);
        keyboard.addView(bottom);

        return keyboard;
    }

    private void addChars(String chars) {
        LinearLayout r = row();
        for (char c : chars.toCharArray()) {
            addKey(r, String.valueOf(c), () -> typeLetter(c));
        }
        keyboard.addView(r);
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        return r;
    }

    private Button addKey(LinearLayout row, String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setPadding(0, 0, 0, 0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setBackground(roundRect(
                label.startsWith("Ctrl") && ctrl
                        ? Color.rgb(120, 170, 235)
                        : Color.WHITE));

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, dp(48), 1f);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(b, lp);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private GradientDrawable roundRect(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(7));
        return d;
    }

    private void typeLetter(char c) {
        if (ctrl) {
            int code = letterKeyCode(Character.toUpperCase(c));
            ctrl = false;
            if (code != KeyEvent.KEYCODE_UNKNOWN) sendKeyWithCtrl(code);
            refreshKeyboard();
            return;
        }

        char out = shift ? Character.toUpperCase(c) : c;
        shift = false;
        type(String.valueOf(out));
    }

    private void type(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(text, 1);
    }

    private void sendKey(int keyCode) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        int meta = ctrl ? KeyEvent.META_CTRL_ON : 0;
        long now = SystemClock.uptimeMillis();
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                keyCode, 0, meta));
        ic.sendKeyEvent(new KeyEvent(now, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, keyCode, 0, meta));

        if (ctrl) {
            ctrl = false;
            refreshKeyboard();
        }
    }

    private void sendKeyWithCtrl(int keyCode) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        long now = SystemClock.uptimeMillis();
        int meta = KeyEvent.META_CTRL_ON;
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                keyCode, 0, meta));
        ic.sendKeyEvent(new KeyEvent(now, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, keyCode, 0, meta));
    }

    private int letterKeyCode(char c) {
        switch (c) {
            case 'A': return KeyEvent.KEYCODE_A;
            case 'C': return KeyEvent.KEYCODE_C;
            case 'V': return KeyEvent.KEYCODE_V;
            default: return KeyEvent.KEYCODE_UNKNOWN;
        }
    }

    private void enter() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        EditorInfo info = getCurrentInputEditorInfo();
        if (info != null) {
            int action = info.imeOptions & EditorInfo.IME_MASK_ACTION;
            if (action != EditorInfo.IME_ACTION_NONE
                    && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                ic.performEditorAction(action);
                return;
            }
        }
        sendKey(KeyEvent.KEYCODE_ENTER);
    }

    private void refreshKeyboard() {
        if (keyboard == null) return;
        View newKeyboard = buildKeyboard();
        setInputView(newKeyboard);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
