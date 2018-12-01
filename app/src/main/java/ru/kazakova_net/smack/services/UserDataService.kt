package ru.kazakova_net.smack.services

import android.graphics.Color
import ru.kazakova_net.smack.controller.App
import java.util.*

/**
 * Created by Kazakova_net on 27.11.2018.
 */
object UserDataService {

    var id = ""
    var avatarColor = ""
    var avatarName = ""
    var email = ""
    var name = ""

    fun logout() {
        id = ""
        avatarColor = ""
        avatarName = ""
        email = ""
        name = ""

        App.prefs.authToken = ""
        App.prefs.userEmail = ""
        App.prefs.isLoggedIn = false

        MessageService.clearChannels()
        MessageService.clearMessages()
    }

    fun returnAvatarColor(components: String): Int {
        // [0.8431372549019608, 0.796078431372549, 0.23921568627450981, 1]
        // 0.8431372549019608 0.796078431372549 0.23921568627450981 1

        val strippedColor = components
            .replace("[", "")
            .replace("]", "")
            .replace(",", "")

        var r = 0
        var g = 0
        var b = 0

        val scanner = Scanner(strippedColor).useLocale(Locale.ENGLISH)
        if (scanner.hasNext()) {
            r = (scanner.nextDouble() * 255).toInt()
            g = (scanner.nextDouble() * 255).toInt()
            b = (scanner.nextDouble() * 255).toInt()
        }

        return Color.rgb(r, g, b)
    }
}