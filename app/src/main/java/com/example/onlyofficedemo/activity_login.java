package com.example.onlyofficedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.timejet.bio.timejet.UTILS.LoggedInUser;
import com.timejet.bio.timejet.UTILS.User;

import org.joda.time.DateTime;

import java.net.URLEncoder;
import java.util.Objects;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static com.example.onlyofficedemo.Network.HTTP_requestsKt.getMyDocuments;
import static com.example.onlyofficedemo.Network.HTTP_requestsKt.loginOnlyOffice;
import static com.example.onlyofficedemo.Utils.UtilsKt.isOnline;
import static com.example.onlyofficedemo.Utils.UtilsKt.saveUserToSharedPrefs;
import static com.example.onlyofficedemo.Utils.UtilsKt.showToast;

public class activity_login extends Activity {
    private static final String TAG = "DEBUG";
    public static Context appContext = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        appContext = getApplicationContext();

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        String userToken = Objects.requireNonNull(LoggedInUser.Companion.getUser()).getUserToken();
        if (userToken != null && !userToken.isEmpty()){
            Intent intent = new Intent(this, activityFoldersFilesView.class);
            startActivity(intent);
        }

        //isTokenValid();
        try {
            getMyDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
//            loginProcess("1", "1", "1");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // привязываю обработчик кнопки логин, сперва проверка на наличия инета, иначе пишем нэт инэт
        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener((View view) -> {
            if (isOnline()) tryToLogin(rootView);
            else showToast("no internet", appContext);
        });
     // проверяем, если мы залогинены, то убиваю текущую активность и перехожу к следующей
    }


    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }


    public void tryToLogin(View view) {
        // Login Portal
        String portalName = null;
        try {
            EditText portalNameEditText = view.findViewById(R.id.editTextPortalName);
            portalName = URLEncoder.encode(portalNameEditText.getText().toString(), "UTF-8");

            //executeCommand(portalName);
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Login EMAIL
        String loginEmailText = null;
        try {
            EditText editTextEmail = view.findViewById(R.id.editTextEmail);
            loginEmailText = editTextEmail.getText().toString();

            if (!isValidEmailAddress(loginEmailText)) {
                showToast(getString(R.string.invalid_email), appContext);
                return; // уходим
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Login Password
        String passwordText = null;
        try {
            EditText passwordEditText = view.findViewById(R.id.editTextPassword);
            passwordText = passwordEditText.getText().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (isInputValid(loginEmailText, passwordText, portalName)) {
            // do loginOnlyOffice
//            showToast("Begin Login Process", appContext); рисуем мультик
            try {
                //loginProcess (loginEmailText, passwordText, portalName);
                loginOnlyOffice(loginEmailText, passwordText, portalName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void resetToken(){
        // время вышло, оставляем имя портала и емейл и стираю Токен и его Дату-Время
        saveUserToSharedPrefs(appContext,
                new User(LoggedInUser.Companion.getUser().getUserPortal(), LoggedInUser.Companion.getUser().getUserEmail(), "", ""));
    }

    // проверка времени токена на валидность в сравнении с текущей датой/время
    public boolean isTokenValid(){
        // 2019-09-24T01:36:13.8831347+03:00
        String userTokenExpires = Objects.requireNonNull(LoggedInUser.Companion.getUser()).getUserToken();

        try { //parse by joda time
            // сохраненное дата-время токена
            DateTime dtToken = DateTime.parse(userTokenExpires);
            long dtTokenMillis = dtToken.getMillis();

            // текущее дата-время
            DateTime dtNow = new DateTime();
            long dateTimeNowMillis = dtNow.getMillis();

            // проверяем не вышло ли время токена
            if (dtTokenMillis >= dateTimeNowMillis) {
                showToast("Token Valid", appContext);
                return true;
            } else {
                showToast("Token Invalid", appContext);
                // делаем релогин, оставляю емейл, стираю токен и время жизни
                resetToken();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }





    // провожу валидацию введенных данных в поля логин-пароль
    boolean isInputValid(String loginEmailText, String passwordText, String portalName) {
        if (loginEmailText == null || loginEmailText.isEmpty()) {
            showToast(getString(R.string.login_cant_be_empty_message), appContext);
            return false;
        }

        if (passwordText == null || passwordText.isEmpty()) {
            showToast(getString(R.string.password_cant_be_empty_message), appContext);
            return false;
        }

        if (portalName == null || portalName.isEmpty()) {
            showToast(getString(R.string.portal_name_cant_be_empty_message), appContext);
            return false;
        }

        return true;
    }



    @Override
    protected void onPause() {
        super.onPause();

    }
}
