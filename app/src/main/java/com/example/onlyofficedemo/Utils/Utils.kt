package com.example.onlyofficedemo.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast
import com.example.onlyofficedemo.activity_login.appContext
import com.timejet.bio.timejet.UTILS.LoggedInUser.Companion.USER_EMAIL
import com.timejet.bio.timejet.UTILS.LoggedInUser.Companion.USER_PORTAL_NAME
import com.timejet.bio.timejet.UTILS.User


// показ тоста, передача контекста вроде как плохая идея
fun showToast(message: String, context: Context) {
    if (Looper.myLooper() == null) Looper.prepare()
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun saveUserToSharedPrefs (context: Context, user: User?){
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()

    editor.putString(USER_PORTAL_NAME, user?.userPortal)
    editor.putString(USER_EMAIL, user?.userEmail)
    editor.putString(JSONhelper.JSON_USER_TOKEN, user?.userToken)
    editor.putString(JSONhelper.USER_TOKEN_EXPIRES, user?.tokenExpires)

    editor.apply()
}


// проверяем есть ли интернеты эти ваши
fun isOnline(): Boolean {
    val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val netInfo: NetworkInfo?

    if (cm != null) {
        netInfo = cm.activeNetworkInfo
        if (netInfo != null && netInfo.isConnected)
            return true
    }
    return false
}