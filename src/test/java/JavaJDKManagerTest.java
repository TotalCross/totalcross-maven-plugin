import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.totalcross.JavaJDKManager;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class JavaJDKManagerTest {
    static AppDirs appDirs = AppDirsFactory.getInstance();
    private static String repoTestDir = Paths
            .get(appDirs.getUserDataDir("TotalCross", null, null), "TotalCrossTestRepo").toFile().getAbsolutePath();
    private static JavaJDKManager javaJDKManager = new JavaJDKManager(repoTestDir);

    @AfterAll
    static void wipeTest() {
        try {
            FileUtils.deleteDirectory(javaJDKManager.getLocalRepositoryDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void downloadAndUnzip() {
        try {
            javaJDKManager.init();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        File jdkDir = javaJDKManager.getPath();

        assertEquals(true, jdkDir.exists(), "JDK dir should exist");
        assertEquals(true, jdkDir.isDirectory(), "JDK dir should be a directory");
        assertEquals(true, new File(jdkDir, "bin").exists(), "bin dir should be a exists");
        assertEquals(true, new File(jdkDir, "lib").exists(), "lib dir should be a exists");
        assertEquals(false, new File(jdkDir, "tempjdk.zip").exists(), "tempjdk.zip should have been deleted");
    }
}
