import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import com.totalcross.TotalCrossSDKManager;
import com.totalcross.exception.SDKVersionNotFoundException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TotalCrossSDKManagerTest {

    private static TotalCrossSDKManager totalCrossSDKDownloader = new TotalCrossSDKManager("6.0.4");
    private static String repoTestDir = System.getProperty("user.home") + File.separator + "TotalCrossTestRepo";

    @BeforeAll
    static void setUpTest() {
        totalCrossSDKDownloader.setLocalRepositoryDir(repoTestDir);
    }

    @AfterAll
    static void wipeTest() {
        try {
            FileUtils.deleteDirectory(totalCrossSDKDownloader.getLocalRepositoryDir());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void downloadAndUnzip() {
        try {
            totalCrossSDKDownloader.init();
        } catch (SDKVersionNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        File sdkDir = totalCrossSDKDownloader.getPath();

        assertEquals(true, sdkDir.exists(), "SDK 6.0.4 dir should exists");
        assertEquals(true, sdkDir.isDirectory(), "SDK 6.0.4 dir should be a directory");
        assertEquals(true, new File(sdkDir + File.separator + "dist").exists(), "dist dir should be a exists");
        assertEquals(true, new File(sdkDir + File.separator + "docs").exists(), "docs dir should be a exists");
        assertEquals(true, new File(sdkDir + File.separator + "etc").exists(), "etc dir should be a exists");
        assertEquals(true, new File(sdkDir + File.separator + "src").exists(), "src dir should be a exists");
        assertEquals(true, new File(sdkDir + File.separator + "license.txt").exists(),
                "license.txt dir should be a exists");

        assertEquals(false,
                new File(totalCrossSDKDownloader.getLocalRepositoryDir(), totalCrossSDKDownloader.baseFolderName + ".zip").exists(),
                "zip file should have been deleted");
        assertEquals(false,
                new File(totalCrossSDKDownloader.getLocalRepositoryDir() + File.separator + "TotalCross").exists(),
                "TotalCross folder inside TotalCross local repo should have been deleted");

    }

    @Test
    void downloadNonExistingVersion() {
        totalCrossSDKDownloader = new TotalCrossSDKManager("nonExistingVersion");
        assertThrows(SDKVersionNotFoundException.class, () -> totalCrossSDKDownloader.init(),
                "Should throws SDKVersionNotFoundException");
        File sdkDir = totalCrossSDKDownloader.getPath();

        assertEquals(false, sdkDir.exists(), "SDK nonExistingVersion dir should exists");

    }

}