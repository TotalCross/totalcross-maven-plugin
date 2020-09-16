package com.totalcross;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public class JavaJDKManager {
    private String jdkPath;
    private String sdksLocalRepositoryDir;
    private static String jdkVersion = "8";

    public void downloadJDK() {
        System.out.println("Downloading JDK");
        try {
            String os = null;
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                os = "windows";
            } else if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
                os = "linux";
            } else if(System.getProperty("os.name").toLowerCase().startsWith("mac")) {
                os = "macos";
            }
            URL url = new URL("https://api.azul.com/zulu/download/community/v1.0/bundles/latest/binary/?jdk_version=" + jdkVersion + "&ext=zip&os=" + os + "&arch=x86&hw_bitness=64");
            URLConnection connection = url.openConnection();
            int fileSize = connection.getContentLength();
            long read_len = 0;
            long written = 0;
            ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream());
            File jdkDir = new File(sdksLocalRepositoryDir);
            if(!jdkDir.exists()) {
                jdkDir.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(new File(sdksLocalRepositoryDir, "zulu_jdk_1-8.zip"));
            FileChannel fileChannel = fileOutputStream.getChannel();
            ProgressBar pb = new ProgressBar("Download JDK", fileSize);
            pb.setExtraMessage("Downloading JDK " + jdkVersion);
            while((read_len = fileChannel.transferFrom(readableByteChannel, written, 4096)) != 0) {
                pb.stepBy(read_len);
                written += read_len;
            }
            fileOutputStream.close();
            pb.close();
            File jdkDir = new File(sdksLocalRepositoryDir);
            if(!jdkDir.exists()) {
                jdkDir.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(new File(sdksLocalRepositoryDir, "zulu_jdk_1-8.zip"));
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            fileOutputStream.close();
        } catch(ProtocolException e) {
            e.printStackTrace();
            System.exit(1);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
    }

    public void init() {
        configureAndCreateDirs();
        if(!verifyJDK()) {
            downloadJDK();
            unzipJDK();
        }
    }

    public boolean verifyJDK() {
        return new File(sdksLocalRepositoryDir, "zulu_jdk_1-8").exists();
    }

    public void configureAndCreateDirs() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        if(sdksLocalRepositoryDir != null) return;
        sdksLocalRepositoryDir = appDirs.getUserDataDir("TotalCross", null, null);
    }

    public String getJdkPath() {
        return jdkPath;
    }

    public void unzipJDK() {
        try {
            ZipFile zipFile = new ZipFile(new File(sdksLocalRepositoryDir, "zulu_jdk_1-8.zip"));
            if(!zipFile.getFile().exists()) return;
            zipFile.extractAll(sdksLocalRepositoryDir);
            List<FileHeader> filesOnZip = zipFile.getFileHeaders();
            String firstFileOnZip = filesOnZip.get(0).getFileName();
            firstFileOnZip = firstFileOnZip.substring(0, firstFileOnZip.length() - 1);
            rename(firstFileOnZip, "zulu_jdk_1-8");
            FileUtils.deleteDirectory(new File(sdksLocalRepositoryDir,firstFileOnZip));
            FileUtils.deleteDirectory(new File(sdksLocalRepositoryDir,"zulu_jdk_1-8.zip"));

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void rename(String from, String to) throws IOException {
        File file = new File(sdksLocalRepositoryDir, from);
        File toFile = new File(sdksLocalRepositoryDir, to);
        if (!file.renameTo(toFile) && System.getProperty("os.name").startsWith("Windows")) {
            File fromFile = new File(sdksLocalRepositoryDir, from);
            FileUtils.copyDirectoryStructure(fromFile, toFile);
            FileUtils.deleteDirectory(file);
        }
        setJdkPath(toFile.getAbsolutePath().toString());
    }

    public void setJdkPath(String jdkPath) {
        this.jdkPath = jdkPath;
    }

    public String getSdksLocalRepositoryDir() {
        return sdksLocalRepositoryDir;
    }

    public void setSdksLocalRepositoryDir(String sdksLocalRepositoryDir) {
        this.sdksLocalRepositoryDir = sdksLocalRepositoryDir;
    }
}
