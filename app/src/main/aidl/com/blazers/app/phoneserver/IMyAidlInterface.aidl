// IMyAidlInterface.aidl
package com.blazers.app.phoneserver;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    void startServer(int port);
    void stopServer();
}
