package com.timejet.bio.timejet.UTILS

import android.preference.PreferenceManager
import com.example.onlyofficedemo.Utils.CUR_FOLDER_ID
import com.example.onlyofficedemo.Utils.CUR_FOLDER_NAME
import com.example.onlyofficedemo.Utils.CUR_FOLDER_PARENT_ID
import com.example.onlyofficedemo.Utils.JSONhelper
import com.example.onlyofficedemo.activity_login.appContext

data class User (var userPortal:String?, var userEmail:String?, var userToken:String?, var tokenExpires:String?)

class LoggedInUser {
    init {
        // данные берем из шаред для инициализации
//        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)

//        userEmail = prefs.getString("userEmail", "")
//        userToken = prefs.getString(JSONhelper.JSON_USER_TOKEN, "")
//        userTokenExpires = prefs.getString(JSONhelper.USER_TOKEN_EXPIRES, "")
    }

    // если при обращении к какому-то элементу его нет, то разово инициализируем объект
    companion object {
        val USER_PORTAL_NAME = "userPortal"
        val USER_EMAIL = "userEmail"

        fun getUser(): User? {
            val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
            val user: User? = User(
                    prefs.getString(USER_PORTAL_NAME, ""),
                    prefs.getString(USER_EMAIL, ""),
                    prefs.getString(JSONhelper.JSON_USER_TOKEN, ""),
                    prefs.getString(JSONhelper.USER_TOKEN_EXPIRES, "")
            );
            return user;
        }

    }
}


data class CurFolderData (var name:String?, var folderID:String?, var parentID:String?)

class CurrentFolder {
    companion object {
        fun getCurrentFolder(): CurFolderData? {
            val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
            val folderData: CurFolderData? = CurFolderData(
                    prefs.getString(CUR_FOLDER_NAME, ""),
                    prefs.getString(CUR_FOLDER_ID, ""),
                    prefs.getString(CUR_FOLDER_PARENT_ID, "")
            )
            return folderData
        }

    }
}