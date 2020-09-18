package com.totalcross;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class JavaJDKManager extends DownloadManager {
    private static String jdkVersion = "8";

    public JavaJDKManager(String localRepositoryDir) {
        super(localRepositoryDir, "zulu_jdk_1-8");
    }

    public JavaJDKManager() {
        super("zulu_jdk_1-8");
    }

    public void init() throws IOException {
        if (!verify()) {
            download();
            unzip();
        }
        setPath(new File(localRepositoryDir, baseFolderName).getAbsolutePath());
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

        try (InputStream inputStream = connection.getInputStream()) {
            super.download("Download JDK " + jdkVersion, inputStream, fileSize);
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
