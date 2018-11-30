package ru.kazakova_net.smack.controller

import android.app.Application
import ru.kazakova_net.smack.utilities.SharedPrefs

/**
 * Created by Kazakova_net on 30.11.2018.
 */
class App : Application() {

    companion object {
        lateinit var prefs: SharedPrefs
    }

    override fun onCreate() {

        prefs = SharedPrefs(applicationContext)

        super.onCreate()
    }
}