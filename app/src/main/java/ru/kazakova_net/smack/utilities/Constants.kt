package ru.kazakova_net.smack.utilities

/**
 * Created by Kazakova_net on 26.11.2018.
 */

const val BASE_URL = "https://chattychatchch.herokuapp.com/v1/"
const val SOCKET_URL = "https://chattychatchch.herokuapp.com/"
const val URL_REGISTER = "${BASE_URL}account/register"
const val URL_LOGIN = "${BASE_URL}account/login"
const val URL_CREATE_USER = "${BASE_URL}user/add"
const val URL_GET_USER = "${BASE_URL}user/byEmail/"
const val URL_GET_CHANNELS="${BASE_URL}channel"
const val URL_GET_MESSAGES="${BASE_URL}message/byChannel/"

// Broadcast Constants
const val BROADCAST_USER_DATA_CHANGED = "BROADCAST_USER_DATA_CHANGED"