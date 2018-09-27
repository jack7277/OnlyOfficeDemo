package com.example.onlyofficedemo.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast
import com.example.onlyofficedemo.Network.resetToken
import com.example.onlyofficedemo.activity_login.appContext
import com.timejet.bio.timejet.UTILS.CurFolderData
import com.timejet.bio.timejet.UTILS.LoggedInUser
import com.timejet.bio.timejet.UTILS.LoggedInUser.Companion.USER_EMAIL
import com.timejet.bio.timejet.UTILS.LoggedInUser.Companion.USER_PORTAL_NAME
import com.timejet.bio.timejet.UTILS.User
import org.joda.time.DateTime
import java.util.*


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

val CUR_FOLDER_NAME = "curFolderName"
val CUR_FOLDER_ID = "curFolderID"
val CUR_FOLDER_PARENT_ID = "curFolderParentID"

fun saveCurFolderDataToSharedPrefs (context: Context, folderData: CurFolderData?){
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()

    editor.putString(CUR_FOLDER_NAME, folderData?.name)
    editor.putString(CUR_FOLDER_ID, folderData?.folderID)
    editor.putString(CUR_FOLDER_PARENT_ID, folderData?.parentID)

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





// проверка времени токена на валидность в сравнении с текущей датой/время
fun isTokenValid(): Boolean {
    // 2019-09-24T01:36:13.8831347+03:00
    val userTokenExpires = Objects.requireNonNull<User>(LoggedInUser.getUser()).tokenExpires

    try { //parse by joda time
        // сохраненное дата-время токена
        val dtToken = DateTime.parse(userTokenExpires)
        val dtTokenMillis = dtToken.millis

        // текущее дата-время
        val dtNow = DateTime()
        val dateTimeNowMillis = dtNow.millis

        // проверяем не вышло ли время токена
        if (dtTokenMillis >= dateTimeNowMillis) {
            //showToast("Token Valid", appContext)
            return true
        } else {
            showToast("Token Invalid", appContext)
            // делаем релогин, оставляю емейл, стираю токен и время жизни
            resetToken()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return false
}