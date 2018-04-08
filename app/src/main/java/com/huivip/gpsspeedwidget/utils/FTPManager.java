package com.huivip.gpsspeedwidget.utils;

import android.content.Context;
import android.text.TextUtils;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;


/**
 * hongzhen yu create at 2017/7/28
 */

public class FTPManager {
    private static FTPManager instance;
    private Context context;
    private FTPManager() {

    }

    /**
     * @return
     * @throws
     * @Title: getInstance
     * @Description: 单例方式提供对象
     */
    public static FTPManager getInstance(Context context) {
        if (instance == null) {
            synchronized (FTPManager.class) {
                if (instance == null) {
                    instance = new FTPManager();
                    instance.setContext(context);
                }
            }
        }

        return instance;
    }
    public void setContext(Context context){
        this.context=context;
    }
   /* public void ftp4jUpload(final String path, final IResultListener listener) {
        new Thread() {
            public void run() {
                try {
                    String targetName = ftp4jUpload(path);
                    if (listener != null) {
                        listener.onSuccess(targetName);
                    }
                } catch (IllegalStateException | IOException
                        | FTPIllegalReplyException | FTPException
                        | FTPDataTransferException | FTPAbortedException e) {
                    e.printStackTrace();
                    Log.d("lixm", "ftp4jUpload error : ", e);
                    if (listener != null) {
                        listener.onFilure(e.getMessage());
                    }
                }

            }
        }.start();
    }
*/
    /**
     * FTP协议文件上传
     *
     * @param path

     */
    public String ftp4jUpload(String path) throws IllegalStateException, IOException{
        // 创建客户端
        final FTPClient client = new FTPClient();
        // 不指定端口，则使用默认端口21
        String ip ="";// PreferenceUtil.getNetworkIP(context);
        String rightIP = "192.168.150.1";
        if (!TextUtils.isEmpty(ip)) {
            String[] ipArr = ip.split("\\.");
            if (ipArr != null && ipArr.length == 4) {
                rightIP = ip;
            }
        }
        int rightPort = 21;
        String port = "21";// PreferenceUtil.getNetworkPort(context);
        if (!TextUtils.isEmpty(port)) {
            rightPort = Integer.valueOf(rightPort);
        }
        client.connect(rightIP, rightPort);
        // 用户登录
        String user ="";// PreferenceUtil.getNetworkUser(context);
        String rightUser = "test";
        if (!TextUtils.isEmpty(user)) {
            rightUser = user;
        }
        String pwd ="";// PreferenceUtil.getNetworkPwd(context);
        String rightPwd = "test";
        if (!TextUtils.isEmpty(pwd)) {
            rightPwd = pwd;
        }
        client.login(rightUser, rightPwd);
        String rightFilePath = "";
        String filePath ="";// PreferenceUtil.getNetworkFtpPath(context);
        if (!TextUtils.isEmpty(filePath)) {
            rightFilePath = "/" + filePath + "/";
           // client.changeDirectory(rightFilePath+PreferenceUtil.getUserCode(MyApplication.getMyApplication())+"/");
        }
        File file = new File(path);
        InputStream in=new FileInputStream(file);
        boolean result=client.storeFile("GPSHistory.db",in);
        //client.upload(file);
       // client.rename(srcName, targetName);
        return "";
    }

