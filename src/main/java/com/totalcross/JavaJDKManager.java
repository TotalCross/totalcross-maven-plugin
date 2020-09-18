package com.totalcross;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class JavaJDKManager extends DownloadManager {
    private static String jdkVersion = "8";

    public JavaJDKManager() {
        super("zulu_jdk_1-8");
    }

    public void init() throws IOException {
        configureAndCreateDirs();
        if (!verify()) {
            download();
            unzip();
        }
        setPath(new File(localRepositoryDir, baseFolderName).getAbsolutePath());
    }

    public boolean verify() {
        return new File(localRepositoryDir, baseFolderName).exists();
    }

    public void configureAndCreateDirs() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        if (localRepositoryDir != null)
            return;
        localRepositoryDir = appDirs.getUserDataDir("TotalCross", null, null);
    }

    public void download() throws IOException {
        URLConnection connection = new URL(
                "https://api.azul.com/zulu/download/community/v1.0/bundles/latest/binary/?jdk_version=" + jdkVersion
                        + "&ext=zip&os=" + SYSTEM_OS + "&arch=x86&hw_bitness=" + SYSTEM_BITNESS).openConnection();
        long fileSize = connection.getContentLength();

        File jdkDir = new File(localRepositoryDir);
        if (!jdkDir.exists()) {
            jdkDir.mkdirs();
        }

        try (InputStream inputStream = connection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(
                        new File(localRepositoryDir, baseFolderName + ".zip"))) {
            super.download("Download JDK " + jdkVersion, inputStream, fileOutputStream, fileSize);
        }
    }

    @Override
    protected void setPath(String path) {
        if (isMac) {
            path += "/zulu-8.jre/Contents/Home";
        }
        super.setPath(path);
    }
}
