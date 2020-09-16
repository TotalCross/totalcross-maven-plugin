package com.totalcross;

import com.totalcross.exception.SDKVersionNotFoundException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "package", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class TotalCrossMojo extends AbstractMojo {

    @Parameter
    private String name;

    @Parameter
    private String activationKey;

    @Parameter
    private String[] platforms;

    @Parameter
    private String[] externalResources;

    @Parameter
    private String certificates;

    @Parameter
    private String totalcrossHome;

    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private String outputDirectory;

    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String finalName;

    @Parameter(defaultValue = "${project.packaging}", required = true)
    private String packaging;

    @Parameter
    private boolean totalcrossLib;

    @Parameter
    private String jdkPath;

    @Component
    private MavenProject mavenProject;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Component
    private PluginDescriptor descriptor;

    private ArrayList<Element> args;

    private String sdkVersion;

    private String projectClassPath = "";

    String classPathSeparator = System.getProperty("os.name").startsWith("Windows") ? ";" : ":";

    Artifact totalcrossArtifact;

    private ArrayList<String> totalcrossLibs = new ArrayList<String>();

    private TCZUtils tczUtils;

    public void execute() throws MojoExecutionException, MojoFailureException {
        tczUtils = new TCZUtils(mavenProject);
        tczUtils.setAdditionalFilePaths(externalResources);
        addDependenciesToClasspath();
        setupSDKPath();
        setupArguments();
        deploy();
        if (totalcrossLib) {
            String pathToTCZ = Paths.get(outputDirectory, name + ".tcz").toAbsolutePath().toString();
            tczUtils.addFileToJar(pathToTCZ);
        }
    }

    private void addDependenciesToClasspath() {
        getLog().info("┌───────── Adding Dependencies to classPath ──────────┐");
        int countTCDeps = 0;
        for (Artifact artifact : mavenProject.getArtifacts()) {
            final File file = artifact.getFile();
            projectClassPath += file.getAbsolutePath() + classPathSeparator;
            String output = "│ "+ artifact.getArtifactId() + ".jar";
            if (tczUtils.extractTCZsFromArtifactDependency(artifact)) {
                output += " (TotalCross Library)";
                countTCDeps++;
            }
            getLog().info(output);
        }
        tczUtils.includeTCZLibsOnAllPKG();
        getLog().info("├─────────────────────────────────────────────────────");
        getLog().info("│ TotalCross Libraries: " + countTCDeps + " | Total: " + mavenProject.getArtifacts().size());
        getLog().info("└─────────────────────────────────────────────────────┘");

        projectClassPath = projectClassPath.substring(0, projectClassPath.length() - 1); // removes last : or ;
    }

    private void setupSDKPath () {

        //Setup environment variable
        if(totalcrossHome == null) {    // check if SDK path is provided, if not
                                        // totalCrossDownloader will check if SDK
                                        // exists, if not, will download it.
            TotalCrossSDKManager totalCrossSDKDownloader = new TotalCrossSDKManager(mavenProject);
            try {
                totalCrossSDKDownloader.init();
            } catch (SDKVersionNotFoundException e) {
                getLog().error(e);
            }
            totalcrossHome = totalCrossSDKDownloader.getSdkDir();
        }
        JavaJDKManager javaJDKManager = new JavaJDKManager();
        javaJDKManager.init();
        jdkPath = javaJDKManager.getJdkPath();
    }

    private String getJarsInsidePath(String path) {
        String returnPath = "";
        String[] extensions = { "jar" };
        Collection<File> files = FileUtils.listFiles(new File(path), extensions, true);
        Iterator<File> iterator = files.iterator();

        while (iterator.hasNext()) {
            returnPath += iterator.next().getAbsolutePath();
            if (!iterator.hasNext()) {
                break;
            }
            returnPath += classPathSeparator;
        }
        return returnPath;
    }

    public void setupArguments() throws MojoExecutionException {
        args = new ArrayList<Element>();
        args.add(element("argument", "-cp")); // exec -classpath argument
        Artifact totalcrossArtifact = mavenProject.getArtifactMap()
                    .get(ArtifactUtils.versionlessKey("com.totalcross"
                            , "totalcross-sdk"));
        sdkVersion = totalcrossArtifact.getVersion();

        String requiredClassPath = null;
        if (totalcrossArtifact.getScope().equals("system")) {
            requiredClassPath = projectClassPath + classPathSeparator + getJarsInsidePath(totalcrossHome);
        } else {
            requiredClassPath = projectClassPath;
        }
        args.add(element("argument", requiredClassPath)); // auto generate a classpath

        args.add(element("argument", "tc.Deploy"));
        args.add(element("argument", "${project.build.directory}/${project.build.finalName}.${project.packaging}"));

        if (platforms != null && !totalcrossLib) {
            for (int i = 0; i < platforms.length; i++) { // each platform
                args.add(element("argument", platforms[i]));
            }
        }

        // Add app name
        if(name != null && !totalcrossLib) {
            args.add(element("argument", "/n"));
            args.add(element("argument", name));
            
        }
        else if(totalcrossLib) {
            args.add(element("argument", "/n"));
            name = mavenProject.getArtifactId();
            name = TCZUtils.verifyAndFixLibName(name);
            args.add(element("argument", name));
        }

        args.add(element("argument", "/p"));
        // Add activation key
        args.add(element("argument", "/r"));
        args.add(element("argument", activationKey));

        // /m parameter
        if (certificates != null) {
            args.add(element("argument", "/m"));
            args.add(element("argument", certificates));
        }
    }

    public void deploy() throws MojoExecutionException {

        Element environmentVariables = element("environmentVariables", element("TOTALCROSS3_HOME", totalcrossHome));

        Element[] elements = new Element[args.size()];
        elements = args.toArray(elements);
        String javaCommand = Paths.get(jdkPath, "bin", "java").toFile().getAbsolutePath();
        executeMojo(
                plugin(groupId("org.codehaus.mojo"),
                        artifactId("exec-maven-plugin"),
                        version("1.6.0")),
                goal("exec"),
                configuration(
                        environmentVariables,
                        element("executable", javaCommand),
                        element(name("arguments"), elements)
                ),  
                executionEnvironment(
                        mavenProject,
                        mavenSession,
                        pluginManager
                )
        );
    }
}
