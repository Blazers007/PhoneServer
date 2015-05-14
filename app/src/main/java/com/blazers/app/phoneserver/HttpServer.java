package com.blazers.app.phoneserver;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import com.blazers.app.phoneserver.Util.GetApks;
import com.blazers.app.phoneserver.Util.GetSDCardFiles;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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

        /* Tell me why remote service still cannot do NETWORK on main thread */
        /* http://stackoverflow.com/questions/14964819/android-what-is-main-thread-in-remote-service-network-operations-in-service */
        @Override
        public void run() {
            try {
                /* 获取输入输出流 */
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream);
                /* 判断输入内容 */
                String method, resource;
                try {
                    String line = reader.readLine();
//                    method = line.substring(0, 4).trim();
                    resource = line.substring(line.indexOf('/'), line.lastIndexOf('/') - 5);
                    if (resource.equals("/")) {
                        /* Index */
                        doGetRoot(reader, writer);
                    } else if (resource.startsWith("/files")) {
                        /* GetFiles */
                        String root;
                        if (resource.length() <= 6 ) {
                            doGetFiles("", reader, writer);
                        } else {
                            root = resource.substring(resource.indexOf("/files")+6);
                            File file = new File(root);
                            if (file.isDirectory()) {
                                doGetFiles(root, reader, writer);
                            } else {
                                doTransferFile(file, outputStream);
                            }
                        }
                    } else if (resource.startsWith("/apks")) {
                        /* 获取已安装的文件列表 */
                        doGetApks(outputStream);
                    } else if (resource.contains("/icon/")) {
                        /* 获取已安装的文件列表 */
                        String icon = resource.substring(resource.indexOf("/icon/")+6);
                        doGetApkIcon(icon, outputStream);
                    } else {
                        writer.println("HTTP/1.0 200 OK");//返回应答消息,并结束应答
                        writer.println("Content-Type:text/html;charset=" + "utf-8");
                        writer.println();
                        String body =
                                "<html><head><title>404 Error</title></head>"
                                        +"<body>"
                                        +"<h1>Error 404</h1>"
                                        +"</body>"
                                        +"</html>";
                        writer.print(body);
                    }
                    sendMessage(">---Handle Request from :" + socket.getInetAddress().toString() + " Over---<\n\n\n");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    inputStream.close();
                    outputStream.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void doGetRoot(BufferedReader reader, PrintWriter writer) throws IOException {
        sendMessage("Method : GET");
        /* Get Message And Judge*/
        String request = "";
        String line;
        while(( line = reader.readLine()) != null) {
            if(line.equalsIgnoreCase(""))
                break;
            request += line + "\n";
        }
        sendMessage("Received: \n" + request);
        /* Write Head */
        writer.println("HTTP/1.0 200 OK");//返回应答消息,并结束应答
        writer.println("Content-Type:text/html;charset=" + "utf-8");
        writer.println();// 根据 HTTP 协议, 空行将结束头信息
        /* Write Body */
        String body =
                "<html><head><title>test server</title></head>"
                        +"<body>"
                            +"<h1>Hello world</h1>"
                            +"<a href=\"/files\">Index SDCard Files</a>"
                            +"<a href=\"/apks\">Index Installed Applications</a>"
                        +"</body>"
                +"</html>";
        writer.println(body);
        writer.flush();
    }

    void doGetFiles(String fileRoot, BufferedReader reader, PrintWriter writer) throws IOException {
        sendMessage("Method : GET");
        /* Get Message And Judge*/
        String request = "";
        String line;
        while(( line = reader.readLine()) != null) {
            if(line.equalsIgnoreCase(""))
                break;
            request += line + "\n";
        }
        sendMessage("Received: \n" + request);
        /* Write Head */
        writer.println("HTTP/1.0 200 OK");//返回应答消息,并结束应答
        writer.println("Content-Type:text/html;charset=" + "utf-8");
        writer.println();// 根据 HTTP 协议, 空行将结束头信息
        /* Write Body */
        String files = "";
        for(File file : GetSDCardFiles.getFilesByPath(fileRoot)) {
            if (file.isDirectory()) {
                /* 文件夹 进一步进入 */
                files += "<a href=\"" + "/files/" + file.getAbsolutePath() +"\">" + file.getName() + "</a><p>目录</p></br>";
            } else {
                /* 文件 查看或者下载 */
                files += "<a href=\"" + "/files/" + file.getAbsolutePath() +"\">" + file.getName() + "</a><p>文件</p></br>";
            }
        }
        String body =
                "<html><head><title>Files Server</title></head>"
                        +"<body>"
                            +files
                        +"</body>"
                        +"</html>";
        writer.println(body);
        writer.flush();
    }

    void doTransferFile(File file, OutputStream outputStream) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 10];
            int length;
            while(true) {
                length = fileInputStream.read(buffer);
                if (length == -1)
                    break;
                outputStream.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void doGetApks(OutputStream outputStream) {
        PrintWriter writer = new PrintWriter(outputStream);
         /* Write Head */
        writer.println("HTTP/1.0 200 OK");//返回应答消息,并结束应答
        writer.println("Content-Type:text/html;charset=" + "utf-8");
        writer.println();// 根据 HTTP 协议, 空行将结束头信息
        /* Write Body */
        String apks = "";
        for(GetApks.APKInfo info : new GetApks().getInstalled(this)) {
            apks += "<tr>"
                    +   "<td>"
                    +        info.name
                    +   "</td>"
                    +   "<td>"
                    +        info.versionName
                    +   "</td>"
                    +   "<td>"
                    +        info.versionCode
                    +   "</td>"
                    +   "<td>"
                    +        "<img src=\"/icon/" +  info.name + "\" />"
                    +   "</td>"
                    +"</tr>";
        }
        String body =
                "<html><head><title>Files Server</title></head>"
                        +"<body>"
                        +   "<table>"
                        +       apks
                        +   "</table>"
                        +"</body>"
                        +"</html>";
        writer.println(body);
        writer.flush();
    }

    void doGetImageOnDisk(String path, OutputStream outputStream) {
        /* 可以用上面的方法代替 */
    }

    void doGetApkIcon(String describe, OutputStream outputStream) {
        /* 并非文件系统上的文件 必须通过其他的方式来传输 如 Drawable */
        try {
            outputStream.write(new GetApks().getInstalledApkIcon(this, describe));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void doGetImageSystem(String describe, OutputStream outputStream) {
        /* 并非文件系统上的文件 必须通过其他的方式来传输 如 Drawable */
    }

    /* 发送系统信息到控制台 */
    private void sendMessage(String msg) {
        Intent broadcast = new Intent("action.console.update");
        broadcast.putExtra("msg", msg);
        sendBroadcast(broadcast);
    }
}
