package ru.kazakova_net.smack.controller

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_create_user.*
import ru.kazakova_net.smack.R
import ru.kazakova_net.smack.services.AuthService
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
    }

    fun generateUserAvatar(view: View) {
        val random = Random()
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        userAvatar = when (color) {
            0 -> "light$avatar"
            else -> "dark$avatar"
        }

        val recourseId = resources.getIdentifier(userAvatar, "drawable", packageName)
        createAvatarImageView.setImageResource(recourseId)
    }

    fun generateColorClicked(view: View) {
        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarImageView.setBackgroundColor(Color.rgb(r, g, b))

        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB, 1]"
    }

    fun createUserClicked(view: View) {
        val email = createEmailTxt.text.toString()
        val password = createPasswordTxt.text.toString()

        AuthService.registerUser(this, email, password) { registerSucces ->
            if (registerSucces) {
                AuthService.loginUser(this, email, password) { loginSucces ->
                    if (loginSucces) {
                        println(AuthService.authToken)
                        println(AuthService.userEmail)
                    }
                }
            }
        }
    }
}
