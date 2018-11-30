package ru.kazakova_net.smack.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import ru.kazakova_net.smack.R
import ru.kazakova_net.smack.model.Channel
import ru.kazakova_net.smack.services.AuthService
import ru.kazakova_net.smack.services.MessageService
import ru.kazakova_net.smack.services.UserDataService
import ru.kazakova_net.smack.utilities.BROADCAST_USER_DATA_CHANGED
import ru.kazakova_net.smack.utilities.SOCKET_URL

class MainActivity : AppCompatActivity() {

    private val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>

    private val onNewChannel = Emitter.Listener { args ->
        runOnUiThread {
            val channelName = args[0] as String
            val channelDesc = args[1] as String
            val channelId = args[2] as String

            val newChannel = Channel(channelName, channelDesc, channelId)
            MessageService.channels.add(newChannel)

            channelAdapter.notifyDataSetChanged()
        }
    }

    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (!App.prefs.isLoggedIn) {
                return
            }

            userNameNavHeader.text = UserDataService.name
            userEmailNavHeader.text = UserDataService.email
            val resourceId = resources.getIdentifier(
                UserDataService.avatarName, "drawable",
                packageName
            )
            userImageNavHeader.setImageResource(resourceId)
            userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
            loginBtnNavHeader.text = "Logout"

            MessageService.getChannels(context){ getChannelsSuccess->
                if (getChannelsSuccess){
                    channelAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        socket.connect()

        socket.on("channelCreated", onNewChannel)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        setupAdapter()

        if (App.prefs.isLoggedIn){
            AuthService.findUserByEmail(this){}
        }
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            userDataChangeReceiver, IntentFilter(
                BROADCAST_USER_DATA_CHANGED
            )
        )

        super.onResume()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)

        socket.disconnect()

        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun setupAdapter() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter
    }

    fun loginBtnOnClicked(view: View) = when {
        App.prefs.isLoggedIn -> {
            // log out
            UserDataService.logout()

            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginBtnNavHeader.text = "Login"
        }
        else -> {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    fun onChannelBtnClicked(view: View) {
        if (!App.prefs.isLoggedIn) {
            return
        }

        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

        builder.setView(dialogView)
            .setPositiveButton("Add") { dialogInterface, i ->
                val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescTxt)
                val channelName = nameTextField.text.toString()
                val channelDesc = descTextField.text.toString()

                socket.emit("newChannel", channelName, channelDesc)
            }.setNegativeButton("Cancel") { dialogInterface, i ->

            }
            .show()
    }

    fun sendMsgBtnClicked(view: View) {
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
