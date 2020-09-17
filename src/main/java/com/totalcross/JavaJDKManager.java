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
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

import me.tongfei.progressbar.ProgressBar;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

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
            unzip();
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
                    + jdkVersion + "&ext=zip&os=" + SYSTEM_OS + "&arch=x86&hw_bitness=64");
            URLConnection connection = url.openConnection();
            int fileSize = connection.getContentLength();
            long read_len = 0;
            long written = 0;
            ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream());
            File jdkDir = new File(localRepositoryDir);
            if (!jdkDir.exists()) {
                jdkDir.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(
                    new File(localRepositoryDir, "zulu_jdk_1-8.zip"));
            FileChannel fileChannel = fileOutputStream.getChannel();
            ProgressBar pb = new ProgressBar("Download JDK", fileSize);
            pb.setExtraMessage("Downloading JDK " + jdkVersion);
            while ((read_len = fileChannel.transferFrom(readableByteChannel, written, 4096)) != 0) {
                pb.stepBy(read_len);
                written += read_len;
            }
            fileOutputStream.close();
            pb.close();
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

    public void unzip() {
        try {
            ZipFile zipFile = new ZipFile(new File(localRepositoryDir, "zulu_jdk_1-8.zip"));
            if (!zipFile.getFile().exists())
                return;
            zipFile.extractAll(localRepositoryDir);
            List<FileHeader> filesOnZip = zipFile.getFileHeaders();
            String firstFileOnZip = filesOnZip.get(0).getFileName();
            firstFileOnZip = firstFileOnZip.substring(0, firstFileOnZip.length() - 1);
            rename(firstFileOnZip, "zulu_jdk_1-8");
            FileUtils.deleteDirectory(new File(localRepositoryDir, firstFileOnZip));
            FileUtils.deleteDirectory(new File(localRepositoryDir, "zulu_jdk_1-8.zip"));

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void rename(String from, String to) throws IOException {
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
