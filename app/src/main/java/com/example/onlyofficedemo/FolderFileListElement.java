package com.example.onlyofficedemo;

// структура данных одного элемента списка
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


