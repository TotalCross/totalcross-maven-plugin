package com.totalcross;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.totalcross.exception.SDKVersionNotFoundException;

import org.codehaus.plexus.util.FileUtils;

import me.tongfei.progressbar.ProgressBar;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class TotalCrossSDKManager extends DownloadManager {

    private String version;
    private String sdkDir;
    private final String baseBucket = "totalcross-release";
    private boolean deleteDirIfSomethingGoesWrong;

    public TotalCrossSDKManager(String sdkVersion) {
        super(sdkVersion);
        this.version = sdkVersion;
    }

    public void init() throws SDKVersionNotFoundException {
        configureAndCreateDirs();
        if (verify())
            return; // No need to download sdk
        download();
        unzip("temp.zip", version);
    }

    public boolean verify() {
        return new File(sdkDir + File.separator + "etc").exists();
    }

    public void configureAndCreateDirs() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        localRepositoryDir = appDirs.getUserDataDir("TotalCross", null, null);
        sdkDir = Paths.get(localRepositoryDir, version).toAbsolutePath().toString();
        File dir = new File(sdkDir);
        deleteDirIfSomethingGoesWrong = !dir.exists(); // Should not delete if already exists
        new File(sdkDir).mkdirs();
    }

    public void download() throws SDKVersionNotFoundException {
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        try {
            S3Object o = s3.getObject(baseBucket, version.substring(0, 3) + "/TotalCross-" + version + ".zip");

            System.out.println(o.getBucketName());
            System.out.println(o.getKey());
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(new File(localRepositoryDir + File.separator + "temp.zip"));
            byte[] read_buf = new byte[1024];
            int read_len = 0;

            long fileSize = o.getObjectMetadata().getContentLength();
            ProgressBar pb = new ProgressBar("Download SDK", fileSize);
            pb.setExtraMessage("Downloading TotalCross SDK " + version);

            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
                pb.stepBy(read_len);
            }
            pb.close();
            s3is.close();
            fos.close();
        } catch (AmazonServiceException e) {
            if (e instanceof AmazonS3Exception && ((AmazonS3Exception) e).getStatusCode() == 404) {
                if (deleteDirIfSomethingGoesWrong) {
                    new File(sdkDir).delete();
                }
                throw new SDKVersionNotFoundException(version);
            }
            e.printStackTrace();
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
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
    }

    public String getSdkDir() {
        return sdkDir;
    }

    public void setPath(String sdkDir) {
        this.sdkDir = sdkDir;
    }

    public String getBaseBucket() {
        return this.baseBucket;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
