import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import com.totalcross.TotalCrossSDKManager;
import org.codehaus.plexus.util.FileUtils;
import com.totalcross.exception.SDKVersionNotFoundException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TotalCrossSDKManagerTest {

    private static TotalCrossSDKManager totalCrossSDKDownloader = new TotalCrossSDKManager("6.0.4");
    private static String repoTestDir = System.getProperty("user.home") + File.separator + "TotalCrossTestRepo";

    @BeforeAll
    static void setUpTest() {
        totalCrossSDKDownloader.setSdksLocalRepositoryDir(repoTestDir);
    }

    @AfterAll
    static void wipeTest() {
        try {
            FileUtils.deleteDirectory(totalCrossSDKDownloader.getSdksLocalRepositoryDir());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    void downloadAndUnzip() {
        try {
            totalCrossSDKDownloader.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sdkDir = totalCrossSDKDownloader.getSdkDir();
        
        assertEquals(true, new File(sdkDir).exists(), "SDK 6.0.4 dir should exists");
        assertEquals(true, new File(sdkDir).isDirectory(), "SDK 6.0.4 dir should be a directory");
        assertEquals(true, new File(sdkDir + File.separator + "dist").exists(), "dist dir should be a exists");
        assertEquals(true, new File(sdkDir + File.separator + "docs").exists(), "docs dir should be a exists");
        assertEquals(true, new File(sdkDir + File.separator + "etc").exists(), "etc dir should be a exists");
        assertEquals(true, new File(sdkDir + File.separator + "src").exists(), "src dir should be a exists");
        assertEquals(true, new File(sdkDir + File.separator + "license.txt").exists(), "license.txt dir should be a exists");

        assertEquals(
            false, 
            new File(totalCrossSDKDownloader.getSdksLocalRepositoryDir() + File.separator + "temp.zip").exists(),
            "temp.zip should have been deleted");
        assertEquals(
            false, 
            new File(totalCrossSDKDownloader.getSdksLocalRepositoryDir() + File.separator + "TotalCross").exists(),
            "TotalCross folder inside TotalCross local repo should have been deleted");

        
    }

    @Test
    void downloadNonExistingVersion() {
        totalCrossSDKDownloader = new TotalCrossSDKManager("nonExistingVersion");
        assertThrows(SDKVersionNotFoundException.class,
                () -> totalCrossSDKDownloader.init(),
                "Should throws SDKVersionNotFoundException");
        String sdkDir = totalCrossSDKDownloader.getSdkDir();

        assertEquals(false, new File(sdkDir).exists(), "SDK nonExistingVersion dir should exists");

    }

}