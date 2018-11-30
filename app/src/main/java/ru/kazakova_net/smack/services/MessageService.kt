package ru.kazakova_net.smack.services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import ru.kazakova_net.smack.controller.App
import ru.kazakova_net.smack.model.Channel
import ru.kazakova_net.smack.utilities.URL_GET_CHANNELS

/**
 * Created by Kazakova_net on 29.11.2018.
 */
object MessageService {

    val channels = ArrayList<Channel>()

    fun getChannels(context: Context, complete: (Boolean) -> Unit) {
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
}