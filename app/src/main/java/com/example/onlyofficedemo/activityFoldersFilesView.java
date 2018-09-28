package com.example.onlyofficedemo;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.onlyofficedemo.Utils.JSONhelper;
import com.squareup.picasso.Picasso;
import com.timejet.bio.timejet.UTILS.CurrentFolder;
import com.timejet.bio.timejet.UTILS.LoggedInUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.example.onlyofficedemo.Network.HTTP_requestsKt.http_getDocuments;
import static com.example.onlyofficedemo.Network.HTTP_requestsKt.http_GetMyselfInfoSaveShared;

import static com.example.onlyofficedemo.Utils.UtilsKt.resetMyToken;
import static com.example.onlyofficedemo.Utils.UtilsKt.showToast;

public class activityFoldersFilesView extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        NavigationView.OnNavigationItemSelectedListener,
        NavigationView.OnCreateContextMenuListener {
    ArrayList<FolderFileListElement> folderFiles = new ArrayList<>();
    private LayoutInflater inflater;
    SwipeRefreshLayout mSwipeRefreshLayout;

    // кнопка андроида назад
    @Override
    public void onBackPressed() {
        // если открыто боковое меню, закрываем
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // относительные пути хттп запросов
    public final static String HTTP_RELATIVE_PATH_MY_DOCUMENTS = "/api/2.0/files/@my.json";
    public final static String HTTP_RELATIVE_PATH_COMMON_DOCUMENTS = "/api/2.0/files/@common.json";
    public final static String HTTP_RELATIVE_PATH_SELF_DOCUMENTS = "/api/2.0/people/@self.json";
    public final static String HTTP_RELATIVE_PATH_TO_FILES = "/api/2.0/files/";


    @Override
    // ловим нажатие на пункты бокового меню
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // находим текстовое поле textViewFolderName класса TextView
        //
        TextView textViewFolderName = findViewById(R.id.textViewFolderName);

        switch (id) {
            // кликаю получить список моих документов
            case R.id.nav_my_documents:
                try {
                    // http асинхронный запрос
                    http_getDocuments(HTTP_RELATIVE_PATH_MY_DOCUMENTS);

                    // и тут же по антипаттерну меняю gui
                    textViewFolderName.setText(getString(R.string.my_documents));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            // кликаю получить список общих документов
            case R.id.nav_common_documents:
                try {
                    http_getDocuments(HTTP_RELATIVE_PATH_COMMON_DOCUMENTS);

                    // опять меняю гуй
                    textViewFolderName.setText(getString(R.string.common_documents));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            // клик логаут
            case R.id.nav_logout:
                // в шареде чищу токен, время жизни, полное имя и аватар
                resetMyToken();

                // запускаю логин экран
                startActivity(new Intent(getApplicationContext(), activity_login.class));

                // текущая активность завершается, чтобы в очереди назад не висела
                finish();
                break;

            default:
                break;
        }

        // закрываем меню
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        // уходим
        return true;
    }

    void ui_ActivateToolbar() {
        // активация тулбара, боковое меню
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        try {
            setSupportActionBar(toolbar);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // тулбар
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    void ui_ToolbarItemClickListener() {
        // слушатель нажатия на пункты бокового меню
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    void ui_SwipeOnRefreshListener() {
        // SwipeRefreshLayout, тянем вниз для обновления, вызов onRefresh()
        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(() -> onRefresh());
        // цвета обновления
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
    }

    void http_DownloadLastOpenedFolder(){
        // при первом запуске последний путь будет равен урл HTTP_RELATIVE_PATH_TO_FILES
        String folderID = relativeIDurl(CurrentFolder.Companion.getCurrentFolder().getFolderID());
        if (!folderID.equals(HTTP_RELATIVE_PATH_TO_FILES))
            try {
                http_getDocuments(folderID);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    // при запуске активности
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.listfoldersfiles);

        http_GetMyselfInfoSaveShared();

        ui_ActivateToolbar();

        ui_ToolbarItemClickListener();

        ui_SwipeOnRefreshListener();

        http_DownloadLastOpenedFolder ();

    }


    // обновляю вид, но может еще notify нужен, пока так работает
    void ui_UpdateRecyclerView(ArrayList<FolderFileListElement> folderFiles) {
        RecyclerView recyclerView = findViewById(R.id.list);
        // создаем адаптер
        DataAdapter adapter = new DataAdapter(this, folderFiles);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // регистрируем eventbus
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        // отключаем eventbus
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    void ui_UpdateUserInfoToolbarMenu(){
        // GUI обновление информации о пользователе, имя, емейл, аватарка в боковом меню
        String userEmail = LoggedInUser.Companion.getUser().getUserEmail();
        String userName = LoggedInUser.Companion.getUser().getUserName();
        String avatar = LoggedInUser.Companion.getUser().getAvatarPicUrl();
        if (userEmail != null &&
                userName != null &&
                avatar != null) {

            ImageView avatarIV = findViewById(R.id.imageViewAvatar);
            TextView TVuserName = findViewById(R.id.TVuserName);
            TextView TVemail = findViewById(R.id.TVemail);

            // avatar picasso loader
            Picasso.get().load(avatar).into(avatarIV);

            TVuserName.setText(userName);
            TVemail.setText(userEmail);
        }
    }

    // ловлю события от eventbus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEventJSONobject event) {
        // опять gui, отключаем показ обновления значка
        mSwipeRefreshLayout.setRefreshing(false);

        // на всякий случай сперва на null
        if (event == null) {
            showToast("network error, null json", getApplicationContext());
            return;
        }

        // если в ответе есть код 200 хттп ок, то считаю что это пакет о списке каталогом и файлов
        if (event.statusCode == 200) {
            ui_UpdateFolderFilesRecyclerView(event);
        } else {
            showToast("error code: " + event.statusCode, getApplicationContext());
        }

        ui_UpdateUserInfoToolbarMenu();
    }


    // обновляю список
    void ui_UpdateFolderFilesRecyclerView(MessageEventJSONobject event) {
        // предполагаю, что пришел нормальный ответ, statusCode и header - это http информация
        // response содержит json ответ о списке каталогов и файлов
        int statusCode = event.statusCode;
        List<? extends Header> headers = event.header;
        JSONObject jsonObject = event.response;

        ArrayList<FolderFileListElement> folderFiles = new ArrayList<>();

        // создаю список каталогов из ответа
        JSONArray folders = new JSONhelper(jsonObject).getFolders();

        // рисую назад кнопку to parent folder
        try {
            folderFiles.add(new FolderFileListElement(getString(R.string.to_parent_folder), "", R.drawable.to_parent));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // каталоги,
        for (int i = 0; i < folders.length(); i++) {
            try {
                // выделяю json каталог из ответа
                JSONObject objFolder = folders.getJSONObject(i);

                // полупарсер id каталога для вставки в интерфейс для хождения по папкам
                String folderID = "";
                try {
                    folderID = objFolder.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONhelper obj = new JSONhelper(objFolder);
                String folderTitle = obj.getTitleFolderFile();
                String filesCountInFolder = obj.getFilesCountInFolder();
                String foldersCountInFolder = obj.getFoldersCountInFolder();

                folderFiles.add(new FolderFileListElement(folderTitle,
                        getString(R.string.documents)
                                + " "
                                + filesCountInFolder
                                + " | "
                                + getString(R.string.subfolders)
                                + " "
                                + foldersCountInFolder
                                + "\nid:"
                                + " "
                                + folderID
                        , R.drawable.folder_image));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // файлы список
        JSONArray files = new JSONhelper(jsonObject).getFiles();
        for (int i = 0; i < files.length(); i++) {
            try {
                JSONObject objFile = files.getJSONObject(i);
                JSONhelper obj = new JSONhelper(objFile);

                int icon = 0;
                String fname = obj.getTitleFolderFile();

                // урл картинки в ответе сервера содержится
                if (fname.contains(".doc")) icon = R.drawable.docx_win_icon;
                if (fname.contains(".xls")) icon = R.drawable.xlsx_win_icon;
                if (fname.contains(".ppt")) icon = R.drawable.pptx_icon;

                folderFiles.add(new FolderFileListElement(fname, "", icon));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // обновляем список recyclerview
        ui_UpdateRecyclerView(folderFiles);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRefresh() {
        // в процессе обновления
        mSwipeRefreshLayout.setRefreshing(true);

        // хттп запрос в зависимости от типа документов
        try {
            // мои документы
            if (CurrentFolder.Companion.getCurrentFolder().getName().equals(getString(R.string.my_documents))) {
                http_getDocuments(HTTP_RELATIVE_PATH_MY_DOCUMENTS);
            } else
                // общие документы
                if (CurrentFolder.Companion.getCurrentFolder().getName().equals(getString(R.string.common_documents))) {
                    http_getDocuments(HTTP_RELATIVE_PATH_COMMON_DOCUMENTS);
                } else {
                    // вызов списка папок и файлов по ID каталога
                    http_getDocuments(relativeIDurl(CurrentFolder.Companion.getCurrentFolder().getFolderID()));
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // на входе id
    // на выходе /api/2.0/files/{id}
    String relativeIDurl(String id) {
        return (HTTP_RELATIVE_PATH_TO_FILES + id);
    }

    // адаптер между RecyclerView и моим самодельным списком
    class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> implements View.OnClickListener {
        //private LayoutInflater inflater;
        private List<FolderFileListElement> folderFiles;

        DataAdapter(Context context, List<FolderFileListElement> folderFiles) {
            this.folderFiles = folderFiles;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.list_item, parent, false);
            view.setOnClickListener(this);

            return new ViewHolder(view);
        }

        @Override
        // вставка одного элемента из объекта списка FolderFileListElement
        public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {
            FolderFileListElement folderFile = this.folderFiles.get(position);

            holder.imageView.setImageResource(folderFile.getImage());
            holder.title1.setText(folderFile.getTitle1());
            holder.title1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.f);
            holder.title2.setText(folderFile.getTitle2());
        }

        @Override
        public int getItemCount() {
            return folderFiles.size();
        }

        @Override
        // нажатие на элемент списка файлов и каталогов
        public void onClick(View view) {
           ui_recyclerViewItemClick(view);
        }

        // получение ссылок на элементы UI
        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView imageView;
            final TextView title1, title2;

            ViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.image);
                title1 = view.findViewById(R.id.title1);
                title2 = view.findViewById(R.id.title2);
            }
        }
    }

    void ui_recyclerViewItemClick(View view){
        // нажатие на кнопку вернуться назад
        String title1Text = null;
        try {
            title1Text = ((TextView) view.findViewById(R.id.title1)).getText().toString();



            if (title1Text.equals(getString(R.string.to_parent_folder))) {
                String parentID = CurrentFolder.Companion.getCurrentFolder().getParentID();
                if (!parentID.equals("0")) http_getDocuments(relativeIDurl(parentID));

                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // нажатие на каталог
        try {
            String title2Text = ((TextView) view.findViewById(R.id.title2)).getText().toString();
            // id каталога беру из UI из элемента TextView title2
            String[] ts2 = title2Text.split("id:");
            String idClicked = ts2[1];
            http_getDocuments(relativeIDurl(idClicked));

            // обновляю текущий UI с названием каталога текущего
//            TextView textViewFolderName = view.findViewById(R.id.textViewFolderName);
//            textViewFolderName.setText(title1Text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
