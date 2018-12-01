package ru.kazakova_net.smack.services

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import org.json.JSONException
import ru.kazakova_net.smack.controller.App
import ru.kazakova_net.smack.model.Channel
import ru.kazakova_net.smack.model.Message
import ru.kazakova_net.smack.utilities.URL_GET_CHANNELS
import ru.kazakova_net.smack.utilities.URL_GET_MESSAGES

/**
 * Created by Kazakova_net on 29.11.2018.
 */
object MessageService {

    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels(complete: (Boolean) -> Unit) {
        val channelsRequest =
                object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null, Response.Listener { response ->

                    try {

                        for (x in 0 until response.length()) {
                            val channel = response.getJSONObject(x)
                            val name = channel.getString("name")
                            val channelDesc = channel.getString("description")
                            val channelId = channel.getString("_id")

                            val newChannel = Channel(name, channelDesc, channelId)
                            this.channels.add(newChannel)
                        }

                        complete(true)

                    } catch (e: JSONException) {
                        Log.d("JSON", "EXC: ${e.localizedMessage}")
                        complete(false)
                    }

                }, Response.ErrorListener { error ->
                    Log.e("ERROR", "Could not retrieve channels: $error")
                    complete(false)
                }) {
                    override fun getBodyContentType(): String {
                        return "application/json; charset=utf-8"
                    }

                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer ${App.prefs.authToken}"

                        return headers
                    }
                }

        App.prefs.requestQueue.add(channelsRequest)
    }

    fun getMessages(channelId: String, complete: (Boolean) -> Unit) {
        val url = "$URL_GET_MESSAGES$channelId"

        val messagesRequest =
                object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->

                    clearMessages()

                    try {

                        for (x in 0 until response.length()) {
                            val message = response.getJSONObject(x)
                            val msgBody = message.getString("messageBody")
                            val userName = message.getString("userName")
                            val channelId = message.getString("channelId")
                            val userAvatar = message.getString("userAvatar")
                            val userAvatarColor = message.getString("userAvatarColor")
                            val timeStamp = message.getString("timeStamp")
                            val id = message.getString("_id")

                            val newMessage = Message(msgBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
                            this.messages.add(newMessage)
                        }

                        complete(true)

                    } catch (e: JSONException) {
                        Log.d("JSON", "EXC: ${e.localizedMessage}")
                        complete(false)
                    }

                }, Response.ErrorListener { error ->
                    Log.e("ERROR", "Could not retrieve messages: $error")
                    complete(false)
                }) {
                    override fun getBodyContentType(): String {
                        return "application/json; charset=utf-8"
                    }

                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer ${App.prefs.authToken}"

                        return headers
                    }
                }

        App.prefs.requestQueue.add(messagesRequest)
    }

    fun clearMessages() {
        messages.clear()
    }

    fun clearChannels() {
        channels.clear()
    }
}