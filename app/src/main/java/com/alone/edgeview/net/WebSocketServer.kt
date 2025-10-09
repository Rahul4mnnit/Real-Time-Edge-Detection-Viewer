package com.alone.edgeview.net

import org.java_websocket.server.WebSocketServer
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.WebSocket
import java.net.InetSocketAddress

class MyWebSocketServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        println("✅ Client connected")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        println("📩 Message: $message")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        println("❌ Connection closed: $reason")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        ex?.printStackTrace()
    }

    override fun onStart() {
        println("🚀 WebSocket server started on port $port")
    }

    fun sendFrame(base64Frame: String) {
        // Broadcast to all connected clients
        for (client in connections) {
            client.send(base64Frame)
        }
    }

}
