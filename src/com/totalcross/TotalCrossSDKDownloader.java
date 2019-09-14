package com.totalcross;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import me.tongfei.progressbar.ProgressBar;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TotalCrossSDKDownloader {

    private String version;
    private String sdkDir;
    private String sdksLocalRepositoryDir;
    private final String baseBucket = "totalcross-release";

    public TotalCrossSDKDownloader (String version) {
        this.version = version;
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
        sdksLocalRepositoryDir = System.getProperty("user.home")
                + File.separator +  "TotalCross";
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
            File zipFile = new File(sdksLocalRepositoryDir + File.separator + "temp.zip");
            FileInputStream fin = new FileInputStream(zipFile);

            //create ZipInputStream from FileInputStream object
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry entry = null;
            while ((entry = zin.getNextEntry()) != null)
            {
                String name = entry.getName();
                if( entry.isDirectory() )
                {
                    File f = new File(sdkDir + File.separator + name);
                    f.mkdirs();
                    continue;
                }
                /* this part is necessary because file entry can come before
                 * directory entry where is file located
                 * i.e.:
                 *   /foo/foo.txt
                 *   /foo/
                 */
                String outputFileName = name.substring("TotalCross/".length()); // Skip TotalCross
                String dir = dirpart(outputFileName);
                if( dir != null ) {
                    new File(sdkDir + File.separator + dir).mkdirs();
                }

                OutputStream os = new FileOutputStream(new File(sdkDir + File.separator + outputFileName));


                byte[] buffer = new byte[1024];
                int length;

                //read the entry from zip file and extract it to disk
                while ((length = zin.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }

                //close the streams
                os.close();
            }
            //crate OutputStream to extract the entry from zip file

            //close the zip file
            zin.close();
            zipFile.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String dirpart(String name)
    {
        int s = name.lastIndexOf( File.separatorChar );
        return s == -1 ? null : name.substring( 0, s );
    }

    public String getSdkDir() {
        return sdkDir;
    }
}
