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
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import ru.kazakova_net.smack.R
import ru.kazakova_net.smack.adapters.MessageAdapter
import ru.kazakova_net.smack.model.Channel
import ru.kazakova_net.smack.model.Message
import ru.kazakova_net.smack.services.AuthService
import ru.kazakova_net.smack.services.MessageService
import ru.kazakova_net.smack.services.UserDataService
import ru.kazakova_net.smack.utilities.BROADCAST_USER_DATA_CHANGED
import ru.kazakova_net.smack.utilities.SOCKET_URL

class MainActivity : AppCompatActivity() {

    private val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel: Channel? = null

    private val onNewChannel = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelId = args[2] as String

                if (channelId == selectedChannel?.id) {

                    val channelName = args[0] as String
                    val channelDesc = args[1] as String

                    val newChannel = Channel(channelName, channelDesc, channelId)
                    MessageService.channels.add(newChannel)

                    channelAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val msgBody = args[0] as String
                val channelId = args[2] as String
                val userName = args[3] as String
                val userAvatar = args[4] as String
                val userAvatarColor = args[5] as String
                val id = args[6] as String
                val timeStamp = args[7] as String

                val newMessage = Message(msgBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
                MessageService.messages.add(newMessage)
                messageAdapter.notifyDataSetChanged()
                messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
            }
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

            MessageService.getChannels { getChannelsSuccess ->
                if (getChannelsSuccess) {
                    if (MessageService.channels.count() > 0) {
                        selectedChannel = MessageService.channels[0]
                        channelAdapter.notifyDataSetChanged()
                        updateWithChannel()
                    }
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
        socket.on("messageCreated", onNewMessage)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        setupAdapter()

        channelList.setOnItemClickListener { _, _, i, l ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this) {}
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

    fun updateWithChannel() {
        mainChannelName.text = "#${selectedChannel?.name}"

        if (selectedChannel != null) {
            MessageService.getMessages(selectedChannel!!.id) { complete ->
                if (complete) {
                    messageAdapter.notifyDataSetChanged()

                    if (messageAdapter.itemCount > 0) {
                        messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    private fun setupAdapter() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channelList.adapter = channelAdapter

        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    fun loginBtnOnClicked(view: View) = when {
        App.prefs.isLoggedIn -> {
            // log out
            UserDataService.logout()

            channelAdapter.notifyDataSetChanged()
            messageAdapter.notifyDataSetChanged()

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
                .setPositiveButton("Add") { _, _ ->
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
        if (!App.prefs.isLoggedIn || !messageTextField.text.isNotEmpty() || selectedChannel == null) return

        val userId = UserDataService.id
        val channelId = selectedChannel!!.id

        socket.emit(
                "newMessage",
                messageTextField.text.toString(),
                userId,
                channelId,
                UserDataService.name,
                UserDataService.avatarName,
                UserDataService.avatarColor
        )

        messageTextField.text.clear()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
