package com.totalcross;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public abstract class DownloadManager {
   public static final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
   public static final boolean isLinux = System.getProperty("os.name").toLowerCase().startsWith("linux");
   public static final boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac");
   public static final String SYSTEM_OS = isWindows ? "windows" : isLinux ? "linux" : isMac ? "macos" : "undefined";

   protected String localRepositoryDir;

   private String baseFolderName;

   DownloadManager(String baseFolderName) {
      this.baseFolderName = baseFolderName;
   }

   public String getLocalRepositoryDir() {
      return this.localRepositoryDir;
   }

   public void setLocalRepositoryDir(String localRepositoryDir) {
      this.localRepositoryDir = localRepositoryDir;
   }

   protected boolean verify(String subpath) {
      return new File(localRepositoryDir, subpath).exists();
   }

   protected abstract void rename(String from, String to) throws IOException;

   public void unzip(String source, String dest) {
      try {
         ZipFile zipFile = new ZipFile(new File(localRepositoryDir, source));
         if (!zipFile.getFile().exists())
            return;
         zipFile.extractAll(localRepositoryDir);
         List<FileHeader> filesOnZip = zipFile.getFileHeaders();
         String firstFileOnZip = filesOnZip.get(0).getFileName();
         if (filesOnZip.get(0).isDirectory()) {
            firstFileOnZip = firstFileOnZip.substring(0, firstFileOnZip.length() - 1);
         }
         rename(firstFileOnZip, dest);
         FileUtils.deleteDirectory(new File(localRepositoryDir, firstFileOnZip));
         FileUtils.deleteDirectory(new File(localRepositoryDir, source));

      } catch (Exception e) {
         e.printStackTrace();
         System.exit(1);
      }
   }
}
