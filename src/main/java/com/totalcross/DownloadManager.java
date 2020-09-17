package com.totalcross;

import java.io.File;

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
}
