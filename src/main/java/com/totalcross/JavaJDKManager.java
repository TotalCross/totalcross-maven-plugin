package com.totalcross;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.codehaus.plexus.util.FileUtils;

import me.tongfei.progressbar.ProgressBar;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class JavaJDKManager extends DownloadManager {
    private String jdkPath;
    private static String jdkVersion = "8";

    public JavaJDKManager() {
        super("zulu_jdk_1-8");
    }

    public void init() {
        configureAndCreateDirs();
        if (!verify()) {
            download();
            unzip("zulu_jdk_1-8.zip", "zulu_jdk_1-8");
        } else {
            setPath(new File(localRepositoryDir, "zulu_jdk_1-8").getAbsolutePath());
        }
    }

    public boolean verify() {
        return new File(localRepositoryDir, "zulu_jdk_1-8").exists();
    }

    public void configureAndCreateDirs() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        if (localRepositoryDir != null)
            return;
        localRepositoryDir = appDirs.getUserDataDir("TotalCross", null, null);
    }

    public void download() {
        System.out.println("Downloading JDK");
        try {
            URL url = new URL("https://api.azul.com/zulu/download/community/v1.0/bundles/latest/binary/?jdk_version="
                    + jdkVersion + "&ext=zip&os=" + SYSTEM_OS + "&arch=x86&hw_bitness=" + SYSTEM_BITNESS);
            URLConnection connection = url.openConnection();
            File jdkDir = new File(localRepositoryDir);
            if (!jdkDir.exists()) {
                jdkDir.mkdirs();
            }

            long fileSize = connection.getContentLength();
            FileOutputStream fileOutputStream = new FileOutputStream(new File(localRepositoryDir, "zulu_jdk_1-8.zip"));
            
            super.download("Download JDK " + jdkVersion, connection.getInputStream(), fileOutputStream, fileSize);
            
            fileOutputStream.close();
        } catch (ProtocolException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    protected void rename(String from, String to) throws IOException {
        File file = new File(localRepositoryDir, from);
        File toFile = new File(localRepositoryDir, to);
        if (!file.renameTo(toFile) && isWindows) {
            File fromFile = new File(localRepositoryDir, from);
            FileUtils.copyDirectoryStructure(fromFile, toFile);
            FileUtils.deleteDirectory(file);
        }
        setPath(toFile.getAbsolutePath().toString());
    }

    public String getPath() {
        return jdkPath;
    }

    public void setPath(String jdkPath) {
        this.jdkPath = jdkPath;
        if (isMac) {
            this.jdkPath += "/zulu-8.jre/Contents/Home";
        }
    }
}
