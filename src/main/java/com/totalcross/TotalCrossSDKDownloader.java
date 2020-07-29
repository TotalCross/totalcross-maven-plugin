package com.totalcross;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import org.codehaus.plexus.util.FileUtils;

import me.tongfei.progressbar.ProgressBar;
import net.lingala.zip4j.ZipFile;

import java.io.*;


public class TotalCrossSDKDownloader {

    private String version;
    private String sdkDir;
    private String sdksLocalRepositoryDir;
    private final String baseBucket = "totalcross-release";

    public TotalCrossSDKDownloader (String version) {
        this.version = version;
        sdksLocalRepositoryDir = System.getProperty("user.home")
                + File.separator +  "TotalCross";
    }

    public void init() {
        configureAndCreateDirs();
        if(verifyDir()) return; // No need to download sdk
        downloadSDK();
        unzipSDK();
    }

    public boolean verifyDir() {
        return new File(sdkDir + File.separator + "etc").exists();
    }

    public void configureAndCreateDirs() {
        sdkDir = sdksLocalRepositoryDir + File.separator + version;
        new File(sdksLocalRepositoryDir).mkdir();
        new File(sdkDir).mkdir();
    }

    public void downloadSDK() {
        try {
            final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
            try {
                S3Object o = s3.getObject(baseBucket,
                          version.substring(0, 3) + "/TotalCross-"+ version + ".zip");

                System.out.println(o.getBucketName());
                System.out.println(o.getKey());
                S3ObjectInputStream s3is = o.getObjectContent();
                FileOutputStream fos = new FileOutputStream(new File(sdksLocalRepositoryDir + File.separator + "temp.zip"));
                byte[] read_buf = new byte[1024];
                int read_len = 0;

                long fileSize = o.getObjectMetadata().getContentLength();
                ProgressBar pb = new ProgressBar("Download SDK", fileSize);
                pb.setExtraMessage("Downloading TotalCross SDK " + version);

                while ((read_len = s3is.read(read_buf)) > 0) {
                    fos.write(read_buf, 0, read_len);
                    pb.stepBy(read_len);
                }
                s3is.close();
                fos.close();
            } catch (AmazonServiceException e) {
                System.err.println(e.getErrorMessage());
                System.exit(1);
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unzipSDK() {
        try {
            ZipFile zipFile = new ZipFile(sdksLocalRepositoryDir + File.separator + "temp.zip");
            zipFile.extractAll(sdksLocalRepositoryDir);
            new File(sdksLocalRepositoryDir + File.separator + "TotalCross")
                    .renameTo(new File(sdkDir));

            if (System.getProperty("os.name").startsWith("Windows")) {
                File from = new File(sdksLocalRepositoryDir + File.separator + "TotalCross");
                File dest = new File(sdkDir);
                FileUtils.copyDirectoryStructure(from, dest);
                FileUtils.deleteDirectory(sdksLocalRepositoryDir + File.separator + "TotalCross");
            }
            FileUtils.deleteDirectory(sdksLocalRepositoryDir + File.separator + "temp.zip");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String getSdkDir() {
        return sdkDir;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public void setSdkDir(String sdkDir) {
        this.sdkDir = sdkDir;
    }

    public String getSdksLocalRepositoryDir() {
        return this.sdksLocalRepositoryDir;
    }

    public void setSdksLocalRepositoryDir(String sdksLocalRepositoryDir) {
        this.sdksLocalRepositoryDir = sdksLocalRepositoryDir;
    }

    public String getBaseBucket() {
        return this.baseBucket;
    }
}
