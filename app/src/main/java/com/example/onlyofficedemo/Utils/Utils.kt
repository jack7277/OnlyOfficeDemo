package com.example.onlyofficedemo.Utils

import android.content.Context
import android.os.Looper
import android.widget.Toast


// показ тоста, копипаста из пред проекта, передача контекста вроде как плохая идея
fun showToast(message: String, context: Context) {
    if (Looper.myLooper() == null) Looper.prepare()
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}