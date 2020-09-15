package com.totalcross;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public class JavaJDKManager {
    private String jdkPath;
    private String sdksLocalRepositoryDir;

    public void downloadJDK() {
        System.out.println("to baxano");
        try {
            String command = "curl -LJO \"https://api.azul.com/zulu/download/community/v1.0/bundles/latest/binary/?jdk_version=8&ext=zip&os=windows&arch=x86&hw_bitness=64\"";
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            File jdkDir = new File(sdksLocalRepositoryDir);
            if(!jdkDir.exists()) {
                jdkDir.mkdirs();
            }
            processBuilder.directory(jdkDir);
            Process process = processBuilder.start();
            process.waitFor();
            Path f1 = Paths.get(sdksLocalRepositoryDir, "_jdk_version=8&ext=zip&os=windows&arch=x86&hw_bitness=64");
            Files.move(f1, f1.resolveSibling("tempjdk.zip"), StandardCopyOption.REPLACE_EXISTING);
        } catch(InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
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
            ZipFile zipFile = new ZipFile(new File(sdksLocalRepositoryDir, "tempjdk.zip"));
            if(!zipFile.getFile().exists()) return;
            zipFile.extractAll(sdksLocalRepositoryDir);
            List<FileHeader> filesOnZip = zipFile.getFileHeaders();
            String firstFileOnZip = filesOnZip.get(0).getFileName();
            if(firstFileOnZip.endsWith("\\") || firstFileOnZip.endsWith("/")) {
                firstFileOnZip = firstFileOnZip.substring(0, firstFileOnZip.length() - 1);
            }
            rename(firstFileOnZip, "zulu_jdk_1-8");
            FileUtils.deleteDirectory(new File(sdksLocalRepositoryDir,firstFileOnZip));
            FileUtils.deleteDirectory(new File(sdksLocalRepositoryDir,"tempjdk.zip"));

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
