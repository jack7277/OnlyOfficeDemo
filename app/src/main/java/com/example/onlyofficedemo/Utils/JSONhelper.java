package com.example.onlyofficedemo.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONhelper {
    public final static String JSON_STATUS_CODE = "statusCode";
    public final static String JSON_USER_TOKEN = "token";
    public final static String JSON_RESPONSE = "response";
    public final static String JSON_USER_TOKEN_EXPIRES = "expires";
    public final static String JSON_MESSAGE = "message";
    public final static String JSON_ERROR = "error";
    public final static String JSON_FOLDERS = "folders";
    public final static String JSON_FILES = "files";
    public final static String JSON_TITLE_FOLDER = "title";
    public final static String JSON_FILES_COUNT_IN_FOLDER = "filesCount";
    public final static String JSON_FOLDERS_COUNT_IN_FOLDER = "foldersCount";
    public final static String JSON_PARENT_ID = "parentId";

    public final static String JSON_USER_DISPLAY_NAME = "displayName";
    public final static String JSON_USER_AVATAR = "avatar";
    public final static String JSON_USER_EMAIL = "email";

    public final static String JSON_CURRENT = "current";
    public final static String JSON_TITLE = "title";
    public final static String JSON_ID = "id";


    private JSONObject jsonObject;

    public JSONhelper(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    // Возвращает код если в json ответе есть "statusCode": 200/201...
    public int getStatusCode() {
        int statusCode = 0;
        try {
            statusCode = getJsonObject().getInt(JSON_STATUS_CODE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return statusCode;
    }

    // вынуть response-current
    public JSONObject getCurrent (JSONObject jsonObject){
        JSONObject jsonObjectCurrent = null;
        try {
            jsonObjectCurrent = getResponse().getJSONObject(JSON_CURRENT);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObjectCurrent;
    }


    // вынуть имя каталога
    public String getTitleFolderFile(){
        String folderTitle = null;
        try {
            folderTitle = getJsonObject().getString(JSON_TITLE_FOLDER);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return folderTitle;
    }

    public String getFilesCountInFolder (){
        String filesCountInFolder = null;
        try {
            filesCountInFolder = getJsonObject().getString(JSON_FILES_COUNT_IN_FOLDER);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return filesCountInFolder;
    }

    public String getFoldersCountInFolder (){
        String foldersCountInFolder = null;
        try {
            foldersCountInFolder = getJsonObject().getString(JSON_FOLDERS_COUNT_IN_FOLDER);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return foldersCountInFolder;
    }

    // возвращает json объект из response, если что возвращается json с ошибкой
    public JSONObject getResponse() {
        JSONObject jsonObject = null;
        try {
            jsonObject = getJsonObject().getJSONObject(JSON_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // {"status":1,"statusCode":500,"error":{"message":"User authentication failed","hresult":-2146233087,"data":{}}}
        // если тут пусто, то это error message
        if (jsonObject == null) {
            // пытаемся достать сообщение об ошибке
            try {
                jsonObject = getJsonObject().getJSONObject(JSON_ERROR);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    // из response ответа авторизации достаем токен юзера
    public String getToken() {
        String token = null;
        try {
            token = getResponse().getString(JSON_USER_TOKEN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return token;
    }


    // массив json объектов с каталогами
    public JSONArray getFolders(){
        JSONArray jsonObject = null;
        try {
            jsonObject = getResponse().getJSONArray(JSON_FOLDERS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    // массив json объектов с файлами
    public JSONArray getFiles(){
        JSONArray jsonObject = null;
        try {
            jsonObject = getResponse().getJSONArray(JSON_FILES);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


    // из response ответа авторизации вынимаю время жизни токена
    public String getTokenExpires() {
        String tokenExpires = null;
        try {
            tokenExpires = getResponse().getString(JSON_USER_TOKEN_EXPIRES);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tokenExpires;
    }

    // из Response вынуть message
    public String getMessage() {
        String message = null;
        try {
            message = getResponse().getString(JSON_MESSAGE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }
}
