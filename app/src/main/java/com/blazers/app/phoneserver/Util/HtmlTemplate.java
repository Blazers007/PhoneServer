package com.blazers.app.phoneserver.Util;

/**
 * Created by liang on 2015/5/15.
 */
public class HtmlTemplate {
    public static String getHead() {
        String head = ""
                +"<head>"
                +   "<meta charset=\"utf-8\" />\n"
                +   "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n"
                +   "<link rel=\"stylesheet\" href=\"/asset/css/kube.min.css\" />\n"
                +   "<link rel=\"stylesheet\" href=\"/asset/css/blazers.css\" />\n"
                +   "<link rel=\"stylesheet\" href=\"/asset/css/android.css\" />\n"
                +   "<script src=\"/asset/js/kube.min.js\"></script>\n"
                +   "<script src=\"/asset/js/jquery-2.1.4.min.js\"></script>\n"
                +"</head>";
        return head;
    }

    public static String makeNotification(String noti) {
        String notification = ""
                +"<div class=\"tools-message tools-message-blue\">\n"
                +   noti+"\n"
                +"</div>";
        return notification;
    }
}
