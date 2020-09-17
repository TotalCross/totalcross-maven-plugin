import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.totalcross.JavaJDKManager;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class JavaJDKManagerTest {
    private static JavaJDKManager javaJDKManager = new JavaJDKManager();
    static AppDirs appDirs = AppDirsFactory.getInstance();
    private static String repoTestDir = Paths
            .get(appDirs.getUserDataDir("TotalCross", null, null), "TotalCrossTestRepo").toFile().getAbsolutePath();

    @BeforeAll
    static void setUpTest() {
        javaJDKManager.setLocalRepositoryDir(repoTestDir);
    }

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
        javaJDKManager.init();
        String jdkDir = javaJDKManager.getPath();

        assertEquals(true, new File(jdkDir).exists(), "JDK dir should exist");
        assertEquals(true, new File(jdkDir).isDirectory(), "JDK dir should be a directory");
        assertEquals(true, new File(jdkDir, "bin").exists(), "bin dir should be a exists");
        assertEquals(true, new File(jdkDir, "lib").exists(), "lib dir should be a exists");
        assertEquals(false, new File(jdkDir, "tempjdk.zip").exists(), "tempjdk.zip should have been deleted");
    }
}
