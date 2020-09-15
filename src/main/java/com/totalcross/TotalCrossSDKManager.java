package com.totalcross;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.totalcross.exception.SDKVersionNotFoundException;
import me.tongfei.progressbar.ProgressBar;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;


public class TotalCrossSDKManager {

    private String version;
    private String sdkDir;
    private String sdksLocalRepositoryDir;
    private final String baseBucket = "totalcross-release";
    private boolean deleteDirIfSomethingGoesWrong;
    public TotalCrossSDKManager(MavenProject mavenProject) {
        Artifact totalcrossArtifact = mavenProject.getArtifactMap().get(ArtifactUtils.versionlessKey("com.totalcross", "totalcross-sdk"));
        this.version = totalcrossArtifact.getVersion();
    }

    public TotalCrossSDKManager(String sdkVersion) {
       this.version = sdkVersion;
    }

    public void init() throws SDKVersionNotFoundException {
        configureAndCreateDirs();
        if(verifyDir()) return; // No need to download sdk
        downloadSDK();
        unzipSDK();
    }

    public boolean verifyDir() {
        return new File(sdkDir + File.separator + "etc").exists();
    }

    public void configureAndCreateDirs() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        sdksLocalRepositoryDir =  appDirs.getUserDataDir("TotalCross", null, null);
        sdkDir = Paths.get(sdksLocalRepositoryDir, version).toAbsolutePath().toString();
        File dir = new File(sdkDir);
        deleteDirIfSomethingGoesWrong = !dir.exists(); // Should not delete if already exists
        new File(sdkDir).mkdirs();
    }

    public void downloadSDK() throws SDKVersionNotFoundException {
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
            pb.close();
            s3is.close();
            fos.close();
        } catch (AmazonServiceException e) {
            if(e instanceof AmazonS3Exception && ((AmazonS3Exception)e).getStatusCode() == 404) {
                if(deleteDirIfSomethingGoesWrong)  {
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

    public void unzip(String source, String dest) {
        try {
            ZipFile zipFile = new ZipFile(new File(sdksLocalRepositoryDir, source));
            if(!zipFile.getFile().exists()) return;
            zipFile.extractAll(sdksLocalRepositoryDir);
            List<FileHeader> filesOnZip = zipFile.getFileHeaders();
            String firstFileOnZip = filesOnZip.get(0).getFileName();
            if(firstFileOnZip.endsWith("\\") || firstFileOnZip.endsWith("/")) {
                firstFileOnZip = firstFileOnZip.substring(0, firstFileOnZip.length() - 1);
            }
            rename(firstFileOnZip, dest);
            FileUtils.deleteDirectory(new File(sdksLocalRepositoryDir,firstFileOnZip));
            FileUtils.deleteDirectory(new File(sdksLocalRepositoryDir,source));

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void rename(String from, String to) throws IOException {
        new File(sdksLocalRepositoryDir, from).renameTo(new File(sdksLocalRepositoryDir, to));

        if (System.getProperty("os.name").startsWith("Windows")) {
            File fromFile = new File(sdksLocalRepositoryDir, from);
            File toFile = new File(sdksLocalRepositoryDir, to);
            FileUtils.copyDirectoryStructure(fromFile, toFile);
            FileUtils.deleteDirectory(new File(sdksLocalRepositoryDir, from));
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
