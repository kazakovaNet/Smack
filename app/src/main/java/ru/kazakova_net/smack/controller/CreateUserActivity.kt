package ru.kazakova_net.smack.controller

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_user.*
import ru.kazakova_net.smack.R
import ru.kazakova_net.smack.services.AuthService
import ru.kazakova_net.smack.utilities.BROADCAST_USER_DATA_CHANGED
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        createSpinner.visibility = View.INVISIBLE
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

        enableSpinner(true)

        val userName = createUserNameTxt.text.toString()
        val email = createEmailTxt.text.toString()
        val password = createPasswordTxt.text.toString()

        if (!userName.isNotEmpty() || !email.isNotEmpty() || !password.isNotEmpty()) {
            Toast.makeText(this, "Make sure user name, email, and password are filled in.", Toast.LENGTH_LONG).show()
            enableSpinner(false)

            return
        }

        AuthService.registerUser(this, email, password) { registerSuccess ->
            if (registerSuccess) {
                AuthService.loginUser(this, email, password) { loginSuccess ->
                    if (loginSuccess) {
                        AuthService.createUser(this, userName, email, userAvatar, avatarColor) { createSuccess ->
                            if (createSuccess) {

                                val userDataChanged = Intent(BROADCAST_USER_DATA_CHANGED)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChanged)

                                enableSpinner(false)
                                finish()
                            } else {
                                errorToast()
                            }
                        }
                    } else {
                        errorToast()
                    }
                }
            } else {
                errorToast()
            }
        }
    }

    fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    private fun enableSpinner(enable: Boolean) {
        when (enable) {
            true -> {
                createSpinner.visibility = View.VISIBLE
            }
            false -> {
                createSpinner.visibility = View.INVISIBLE
            }
        }
        createUserBtn.isEnabled = !enable
        createAvatarImageView.isEnabled = !enable
        backgroundColorBtn.isEnabled = !enable
    }
}
