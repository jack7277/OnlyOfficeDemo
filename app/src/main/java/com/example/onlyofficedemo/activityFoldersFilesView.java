package com.example.onlyofficedemo;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class activityFoldersFilesView extends AppCompatActivity {
    List<FolderFile> folderFiles = new ArrayList<>();
    private LayoutInflater inflater;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.listfoldersfiles);

        setInitialData();


        RecyclerView recyclerView = findViewById(R.id.list);
        // создаем адаптер
        DataAdapter adapter = new DataAdapter(this, folderFiles);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);
    }

        private void setInitialData () {
//            folderFiles.add(new FolderFile("Folder", "Folder", R.drawable.folder_image));
//            folderFiles.add(new FolderFile("Document", "Document", R.drawable.docx_win_icon));
//            folderFiles.add(new FolderFile("Table", "Table", R.drawable.xlsx_win_icon));
//            folderFiles.add(new FolderFile("Presentation", "Presentation", R.drawable.pptx_icon));
            //создаю запрос
            //getmydocuments

        }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }





    public class FolderFile {
        private String title1;
        private String title2;
        private int image;

        public FolderFile(String title1, String title2, int image){
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
        private List<FolderFile> folderFiles;

        DataAdapter(Context context, List<FolderFile> folderFiles) {
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
            FolderFile folderFile = this.folderFiles.get(position);

            holder.imageView.setImageResource(folderFile.getImage());
            holder.title1.setText(folderFile.getTitle1());
            holder.title1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.f);
            holder.title2.setText(folderFile.getTitle2());
        }

        @Override
        public int getItemCount() {
            return folderFiles.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView imageView;
            final TextView title1, title2;
            ViewHolder(View view){
                super(view);
                imageView = view.findViewById(R.id.image);
                title1 = view.findViewById(R.id.title1);
                title2 = view.findViewById(R.id.title2);
            }
        }
    }
}
