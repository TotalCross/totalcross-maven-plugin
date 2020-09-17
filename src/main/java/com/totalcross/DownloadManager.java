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

   protected void download(String taskName, InputStream input, FileOutputStream output, long inputSize)
         throws IOException {
      final ReadableByteChannel readableByteChannel = Channels.newChannel(input);
      final FileChannel fileChannel = output.getChannel();
      final ProgressBar pb = new ProgressBar(taskName, inputSize);
      for (long ret = 0, written = 0; 
            (ret = fileChannel.transferFrom(readableByteChannel, written, 8192)) != 0; 
            written += ret) {
         pb.stepBy(ret);
      }
      pb.close();
   }
}
