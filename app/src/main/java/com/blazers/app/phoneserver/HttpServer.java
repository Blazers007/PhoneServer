package com.blazers.app.phoneserver;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Created by liang on 2015/5/13.
 */
public class HttpServer extends Service {

//    private static final int PORT = 8080;

    private ServerSocket serverSocket;
    /* Tags */
    private boolean serverListening;
    /* Binder */
    private IMyAidlInterface.Stub binder = new IMyAidlInterface.Stub() {
        @Override
        public void startServer(int port) throws RemoteException {
            startHttpServer(port);
        }

        @Override
        public void stopServer() throws RemoteException {
            stopHttpServer();
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void startHttpServer(final int PORT) {
        /* Init */
        serverListening = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* Start */
                try {
                    serverSocket = new ServerSocket(PORT);
                    sendMessage("Server started at port : " + PORT);
                    while(serverListening) {
                        new Thread(new HandleRequest(serverSocket.accept())).start();
                    }
                    /* 服务器不在监听新连接 */
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void stopHttpServer() {
        serverListening = false;
        sendMessage("Server Closed");
    }

    class HandleRequest implements Runnable {

        private Socket socket;

        public HandleRequest(Socket socket) {
            this.socket = socket;
            sendMessage("Received Request from : " + socket.getInetAddress().toString());
        }

        @Override
        public void run() {
            try {
                /* 获取输入输出流 */
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                /* 判断输入内容 */
//                String request =
                /* 返回内容 */
                PrintWriter out = new PrintWriter(outputStream);
                out.println("HTTP/1.0 200 OK");//返回应答消息,并结束应答
                out.println("Content-Type:text/html;charset=" + "utf-8");
                out.println();// 根据 HTTP 协议, 空行将结束头信息

                out.println("<h1> Hello Http Server</h1>");
                out.println("你好, 这是一个 Java HTTP 服务器 demo 应用.<br>");

                /* 关闭输入输出流 */
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendMessage(String msg) {
        Intent broadcast = new Intent("action.console.update");
        broadcast.putExtra("msg", msg);
        sendBroadcast(broadcast);
    }
}
