package com.totalcross;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

import me.tongfei.progressbar.ProgressBar;
import net.harawata.appdirs.AppDirsFactory;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public abstract class DownloadManager {
   public static final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
   public static final boolean isLinux = System.getProperty("os.name").toLowerCase().startsWith("linux");
   public static final boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac");
   public static final boolean is64bits = System.getProperty("os.arch").contains("64");
   public static final String SYSTEM_OS = isWindows ? "windows" : isLinux ? "linux" : isMac ? "macos" : "undefined";
   public static final String SYSTEM_BITNESS = is64bits ? "64" : "32";

   protected final String localRepositoryDir;

   private File path;
   public final String baseFolderName;

   DownloadManager(String baseFolderName) {
      this(AppDirsFactory.getInstance().getUserDataDir("TotalCross", null, null), baseFolderName);
   }

   DownloadManager(String localRepositoryDir, String baseFolderName) {
      this.localRepositoryDir = localRepositoryDir;
      this.baseFolderName = baseFolderName;
      setPath(new File(localRepositoryDir, baseFolderName).getAbsolutePath());
   }

   public String getLocalRepositoryDir() {
      return this.localRepositoryDir;
   }

   public File getPath() {
      return path;
   }

   protected void setPath(String path) {
      this.path = new File(path);
   }

   protected boolean verify(String subpath) {
      return new File(path, subpath).exists();
   }

   public boolean verify() {
      return path.exists();
   }

   private void rename(String from, String to) throws IOException {
      File file = new File(localRepositoryDir, from);
      File toFile = new File(localRepositoryDir, to);
      if (!file.renameTo(toFile) && isWindows) {
         File fromFile = new File(localRepositoryDir, from);
         FileUtils.copyDirectoryStructure(fromFile, toFile);
         FileUtils.deleteDirectory(file);
      }
   }

   public void unzip() throws IOException {
         ZipFile zipFile = new ZipFile(new File(localRepositoryDir, baseFolderName + ".zip"));
         if (!zipFile.getFile().exists())
            return;
         zipFile.extractAll(localRepositoryDir);
         List<FileHeader> filesOnZip = zipFile.getFileHeaders();
         String firstFileOnZip = filesOnZip.get(0).getFileName();
         if (filesOnZip.get(0).isDirectory()) {
            firstFileOnZip = firstFileOnZip.substring(0, firstFileOnZip.length() - 1);
         }
         rename(firstFileOnZip, baseFolderName);
         FileUtils.deleteDirectory(new File(localRepositoryDir, firstFileOnZip));
         FileUtils.deleteDirectory(new File(localRepositoryDir, baseFolderName + ".zip"));
   }

   protected void download(String taskName, InputStream input, long inputSize) throws IOException {
      try (final FileOutputStream output = new FileOutputStream(
            new File(localRepositoryDir, baseFolderName + ".zip"))) {
         final ReadableByteChannel readableByteChannel = Channels.newChannel(input);
         final FileChannel fileChannel = output.getChannel();
         final ProgressBar pb = new ProgressBar(taskName, inputSize);
         for (long ret = 0, written = 0; (ret = fileChannel.transferFrom(readableByteChannel, written,
               8192)) != 0; written += ret) {
            pb.stepBy(ret);
         }
         pb.close();
      }
   }
}