   /* //从ftp服务器下载文件
    public void ftpDownLoad( final FTPDataTransferListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 创建客户端
                final FTPClient client = new FTPClient();
                //不指定IP的话，默认IP
                String ip = PreferenceUtil.getNetworkIP(context);
                String rightIP = "192.168.128.52";
                if (!TextUtils.isEmpty(ip)) {
                    String[] ipArr = ip.split("\\.");
                    if (ipArr != null && ipArr.length == 4) {
                        rightIP = ip;
                    }
                }
                // 不指定端口，则使用默认端口21
                int rightPort = 21;
                String port = PreferenceUtil.getNetworkPort(context);
                if (!TextUtils.isEmpty(port)) {
                    rightPort = Integer.valueOf(rightPort);
                }
                try {
                    client.connect(rightIP, rightPort);
                    // 用户登录
                    String user = PreferenceUtil.getNetworkUser(context);
                    String rightUser = "test";
                    if (!TextUtils.isEmpty(user)) {
                        rightUser = user;
                    }
                    String pwd = PreferenceUtil.getNetworkPwd(context);
                    String rightPwd = "test";
                    if (!TextUtils.isEmpty(pwd)) {
                        rightPwd = pwd;
                    }
                    client.login(rightUser, rightPwd);
                    String pathFTP = "/" + "ZIP"+"/";
                    String filePath = PreferenceUtil.getNetworkFtpPath(context);
                    if (!TextUtils.isEmpty(filePath)) {
                        pathFTP = filePath + "/";
                    }
                    client.changeDirectory(pathFTP+PreferenceUtil.getUserCode(MyApplication.getMyApplication())+"/result");
                    String[] strings = client.listNames();
                    for (int i = 0; i < strings.length; i++) {
                        Log.i("listfile", strings[i]);
                        String name = strings[i];
                        Log.i("file", name);
                        File file = new File(Constant.RESUIT_PATH + name);
                        // 输出流
                        OutputStream outputStream = new FileOutputStream(file);
                        client.download(name, outputStream, 0, listener);
                        outputStream.close();
                        client.logout();
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }
*/
    /**
     * 在ftp服务器上创建指定目录
     * @param
     */
   /* public void ftpMakeUserDir() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 创建客户端
                final FTPClient client = new FTPClient();
                // 不指定端口，则使用默认端口21
                String rightIP = "192.168.128.52";
                String ip = PreferenceUtil.getNetworkIP(context);
                if (!TextUtils.isEmpty(ip)) {
                    String[] ipArr = ip.split("\\.");
                    if (ipArr != null && ipArr.length == 4) {
                        rightIP = ip;
                    }
                }
                int rightPort = 21;
                String port = PreferenceUtil.getNetworkPort(context);
                if (!TextUtils.isEmpty(port)) {
                    rightPort = Integer.valueOf(rightPort);
                }
                try {
                    client.connect(rightIP, rightPort);//连接ftp服务器
                    //配置用户名
                    String user = PreferenceUtil.getNetworkUser(context);
                    String rightUser = "test";
                    if (!TextUtils.isEmpty(user)) {
                        rightUser = user;
                    }
                    //配置密码
                    String pwd = PreferenceUtil.getNetworkPwd(context);
                    String rightPwd = "test";
                    if (!TextUtils.isEmpty(pwd)) {
                        rightPwd = pwd;
                    }
                    client.login(rightUser, rightPwd);//登录到ftp
                    //配置ftp服务器根目录
                    String pathFTP = "/" + "ZIP"+"/";
                    String filePath = PreferenceUtil.getNetworkFtpPath(context);
                    if (!TextUtils.isEmpty(filePath)) {
                        pathFTP = filePath + "/";
                    }
                    client.changeDirectory(pathFTP);
                    //创建用户目录
                    String userCode = PreferenceUtil.getUserCode(MyApplication.getMyApplication());
                    if (!isDirExist(client, userCode)) {
                        client.createDirectory(userCode);
                        LogUtil.logI("创建目录成功");
                    }
                    LogUtil.logI("存在");
                    client.logout();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FTPIllegalReplyException e) {
                    e.printStackTrace();
                } catch (FTPException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }*/
   /* public void ftpMakeResultDir() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 创建客户端
                final FTPClient client = new FTPClient();
                // 不指定端口，则使用默认端口21
                String rightIP = "192.168.128.52";
                String ip = PreferenceUtil.getNetworkIP(context);
                if (!TextUtils.isEmpty(ip)) {
                    String[] ipArr = ip.split("\\.");
                    if (ipArr != null && ipArr.length == 4) {
                        rightIP = ip;
                    }
                }
                int rightPort = 21;
                String port = PreferenceUtil.getNetworkPort(context);
                if (!TextUtils.isEmpty(port)) {
                    rightPort = Integer.valueOf(rightPort);
                }
                try {
                    client.connect(rightIP, rightPort);//连接ftp服务器
                    //配置用户名
                    String user = PreferenceUtil.getNetworkUser(context);
                    String rightUser = "test";
                    if (!TextUtils.isEmpty(user)) {
                        rightUser = user;
                    }
                    //配置密码
                    String pwd = PreferenceUtil.getNetworkPwd(context);
                    String rightPwd = "test";
                    if (!TextUtils.isEmpty(pwd)) {
                        rightPwd = pwd;
                    }
                    client.login(rightUser, rightPwd);//登录到ftp
                    //配置ftp服务器根目录
                    String pathFTP = "/" + "ZIP"+"/";
                    String filePath = PreferenceUtil.getNetworkFtpPath(context);
                    if (!TextUtils.isEmpty(filePath)) {
                        pathFTP = filePath + "/";
                    }
                    //创建用户目录
                    String userCode = PreferenceUtil.getUserCode(MyApplication.getMyApplication());
                    String usrPath=pathFTP+userCode+"/";
                    client.changeDirectory(usrPath);
                    //创建用户目录下的result目录
                    if (!isDirExist(client, "result")) {
                        client.createDirectory("result");
                        LogUtil.logI("resultPath创建目录成功");
                    }
                    LogUtil.logI("result存在");
                    client.logout();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FTPIllegalReplyException e) {
                    e.printStackTrace();
                } catch (FTPException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    *//** 判断Ftp目录是否存在 ,没有原生判断目录是否存在的方法*//*
    public boolean isDirExist(FTPClient ftpClient, String dir)

    {
        try {
            ftpClient.changeDirectory(dir);
        } catch (FTPException e) {
            return false;
        } catch (IOException e) {
            return false;
        } catch (FTPIllegalReplyException e) {
            return false;
        }
        return true;
    }*/

