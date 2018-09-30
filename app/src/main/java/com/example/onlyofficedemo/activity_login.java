package com.example.onlyofficedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rollbar.notifier.Rollbar;
import com.timejet.bio.timejet.UTILS.LoggedInUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URLEncoder;
import java.util.Objects;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static com.example.onlyofficedemo.Network.HTTP_requestsKt.http_loginOnlyOffice;
import static com.example.onlyofficedemo.Network.HTTP_requestsKt.isOnline;
import static com.example.onlyofficedemo.Network.HTTP_requestsKt.isTokenValid;
import static com.example.onlyofficedemo.Utils.UtilsKt.showToast;
import static com.rollbar.notifier.config.ConfigBuilder.withAccessToken;

public class activity_login extends Activity {
    private static final String TAG = "DEBUG";
    public static Context appContext = null;
    static final String ROLLBAR_API_KEY = "865f8071fa6f4a6397f2c82450422da3";
    public static final String APP_VERSION = "0.12";

    // Инициализирую Rollbar своим ключом и передаю текст текущей версии
    // ловить все непойманные исключения
    public static final Rollbar rollbar = new Rollbar(
            withAccessToken(ROLLBAR_API_KEY)
                    .environment("OnlyOffice Demo v." + APP_VERSION)
                    .handleUncaughtErrors(true)
                    .build());


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        appContext = getApplicationContext();

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        // если на старте логин активности уже в шаред есть ключ токена ненулевой длины,
        // то запускаю активность показать список файлов и папок
        String userToken = Objects.requireNonNull(LoggedInUser.Companion.getUser()).getUserToken();
        if (userToken != null && !userToken.isEmpty() && isTokenValid()) {
            startActivityList(this);
            // тут уже finish() сработал, дальше ничего не надо выполнять
        }


        // привязываю обработчик кнопки логин
        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener((View view) -> {
            // если есть инет, то пытаемся логиниться
            if (isOnline()) tryToLogin(rootView);
            else // иначе пишем нет инет
                showToast(getString(R.string.no_internet), appContext);
        });
    }

    // старт активности показать список и завершить текущую активити
    public final void startActivityList(Context context) {
        Intent intent = new Intent(context, activityFoldersFilesView.class);
        startActivity(intent);

        finish();
    }

    // идентификатор успешности логина юзера
    public final static int USER_SUCCESS_LOGIN = 12345;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEventJSONobject event) {
        // получаю от сервера список файлов и каталогов
        if (event == null) {
            showToast("network error, null json", getApplicationContext());
            return;
        }

        // это не моё событие, я жду только 1 событие успешный логин
        if (event.statusCode != USER_SUCCESS_LOGIN && event.response != null && event.header != null)
            return;

        // если пришел ответ от сервера, что юзер успешно залогинился,
        // запускаем активность список файлов и останавливаю текущую активность
        startActivityList(this);
    }

    // валидация емейла с помощью javax.mail validate
    // возвращает true если емейл валидный
    boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    String ui_getEncodedPortalName() {
        String portalName = null;
        try {
            // лезу в ui элемент
            EditText portalNameEditText = findViewById(R.id.editTextPortalName);
            // экранирую введенный урл
            portalName = URLEncoder.encode(portalNameEditText.getText().toString(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return portalName;
    }

    String ui_getValidEmail() {
        try {
            EditText editTextEmail = findViewById(R.id.editTextEmail);
            String email = editTextEmail.getText().toString();

            if (isValidEmailAddress(email)) return email;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    String ui_getRawPassword() {
        String passwordText = null;
        try {
            EditText passwordEditText = findViewById(R.id.editTextPassword);
            passwordText = passwordEditText.getText().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return passwordText;
    }

    // пытаемся логиниться
    public void tryToLogin(View view) {
        // Login Portal полный урл получаю из ui
        String portalName = ui_getEncodedPortalName();

        // Login EMAIL
        String loginEmail = ui_getValidEmail();

        // Password
        String password = ui_getRawPassword();

        // проверяю все три элемента на валидность
        if (isInputValid(loginEmail, password, portalName)) {
//            showToast("Begin Login Process", appContext); рисуем мультик
            try {
                // фильтр входных данных пройден, пытаемся логиниться
                // должен залогиниться один из трех
                http_loginOnlyOffice(loginEmail, password, portalName + ".onlyoffice.com");
                http_loginOnlyOffice(loginEmail, password, portalName + ".onlyoffice.eu");
                http_loginOnlyOffice(loginEmail, password, portalName + ".onlyoffice.sg");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // провожу валидацию введенных данных в поля логин-пароль
    boolean isInputValid(String loginEmailText, String passwordText, String portalName) {
        if (loginEmailText == null || loginEmailText.isEmpty()) {
            showToast(getString(R.string.login_cant_be_empty_message), appContext);
            showToast(getString(R.string.invalid_email), appContext);
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

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

}
