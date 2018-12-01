package ru.kazakova_net.smack.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.kazakova_net.smack.R
import ru.kazakova_net.smack.model.Message
import ru.kazakova_net.smack.services.UserDataService

/**
 * Created by Kazakova_net on 01.12.2018.
 */
class MessageAdapter(val context: Context, val messages: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bindMessage(context, messages[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userImage = itemView.findViewById<ImageView>(R.id.messageUserImage)
        private val userName = itemView.findViewById<TextView>(R.id.messageUserName)
        private val timeStamp = itemView.findViewById<TextView>(R.id.messageTimeStamp)
        private val msgBody = itemView.findViewById<TextView>(R.id.messageBody)

        fun bindMessage(context: Context, message: Message) {
            val recourseId = context.resources.getIdentifier(
                    message.userAvatar,
                    "drawable",
                    context.packageName
            )
            userImage.setImageResource(recourseId)
            userImage.setBackgroundColor(UserDataService.returnAvatarColor(message.userAvatarColor))

            userName.text = message.userName
            timeStamp.text = message.timestamp
            msgBody.text = message.message
        }
    }
}