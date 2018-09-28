package com.example.onlyofficedemo.Network

import com.example.onlyofficedemo.MessageEventJSONobject
import com.example.onlyofficedemo.R.string.*
import com.example.onlyofficedemo.Utils.*
import com.example.onlyofficedemo.Utils.JSONhelper.*
import com.example.onlyofficedemo.activity_login.USER_SUCCESS_LOGIN
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
import com.timejet.bio.timejet.UTILS.CurFolderData

import org.greenrobot.eventbus.EventBus
import java.net.URLEncoder


// функция сброса токена
// записывает пустые строки в: токен, время жизни токена, полное имя юзера, аватарка-урл
internal fun resetMyToken() {
    // время вышло, оставляем имя портала и емейл и стираю Токен и его Дату-Время
    saveUserToSharedPrefs(appContext,
            User(LoggedInUser.getUser()!!.userPortal, LoggedInUser.getUser()!!.userEmail, "", "","",""))
}

// отдельная функция на авторизацию по емейлу, паролю и полному имени портала
// полный url, "имя портала короткое + onlyoffice.eu/.com/.sg"
// в моем случае полный url портала  "biojack.onlyoffice.eu"
// емейл на входе экранирован библиотекой фильтра емейлов javax.mail validate
// пароль передается без экранирования
@Throws(Exception::class)
fun loginOnlyOffice(loginEmailText: String?, passwordText: String?, portalName: String?) {
    val url: String?

    // собираю полный урл со ссылкой на авторизацию
    url = "https://" +
            portalName +
            "/api/2.0/authentication.json"

    val jsonParams = JSONObject()
    val entity: StringEntity?

    // в post http запросе формирую поля для передачи серверу, в формате json
    jsonParams.put("userName", loginEmailText)
    jsonParams.put("password", passwordText)
    entity = StringEntity(jsonParams.toString())
    entity.contentType = BasicHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())

    val client = AsyncHttpClient()

    // в апи указано посылать в запросе уточнение, что это json
    client.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
    client.addHeader("Accept", ContentType.APPLICATION_JSON.toString())

    // автоматическое сохранение куки
    val myCookieStore = PersistentCookieStore(appContext)
    client.setCookieStore(myCookieStore)

    // post запрос серверу onlyoffice
    client.post(appContext, url, entity, ContentType.APPLICATION_JSON.toString(), object : JsonHttpResponseHandler() {
        // если запрос прошел успешно
        override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
            super.onSuccess(statusCode, headers, response)

            // вынимаем из ответа токен пользователя и время жизни токена
            val jsonHelper = JSONhelper(response)
            val token = jsonHelper.token
            val tokenExpires = jsonHelper.tokenExpires

            // сохраняю емейл, токен и время его жизни
            val user: User? = User(portalName, loginEmailText, token, tokenExpires, "", "")
            saveUserToSharedPrefs(appContext, user)

            showToast(appContext.getResources().getString(login_success), appContext)

            // посылаю событие в activity_login о юзере, статус код USER_SUCCESS_LOGIN означает, что юзер залогинился успешно
            EventBus.getDefault().post(MessageEventJSONobject(USER_SUCCESS_LOGIN, null, null));
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
// получение моих документов, общих документов и каталога по его id
@Throws(Exception::class)
fun getDocuments(relativeURL: String?) {
    // нечего дальше делать если null
    if (relativeURL == null) return;

    // проверяем токен на достоверность
    if (!isTokenValid()) {
        showToast("Token Invalid, Relogin", appContext)
        return;
    }

    // из шаред беру урл юзера и его токен
    val portalName = LoggedInUser.getUser()?.userPortal
    val autorization = LoggedInUser.getUser()?.userToken

    // экранируем запрос по полному урлу к серверу
    val url: String? = URLEncoder.encode("https://$portalName$relativeURL", "utf-8");

    val client = AsyncHttpClient()

    // заголовок запроса, Authorization = токен
    client.addHeader("Authorization", autorization)

    // сообщаю серверу что работаем с json
    client.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
    client.addHeader("Accept", ContentType.APPLICATION_JSON.toString())

    // автокуки
    val myCookieStore = PersistentCookieStore(appContext)
    client.setCookieStore(myCookieStore)

    client.get(appContext, url, object : JsonHttpResponseHandler() {
        override fun onSuccess(statusCode: Int, headers: Array<out Header>, response: JSONObject?) {
            super.onSuccess(statusCode, headers, response)
            // event bus из ответа о списке файлов каталога
            EventBus.getDefault().post(MessageEventJSONobject(statusCode, headers.toMutableList(), response));

            // парсинг в ответе ужасная идея
            // это делать для ответа по запросу открытия каталогов @my, @common и каталог по идентификатору id
                val jsonHelper = JSONhelper(response)
                val title :String? =  jsonHelper.getCurrent(response)?.getString(JSON_TITLE)
                val id:String? = jsonHelper.getCurrent(response)?.getString(JSON_ID)
                val parentID:String? = jsonHelper.getCurrent(response)?.getString(JSON_PARENT_ID)

                // в ответе о @self id, title и parentsID будут null
            if (title!=null && id!=null && parentID!=null) {
                val curFolderData: CurFolderData = CurFolderData(title, id, parentID)
                saveCurFolderDataToSharedPrefs(appContext, curFolderData)
                return
            }
            // тут ловим ответ юзера, нет разделения трафика
            else{
                val uName:String? = jsonHelper.response?.getString(JSON_USER_DISPLAY_NAME)
                val uEmail:String? = jsonHelper.response?.getString(JSON_USER_EMAIL)
                val uAvatar:String? = jsonHelper.response?.getString(JSON_USER_AVATAR)

                if (uName!=null && uEmail!=null && uAvatar!=null){
                    val user: User? = User(LoggedInUser.getUser()?.userPortal,
                            uEmail,
                            LoggedInUser.getUser()?.userToken,
                            LoggedInUser.getUser()?.tokenExpires,
                            uName,
                            uAvatar)

                    saveUserToSharedPrefs(appContext, user)
                    return
                }
            }
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


