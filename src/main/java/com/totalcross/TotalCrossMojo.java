package com.totalcross;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
@Mojo(name = "package", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class TotalCrossMojo extends AbstractMojo {

    @Parameter
    private String name;

    @Parameter
    private String activationKey;

    @Parameter
    private String [] platforms;

    @Parameter
    private String certificates;

    @Parameter
    private String totalcrossHome;

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

    public void execute() throws MojoExecutionException, MojoFailureException {
        addDependenciesToClasspath();
        setupSDKPath();
        setupArguments();
        deploy();
    }

    private void addDependenciesToClasspath() {
        
        for (Artifact artifact : mavenProject.getDependencyArtifacts()) {
            sdkVersion = artifact.getVersion();
            final File file = artifact.getFile();
            projectClassPath += file.getAbsolutePath() + classPathSeparator;
        }
    }

    private void setupSDKPath () {      
        //Setup environment variable
        if(totalcrossHome == null) {    // check if SDK path is provided, if not
                                        // totalCrossDownloader will check if SDK
                                        // exists, if not, will download it.
            TotalCrossSDKDownloader totalCrossSDKDownloader = new TotalCrossSDKDownloader(sdkVersion);
            totalCrossSDKDownloader.init();
            totalcrossHome = totalCrossSDKDownloader.getSdkDir();
        }
    }

    private String getFiles(String path) {
        String returnPath = "";
        String[] extensions = {"jar"};
        Collection<File> files = FileUtils.listFiles(new File(path), extensions, true);
        Iterator<File> iterator = files.iterator();
        
        while(iterator.hasNext()) {
            returnPath += iterator.next().getAbsolutePath();
            if(!iterator.hasNext()) {
                break;
            }
            returnPath += classPathSeparator;
        }
        return returnPath;
    }

    public void setupArguments() throws MojoExecutionException {
        args = new ArrayList<Element>();
        String requiredClassPath;
        if(System.getProperty("os.name").startsWith("Windows")) {
            requiredClassPath = projectClassPath + getFiles(totalcrossHome);
        } else {
            requiredClassPath = 
            projectClassPath + Paths.get(totalcrossHome, "etc", "libs", "*").toAbsolutePath();
        }
        args.add(element("argument", "-cp")); // exec -classpath argument
        args.add(element("argument", requiredClassPath)); // auto generate a classpath

        args.add(element("argument", "tc.Deploy"));
        args.add(element("argument",
                "${project.build.directory}/${project.build.finalName}.${project.packaging}"));

        if(platforms != null) {
            for (int i = 0; i < platforms.length; i++) { // each platform
                args.add(element("argument", platforms[i]));
            }
        }

        // Add app name
        args.add(element("argument", "/n"));
        args.add(element("argument", name));
        args.add(element("argument", "/p"));

        // Add activation key
        args.add(element("argument", "/r"));
        args.add(element("argument", activationKey));

        // /m parameter
        if(certificates != null) {
            args.add(element("argument", "/m"));
            args.add(element("argument", certificates));
        }
    }
    
    public void deploy() throws MojoExecutionException {

        Element environmentVariables = element("environmentVariables", "");
        environmentVariables = element("environmentVariables",
                    element("TOTALCROSS3_HOME", totalcrossHome));

        Element[] elements = new Element[args.size()];
        elements = args.toArray(elements);

        executeMojo(
                plugin(groupId("org.codehaus.mojo"),
                        artifactId("exec-maven-plugin"),
                        version("1.6.0")),
                goal("exec"),
                configuration(
                        environmentVariables,
                        element("executable", "java"),
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
