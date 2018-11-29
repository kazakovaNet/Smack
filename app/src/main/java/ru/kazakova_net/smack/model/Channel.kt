package ru.kazakova_net.smack.model

/**
 * Created by Kazakova_net on 29.11.2018.
 */
class Channel(
    val name: String,
    val description: String,
    val id: String
) {
    override fun toString(): String {
        return "#$name"
    }
}