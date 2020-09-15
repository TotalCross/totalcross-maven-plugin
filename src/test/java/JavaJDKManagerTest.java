import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import com.totalcross.JavaJDKManager;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JavaJDKManagerTest {
    private static JavaJDKManager javaJDKManager = new JavaJDKManager();
    private static String repoTestDir = System.getProperty("user.home") + File.separator + "TotalCrossTestRepo";


    @BeforeAll
    static void setUpTest() {
        javaJDKManager.setSdksLocalRepositoryDir(repoTestDir);
    }

    @AfterAll
    static void wipeTest() {
        try {
            FileUtils.deleteDirectory(javaJDKManager.getSdksLocalRepositoryDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void downloadAndUnzip() {
        javaJDKManager.init();
        String jdkDir = javaJDKManager.getJdkPath();

        assertEquals(true, new File(jdkDir).exists(), "JDK dir should exist");
        assertEquals(true, new File(jdkDir).isDirectory(), "JDK dir should be a directory");
        assertEquals(true, new File(jdkDir, "bin").exists(), "bin dir should be a exists");
        assertEquals(true, new File(jdkDir, "lib").exists(), "lib dir should be a exists");
        assertEquals(false, new File(jdkDir, "tempjdk.zip").exists(), "tempjdk.zip should have been deleted");
    }
}
