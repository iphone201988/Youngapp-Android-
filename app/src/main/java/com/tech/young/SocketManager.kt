package com.tech.young

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.net.URISyntaxException

object SocketManager {
    private const val SERVER_URL = "https://3.148.147.103:8000"
    var mSocket: Socket? = null



    @Synchronized
    fun setSocket(token: String) {
        try {
            if (token.isBlank()) {
                Log.e("SocketHandler", "Token is empty! Cannot initialize socket.")
                return
            }

            val options = IO.Options().apply {
                extraHeaders = mapOf("token" to listOf(token.trim()))  // Ensure correct header format
                reconnection = true  // Enables automatic reconnection
                reconnectionAttempts = 5  // Retry 5 times before failing
                reconnectionDelay = 2000  // Wait 2 seconds before retrying
            }

            val socket = IO.socket(SERVER_URL, options)  // Use a local variable
            mSocket = socket  // Assign to the global property

            Log.i("SocketHandler", "Socket initialized with token: $token")

            // Ensure headers are attached dynamically during handshake
            socket.io()?.on(Manager.EVENT_TRANSPORT) { args ->
                try {
                    val transport = args[0] as? Transport ?: return@on
                    transport.on(Transport.EVENT_REQUEST_HEADERS) { headerArgs ->
                        try {
                            val headers = headerArgs[0] as? MutableMap<String, List<String>> ?: return@on
                            headers["token"] = listOf(token.trim())  // Attach token dynamically
                            Log.i("SocketHandler", "Added token dynamically: $headers")
                        } catch (e: Exception) {
                            Log.e("SocketHandler", "Error modifying request headers: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SocketHandler", "Error in transport setup: ${e.message}")
                }
            }

        } catch (e: URISyntaxException) {
            Log.e("SocketHandler", "Socket initialization error: ${e.message}")
        } catch (e: Exception) {
            Log.e("SocketHandler", "Unexpected error: ${e.message}")
        }
    }

    fun emitOffer(offer: SessionDescription, roomId: String) {
        val message = JSONObject().apply {
            put("sdp", offer.description)
            put("partnerSocketId", roomId)
        }
        mSocket?.emit("offer", message)
    }


    fun emitSendMessage(
        message: String?,
        id: String?,
        name: String?,
        type: String?,
        roomId: String?,
    ) {
        val message = JSONObject().apply {
            put("message", message)
            put("id", id)
            put("name", name)
            put("type", type)
            put("roomId", roomId)
        }
        mSocket?.emit("sendMessage2", message)
    }

    fun emiLeaveMessage(id: String?) {

        val message = JSONObject().apply {
            put("userId", id)
        }
        mSocket?.emit("Leave", message)

    }


    fun emitAnswer(answer: SessionDescription, roomId: String) {
        val message = JSONObject().apply {
            put("sdp", answer.description)
            put("partnerSocketId", roomId)
        }
        mSocket?.emit("answer", message)
    }

    fun emitLogin(userId: String, lat: String, long: String) {
        val message = JSONObject().apply {
            put("userId", userId)
            put("lat", lat.toFloat())
            put("lang", long.toFloat())
        }
        mSocket?.emit("login", message)
    }

    fun emitIceCandidate(candidate: IceCandidate, roomId: String) {
        val jsonCandidate = JSONObject().apply {
            put("sdpMid", candidate.sdpMid)
            put("sdpMLineIndex", candidate.sdpMLineIndex)
            put("candidate", candidate.sdp)
            put("partnerSocketId", roomId)
        }
        mSocket?.emit("ice-candidate", jsonCandidate)
    }

    @Synchronized
    fun getSocket(): Socket? {
        if (mSocket?.connected() == true) {
            Log.d("_root_ide_package_.com.tech.young.SocketManager.mSocket", "getSocket: Already Connected")
        } else if (mSocket?.connected() == false) {
            Log.d("_root_ide_package_.com.tech.young.SocketManager.mSocket", "getSocket: Socket is disconnected, attempting to reconnect.")
            mSocket?.connect()
        } else {
            Log.d("_root_ide_package_.com.tech.young.SocketManager.mSocket", "getSocket: Socket is neither connected nor disconnected, attempting to connect.")
            mSocket?.connect()
        }

        return mSocket
    }


    @Synchronized
    fun socketIsConnected(): Boolean {
        val isConnected = mSocket?.connected() == true
        Log.d("SocketHandler", "Socket connection status: $isConnected")
        mSocket?.emit("Connected", if (isConnected) "Connected" else "Disconnected")
        return isConnected
    }

    @Synchronized
    fun establishConnection() {
        if (mSocket == null) {
            Log.e("SocketHandler", "Socket is not initialized. Call setSocket() first.")
            return
        }

        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            Log.d("SocketHandler", "Attempting to establish _root_ide_package_.com.tech.young.SocketManager.mSocket connection...")

            // Listen for successful connection
            mSocket!!.on(Socket.EVENT_CONNECT) {
                Log.i("SocketHandler", "Socket connected successfully.")
            }

            // Listen for disconnection
            mSocket!!.on(Socket.EVENT_DISCONNECT) {
                Log.e("SocketHandler", "Socket disconnected.")
            }

            // Listen for connection errors
            mSocket!!.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("SocketHandler", "Socket connection error: ${args.joinToString()}")
            }




        } else {
            Log.d("SocketHandler", "Socket is already connected.")
        }
    }


    @Synchronized
    fun closeConnection() {
        if (mSocket != null && mSocket!!.connected()) {
            mSocket!!.disconnect()
            Log.d("SocketHandler", "Socket disconnected successfully.")
        } else {
            Log.d("SocketHandler", "Socket is already disconnected or not initialized.")
        }
    }
}

