package ru.kazakova_net.smack.services

import android.graphics.Color
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

        AuthService.authToken = ""
        AuthService.userEmail = ""
        AuthService.isLoggedIn = false
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