    /**
     * 删除ftp服务器指定文件
     * @param dirName
     */
   /* private void ftpDeleteFile(final String dir, final String dirName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 创建客户端
                final FTPClient client = new FTPClient();
                // 不指定端口，则使用默认端口21
                String rightIP = "192.168.128.52";
                String ip = PreferenceUtil.getNetworkIP(context);
                if (!TextUtils.isEmpty(ip)) {
                    String[] ipArr = ip.split("\\.");
                    if (ipArr != null && ipArr.length == 4) {
                        rightIP = ip;
                    }
                }
                int rightPort = 21;
                String port = PreferenceUtil.getNetworkPort(context);
                if (!TextUtils.isEmpty(port)) {
                    rightPort = Integer.valueOf(rightPort);
                }
                try {
                    client.connect(rightIP, rightPort);
                    //配置用户名
                    String user = PreferenceUtil.getNetworkUser(context);
                    String rightUser = "test";
                    if (!TextUtils.isEmpty(user)) {
                        rightUser = user;
                    }
                    //配置密码
                    String pwd = PreferenceUtil.getNetworkPwd(context);
                    String rightPwd = "test";
                    if (!TextUtils.isEmpty(pwd)) {
                        rightPwd = pwd;
                    }
                    client.login(rightUser, rightPwd);//登录到ftp
                    //配置ftp服务器根目录
                    String pathFTP = "/" + "ZIP"+"/";
                    String filePath = PreferenceUtil.getNetworkFtpPath(context);
                    if (!TextUtils.isEmpty(filePath)) {
                        pathFTP = filePath + "/";
                    }
                    //创建用户目录
                    String userCode = PreferenceUtil.getUserCode(MyApplication.getMyApplication());
                    String resultPath=pathFTP+userCode+"/"+"result/";
                    client.changeDirectory(resultPath);
                    client.deleteFile(dirName);
                    client.logout();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FTPIllegalReplyException e) {
                    e.printStackTrace();
                } catch (FTPException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }*/
}
