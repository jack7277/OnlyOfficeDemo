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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.onlyofficedemo.Utils.JSONhelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.example.onlyofficedemo.Network.HTTP_requestsKt.getDocuments;
import static com.example.onlyofficedemo.Network.HTTP_requestsKt.resetToken;
import static com.example.onlyofficedemo.Utils.UtilsKt.showToast;

public class activityFoldersFilesView extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener {
    ArrayList<FolderFileListElement> folderFiles = new ArrayList<>();
    private LayoutInflater inflater;
    SwipeRefreshLayout mSwipeRefreshLayout;

    // кнопка андроида назад
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    final static String MY_DOCUMENTS = "@my.json";
    final static String COMMON_DOCUMENTS = "@common.json";


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        TextView textViewFolderName = findViewById(R.id.textViewFolderName);

        switch (id) {
            case R.id.nav_my_documents:
                try {
                    getDocuments(MY_DOCUMENTS);
                    textViewFolderName.setText(getString(R.string.my_documents));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.nav_common_documents:
                try {
                    getDocuments(COMMON_DOCUMENTS);
                    textViewFolderName.setText(getString(R.string.common_documents));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.nav_logout:
                resetToken();
                startActivity(new Intent(getApplicationContext(), activity_login.class));
                finish();
                break;

            default:
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout() {
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.listfoldersfiles);

        //setInitialData();


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        try {
            //getSupportActionBar().hide();//Ocultar ActivityBar anterior
            setSupportActionBar(toolbar);
        } catch (Exception e) {
            e.printStackTrace();
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // SwipeRefreshLayout
        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            onRefresh();
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
            // Fetching data from server
            try {
                //getDocuments();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //updateRecyclerView(folderFiles);

        RecyclerView recyclerView = findViewById(R.id.list);
        // создаем адаптер
        DataAdapter adapter = new DataAdapter(this, folderFiles);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);

        try {
            getDocuments("@my.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void updateRecyclerView(ArrayList<FolderFileListElement> folderFiles) {
        RecyclerView recyclerView = findViewById(R.id.list);
        // создаем адаптер
        DataAdapter adapter = new DataAdapter(this, folderFiles);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEventJSONobject event) {
        mSwipeRefreshLayout.setRefreshing(false);

        // получаю от сервера список файлов и каталогов
        if (event == null) {
            showToast("network error, null json", getApplicationContext());
            return;
        }

        Log.d("TAG", "onMessageEvent: ");
        //showToast("bus", getApplicationContext());
        if (event.statusCode == 200) {
            updateList(event);
        } else {
            showToast("error code: " + event.statusCode, getApplicationContext());
        }

    }


    final static String TAG = "activityFoldersFiles";

    void updateList(MessageEventJSONobject event) {
        int statusCode = event.statusCode;
        List<? extends Header> headers = event.header;
        JSONObject jsonObject = event.response;

        ArrayList<FolderFileListElement> folderFiles = new ArrayList<>();

        // создаю список каталогов из ответа
        JSONArray folders = new JSONhelper(jsonObject).getFolders();
        Log.d(TAG, "updateList: ");

        // try to get parent ID
        String parentID = null;
        try {
            parentID = new JSONhelper(folders.getJSONObject(0)).getParentID();
            folderFiles.add(new FolderFileListElement("to parent folder", "", R.drawable.to_parent));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < folders.length(); i++) {
            try {
                JSONObject objFolder = folders.getJSONObject(i);
                JSONhelper obj = new JSONhelper(objFolder);

                String folderTitle = obj.getTitle();
                String filesCountInFolder = obj.getFilesCountInFolder();
                String foldersCountInFolder = obj.getFoldersCountInFolder();

                folderFiles.add(new FolderFileListElement(folderTitle, "Documents: " + filesCountInFolder + " | Subfolders: " + foldersCountInFolder, R.drawable.folder_image));
                Log.d(TAG, "updateList: ");
                //folderFiles.add()

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONArray files = new JSONhelper(jsonObject).getFiles();
        for (int i = 0; i < files.length(); i++) {
            try {
                JSONObject objFile = files.getJSONObject(i);
                JSONhelper obj = new JSONhelper(objFile);

                int icon = 0;
                String fname = obj.getTitle();
                if (fname.contains(".doc")) icon = R.drawable.docx_win_icon;
                if (fname.contains(".xls")) icon = R.drawable.xlsx_win_icon;
                if (fname.contains(".ppt")) icon = R.drawable.pptx_icon;

                folderFiles.add(new FolderFileListElement(fname, "", icon));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        updateRecyclerView(folderFiles);
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
        Log.d("TAG", "onRefresh: ");
        mSwipeRefreshLayout.setRefreshing(true);
        try {
            getDocuments("@my.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class FolderFileListElement {
        private String title1;
        private String title2;
        private int image;

        public FolderFileListElement(String title1, String title2, int image) {
            this.title1 = title1;
            this.title2 = title2;
            this.image = image;
        }

        public String getTitle1() {
            return this.title1;
        }

        public void setTitle1(String title1) {
            this.title1 = title1;
        }

        public String getTitle2() {
            return this.title2;
        }

        public void setTitle2(String title2) {
            this.title2 = title2;
        }

        public int getImage() {
            return this.image;
        }

        public void setImage(int image) {
            this.image = image;
        }
    }


    class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
        //private LayoutInflater inflater;
        private List<FolderFileListElement> folderFiles;

        DataAdapter(Context context, List<FolderFileListElement> folderFiles) {
            this.folderFiles = folderFiles;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.list_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
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

        public class ViewHolder extends RecyclerView.ViewHolder {
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
}
