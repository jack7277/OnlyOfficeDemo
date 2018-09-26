package com.example.onlyofficedemo.Network

import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import com.example.onlyofficedemo.MessageEventJSONobject
import com.example.onlyofficedemo.R.string.*
import com.example.onlyofficedemo.Utils.*
import com.example.onlyofficedemo.activityFoldersFilesView
import com.example.onlyofficedemo.activity_login.appContext
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.PersistentCookieStore
import com.timejet.bio.timejet.UTILS.LoggedInUser
import com.timejet.bio.timejet.UTILS.User
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.ContentType
import cz.msebera.android.httpclient.entity.StringEntity
import cz.msebera.android.httpclient.message.BasicHeader
import cz.msebera.android.httpclient.protocol.HTTP
import org.json.JSONArray
import org.json.JSONObject
import com.loopj.android.http.RequestParams
import org.greenrobot.eventbus.EventBus


internal fun resetToken() {
    // время вышло, оставляем имя портала и емейл и стираю Токен и его Дату-Время
    saveUserToSharedPrefs(appContext,
            User(LoggedInUser.getUser()!!.userPortal, LoggedInUser.getUser()!!.userEmail, "", ""))
}

@Throws(Exception::class)
fun loginOnlyOffice(loginEmailText: String?, passwordText: String?, portalName: String?) {
    val url: String?

    url = "https://" +
            portalName +
            "/api/2.0/authentication.json"

    val jsonParams = JSONObject()
    val entity: StringEntity?

    jsonParams.put("userName", loginEmailText)
    jsonParams.put("password", passwordText)
    entity = StringEntity(jsonParams.toString())
    entity.contentType = BasicHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())

    val client = AsyncHttpClient()

    client.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
    client.addHeader("Accept", ContentType.APPLICATION_JSON.toString())

    val myCookieStore = PersistentCookieStore(appContext)
    client.setCookieStore(myCookieStore)

    client.post(appContext, url, entity, ContentType.APPLICATION_JSON.toString(), object : JsonHttpResponseHandler() {
        override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
            super.onSuccess(statusCode, headers, response)

            // вынимаем из ответа токен и время жизни токена
            val jsonHelper = JSONhelper(response)
            val token = jsonHelper.token
            val tokenExpires = jsonHelper.tokenExpires

            // сохраняю емейл, токен и время его жизни
            val user: User? = User(portalName, loginEmailText, token, tokenExpires)
            saveUserToSharedPrefs(appContext, user)

            //saveToSharedPrefs(appContext, loginEmailText, token, tokenExpires)

            showToast(appContext.getResources().getString(login_success), appContext)
            // так не очень

            // посылаю событие в activity_login о юзере
            EventBus.getDefault().post(MessageEventJSONobject(666, null, null));
        }


        override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONObject?) {
            super.onFailure(statusCode, headers, throwable, errorResponse)

            var errorMessage: String? = "unknown error"
            if (errorResponse != null) {
                val jsonHelper = JSONhelper(errorResponse)
                errorMessage = jsonHelper.message
            }
            showToast(appContext.getResources().getString(error_code) + " " + statusCode + "\n" + errorMessage, appContext)
        }

        override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
            super.onFailure(statusCode, headers, responseString, throwable)
            var message = ""
            // вот эту часть можно сжать до введения имя портала, portalName + ".onlyOffice.com, .eu, .sg
            if (statusCode == 404) message = "\n" + appContext.getResources().getString(portal_not_found) + "\nTry: .com, .eu, sg"
            showToast(appContext.getResources().getString(error_code) + " " + statusCode + message, appContext)
        }
    })
}

// Returns the detailed list of files and folders located in the current user 'My Documents' section
// return json object только возврат асинхронный, ловим EventBus
@Throws(Exception::class)
fun getDocuments(relativeURL: String?) {
    // нечего дальше делать если null
    if (relativeURL == null) return;

    if (!isTokenValid()) {
        showToast("Token Invalid, Relogin", appContext)
        return;
    }

    val portalName = LoggedInUser.getUser()?.userPortal
    val autorization = LoggedInUser.getUser()?.userToken

    val url: String? = "https://" +
            portalName +
            "/api/2.0/files/" +//@my.json"
            relativeURL;

    val client = AsyncHttpClient()

    client.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
    client.addHeader("Accept", ContentType.APPLICATION_JSON.toString())
    client.addHeader("Authorization", autorization)

    val myCookieStore = PersistentCookieStore(appContext)
    client.setCookieStore(myCookieStore)

    client.get(appContext, url, object : JsonHttpResponseHandler() {
        override fun onSuccess(statusCode: Int, headers: Array<out Header>, response: JSONObject?) {
            super.onSuccess(statusCode, headers, response)
            // event bus из ответа о списке файлов каталога
            EventBus.getDefault().post(MessageEventJSONobject(statusCode, headers.toMutableList(), response));
        }

        override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
            super.onFailure(statusCode, headers, throwable, errorResponse)
            showToast("HTTP Error: " + statusCode, appContext)
        }

        override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONArray?) {
            super.onFailure(statusCode, headers, throwable, errorResponse)
            showToast("HTTP Error: " + statusCode, appContext)
        }

        override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
            super.onFailure(statusCode, headers, responseString, throwable)
            showToast("HTTP Error: " + statusCode, appContext)
        }

    })
}


