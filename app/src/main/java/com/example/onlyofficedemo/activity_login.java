package com.example.onlyofficedemo;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.example.onlyofficedemo.Utils.UtilsKt.showToast;

public class activity_login extends Activity {
    Context appContext = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        appContext = getApplicationContext();

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        // привязываю обработчик кнопки логин, сперва проверка на наличия инета, иначе пишем нэт инэт
        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener((View view) -> {
            if (isOnline()) tryToLogin(rootView);
            else showToast("no internet", appContext);
        });

        // проверяем, если мы залогинены, то убиваю текущую активность и перехожу к следующей
    }

    public void tryToLogin(View view) {
        EditText editTextEmail = view.findViewById(R.id.editTextEmail);
        String loginEmailText = null;
        try {
            loginEmailText = editTextEmail.getText().toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }

        EditText passwordEditText = view.findViewById(R.id.editTextPassword);
        String passwordText = null;
        try {
            passwordText = passwordEditText.getText().toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isInputValid(loginEmailText, passwordText)) {
            // do login
            showToast("Begin Login Proccess", appContext);
        }
    }

    // провожу валидацию введенных данных в поля логин-пароль
    boolean isInputValid(String loginEmailText, String passwordText) {
        if (loginEmailText == null || loginEmailText.trim().isEmpty()) {
            showToast(getString(R.string.login_cant_be_empty_message), appContext);
            return false;
        }

        if (passwordText == null || passwordText.trim().isEmpty()) {
            showToast(getString(R.string.password_cant_be_empty_message), appContext);
            return false;
        }

        return true;
    }

    // проверяем есть ли интернеты эти ваши
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        if (cm != null) {
            netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected())
                return true;
        }

        return false;
    }


    @Override
    protected void onPause() {
        super.onPause();
    }
}
