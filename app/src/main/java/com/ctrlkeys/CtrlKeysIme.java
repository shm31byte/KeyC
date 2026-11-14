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

public class CtrlKeysIme extends InputMethodService {
    private LinearLayout keyboard;
    private boolean numbersOnTop = false;
    private boolean symbols = false;
    private boolean shift = false;
    private boolean ctrl = false;

    @Override
    public View onCreateInputView() {
        return buildKeyboard();
    }

    private LinearLayout buildKeyboard() {
        keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setPadding(dp(4), dp(4), dp(4), dp(4));
        keyboard.setBackgroundColor(Color.rgb(225, 229, 235));

        // Tombol untuk menukar baris khusus dengan baris angka
        LinearLayout switchRow = row();
        addKey(
                switchRow,
                numbersOnTop ? "ABC" : "123",
                () -> {
                    numbersOnTop = !numbersOnTop;
                    refreshKeyboard();
                }
        );
        keyboard.addView(switchRow);

        // Baris Ctrl/Tab/Esc/panah atau angka 1–0
        LinearLayout topRow = row();

        if (numbersOnTop) {
            for (char c : "1234567890".toCharArray()) {
                addKey(topRow, String.valueOf(c), () -> type(String.valueOf(c)));
            }
        } else {
            addKey(
                    topRow,
                    ctrl ? "Ctrl ✓" : "Ctrl",
                    () -> {
                        ctrl = !ctrl;
                        refreshKeyboard();
                    }
            );
            addKey(topRow, "Tab", () -> sendKey(KeyEvent.KEYCODE_TAB));
            addKey(topRow, "Esc", () -> sendKey(KeyEvent.KEYCODE_ESCAPE));
            addKey(topRow, "←", () -> sendKey(KeyEvent.KEYCODE_DPAD_LEFT));
            addKey(topRow, "→", () -> sendKey(KeyEvent.KEYCODE_DPAD_RIGHT));
            addKey(topRow, "↑", () -> sendKey(KeyEvent.KEYCODE_DPAD_UP));
            addKey(topRow, "↓", () -> sendKey(KeyEvent.KEYCODE_DPAD_DOWN));
        }

        keyboard.addView(topRow);

        // Baris huruf atau simbol
        if (!symbols) {
            addChars("qwertyuiop");
            addChars("asdfghjkl");

            LinearLayout thirdRow = row();

            addKey(
                    thirdRow,
                    "⇧",
                    () -> {
                        shift = !shift;
                        refreshKeyboard();
                    }
            );

            for (char c : "zxcvbnm".toCharArray()) {
                addKey(thirdRow, String.valueOf(c), () -> typeLetter(c));
            }

            addKey(thirdRow, "⌫", () -> sendKey(KeyEvent.KEYCODE_DEL));
            keyboard.addView(thirdRow);
        } else {
            addChars("1234567890");
            addChars("@#$%&-*+=");
            addChars("()!?':;\"");
        }

        // Baris bawah
        LinearLayout bottomRow = row();

        addKey(
                bottomRow,
                symbols ? "ABC" : "?123",
                () -> {
                    symbols = !symbols;
                    refreshKeyboard();
                }
        );

        addKey(bottomRow, ",", () -> type(","));
        addKey(bottomRow, "spasi", () -> type(" "));
        addKey(bottomRow, ".", () -> type("."));
        addKey(bottomRow, "↵", this::enter);

        keyboard.addView(bottomRow);

        return keyboard;
    }

    private void addChars(String chars) {
        LinearLayout keyRow = row();

        for (char c : chars.toCharArray()) {
            addKey(keyRow, String.valueOf(c), () -> typeLetter(c));
        }

        keyboard.addView(keyRow);
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        return row;
    }

    private Button addKey(LinearLayout row, String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setPadding(0, 0, 0, 0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);

        int color = label.startsWith("Ctrl") && ctrl
                ? Color.rgb(120, 170, 235)
                : Color.WHITE;

        button.setBackground(roundRect(color));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, dp(48), 1f);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(button, params);

        button.setOnClickListener(view -> action.run());
        return button;
    }

    private GradientDrawable roundRect(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(7));
        return drawable;
    }

    private void typeLetter(char c) {
        if (ctrl) {
            int keyCode = letterKeyCode(Character.toUpperCase(c));

            if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                sendKeyWithCtrl(keyCode);
            }

            // Ctrl sengaja tidak dimatikan di sini.
            return;
        }

        char output = shift ? Character.toUpperCase(c) : c;
        shift = false;
        type(String.valueOf(output));
    }

    private int letterKeyCode(char c) {
        return KeyEvent.keyCodeFromString("KEYCODE_" + c);
    }

    private void type(String text) {
        InputConnection connection = getCurrentInputConnection();

        if (connection != null) {
            connection.commitText(text, 1);
        }
    }

    private void sendKey(int keyCode) {
        InputConnection connection = getCurrentInputConnection();

        if (connection == null) {
            return;
        }

        int metaState = ctrl ? KeyEvent.META_CTRL_ON : 0;
        long now = SystemClock.uptimeMillis();

        connection.sendKeyEvent(
                new KeyEvent(
                        now,
                        now,
                        KeyEvent.ACTION_DOWN,
                        keyCode,
                        0,
                        metaState
                )
        );

        connection.sendKeyEvent(
                new KeyEvent(
                        now,
                        SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_UP,
                        keyCode,
                        0,
                        metaState
                )
        );

        // Ctrl sengaja tetap aktif setelah key event.
    }

    private void sendKeyWithCtrl(int keyCode) {
        InputConnection connection = getCurrentInputConnection();

        if (connection == null) {
            return;
        }

        long now = SystemClock.uptimeMillis();
        int metaState = KeyEvent.META_CTRL_ON;

        connection.sendKeyEvent(
                new KeyEvent(
                        now,
                        now,
                        KeyEvent.ACTION_DOWN,
                        keyCode,
                        0,
                        metaState
                )
        );

        connection.sendKeyEvent(
                new KeyEvent(
                        now,
                        SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_UP,
                        keyCode,
                        0,
                        metaState
                )
        );
    }

    private void enter() {
        InputConnection connection = getCurrentInputConnection();

        if (connection == null) {
            return;
        }

        EditorInfo info = getCurrentInputEditorInfo();

        if (info != null) {
            int action = info.imeOptions & EditorInfo.IME_MASK_ACTION;

            if (action != EditorInfo.IME_ACTION_NONE
                    && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                connection.performEditorAction(action);
                return;
            }
        }

        sendKey(KeyEvent.KEYCODE_ENTER);
    }

    private void refreshKeyboard() {
        if (keyboard == null) {
            return;
        }

        setInputView(buildKeyboard());
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density
        );
    }
}
