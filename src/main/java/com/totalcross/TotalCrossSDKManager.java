package com.totalcross;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.totalcross.exception.SDKVersionNotFoundException;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class TotalCrossSDKManager extends DownloadManager {
    private static final String BASE_BUCKET = "totalcross-release";
    private boolean deleteDirIfSomethingGoesWrong;

    public TotalCrossSDKManager(String sdkVersion) {
        super(sdkVersion);
    }

    public void init() throws SDKVersionNotFoundException, IOException {
        configureAndCreateDirs();
        if (!verify()) {
            download();
            unzip();
        }
    }

    public boolean verify() {
        return new File(getPath(), "etc").exists();
    }

    public void configureAndCreateDirs() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        localRepositoryDir = appDirs.getUserDataDir("TotalCross", null, null);
        setPath(Paths.get(localRepositoryDir, baseFolderName).toAbsolutePath().toString());
        File dir = getPath();
        deleteDirIfSomethingGoesWrong = !dir.exists(); // Should not delete if already exists
        getPath().mkdirs();
    }

    public void download() throws SDKVersionNotFoundException, IOException {
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        try (S3Object o = s3.getObject(BASE_BUCKET,
                baseFolderName.substring(0, 3) + "/TotalCross-" + baseFolderName + ".zip")) {
            long fileSize = o.getObjectMetadata().getContentLength();

            try (InputStream inputStream = o.getObjectContent()) {
                super.download("Download TotalCross SDK " + baseFolderName, inputStream, fileSize);
            }
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                if (deleteDirIfSomethingGoesWrong) {
                    getPath().delete();
                }
                throw new SDKVersionNotFoundException(baseFolderName);
            }
            throw e;
        }
    }
}
