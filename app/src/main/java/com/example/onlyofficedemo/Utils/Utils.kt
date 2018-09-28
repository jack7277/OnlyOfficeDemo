package com.example.onlyofficedemo.Utils

import android.content.Context
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast
import com.example.onlyofficedemo.activity_login.appContext
import com.timejet.bio.timejet.UTILS.CurFolderData
import com.timejet.bio.timejet.UTILS.LoggedInUser
import com.timejet.bio.timejet.UTILS.LoggedInUser.Companion.USER_EMAIL
import com.timejet.bio.timejet.UTILS.LoggedInUser.Companion.USER_PORTAL_NAME
import com.timejet.bio.timejet.UTILS.User


// показ тоста, передача контекста вроде как плохая идея
fun showToast(message: String, context: Context) {
    if (Looper.myLooper() == null) Looper.prepare()
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

// сохраняю полученного юзера в шаред
fun saveUserToSharedPrefs (context: Context, user: User?){
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()

    editor.putString(USER_PORTAL_NAME, user?.userPortal)
    editor.putString(USER_EMAIL, user?.userEmail)
    editor.putString(JSONhelper.JSON_USER_TOKEN, user?.userToken)
    editor.putString(JSONhelper.JSON_USER_TOKEN_EXPIRES, user?.tokenExpires)
    editor.putString(JSONhelper.JSON_USER_DISPLAY_NAME, user?.userName)
    editor.putString(JSONhelper.JSON_USER_AVATAR, user?.avatarPicUrl)

    editor.apply()
}

val CUR_FOLDER_NAME = "curFolderName"
val CUR_FOLDER_ID = "curFolderID"
val CUR_FOLDER_PARENT_ID = "curFolderParentID"

// сохраняю текущий каталог в шаред
fun saveCurFolderDataToSharedPrefs (context: Context, folderData: CurFolderData?){
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()

    editor.putString(CUR_FOLDER_NAME, folderData?.name)
    editor.putString(CUR_FOLDER_ID, folderData?.folderID)
    editor.putString(CUR_FOLDER_PARENT_ID, folderData?.parentID)

    editor.apply()
}


// функция сброса токена
// записывает пустые строки в: токен, время жизни токена, полное имя юзера, аватарка-урл
internal fun resetMyToken() {
    // время вышло, оставляем имя портала и емейл и стираю Токен и его Дату-Время
    saveUserToSharedPrefs(appContext,
            User(LoggedInUser.getUser()!!.userPortal, LoggedInUser.getUser()!!.userEmail, "", "", "", ""))
}

