package com.mjh.codesandbox.sandbox.Security;


import java.security.Permission;

public class MySecurityManeger extends SecurityManager {
    //不做限制and限制所有权限
    @Override
    public void checkPermission(Permission perm) {
        //super.checkPermission(perm);
    }


    //限制读权限
    @Override
    public void checkRead(String file) {
        //super.checkRead(file);
    }
    //限制写权限
    @Override
    public void checkWrite(String file) {
        //super.checkWrite(file);
        //throw new SecurityException("写权限异常"+file.toString());
    }
    //限制网络连接权限
    @Override
    public void checkConnect(String host, int port) {
        //super.checkConnect(host, port);
    }
    //限制执行权限
    @Override
    public void checkExec(String cmd) {
        //super.checkExec(cmd);
    }
}
