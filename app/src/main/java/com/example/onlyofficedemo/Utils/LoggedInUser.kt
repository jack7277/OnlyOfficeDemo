package com.timejet.bio.timejet.UTILS

import android.preference.PreferenceManager
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
//        @Volatile
//        private var instance: LoggedInUser? = null
//        private var userToken: String? = null
//        private var userTokenExpires: String? = null

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

//        fun getUserEmail(): String? {
//            var userEmail: String? = null
//            val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
//            userEmail = prefs.getString("userEmail", "")
////            if (userEmail == null) LoggedInUser()
//            return userEmail
//        }

//        fun getToken(): String? {
//            if (userToken == null) LoggedInUser()
//            return userToken
//        }

//        fun getTokenExpires(): String? {
//            if (userTokenExpires == null) LoggedInUser()
//            return userTokenExpires
//        }


//        fun getInstance(): LoggedInUser? {
//            if (instance == null) {
//                synchronized(LoggedInUser::class.java) {
//                    if (instance == null) {
//                        instance = LoggedInUser()
//                    }
//                }
//            }
//            return instance
//        }
    }
}
