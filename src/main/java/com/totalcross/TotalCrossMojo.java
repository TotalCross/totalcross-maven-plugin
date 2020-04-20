package com.totalcross;

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
import java.util.ArrayList;

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

    private String totalcrossSDKJARPath = null;

    public void execute() throws MojoExecutionException, MojoFailureException {
        addDependenciesToClasspath("totalcross-sdk");
        setupArguments();
        deploy();
    }

    private void addDependenciesToClasspath(String artifactId) {
        System.out.println("Number of dependencies: " + mavenProject.getDependencies().size());
        for (Artifact artifact : mavenProject.getDependencyArtifacts()) {
            if (artifact.getArtifactId().equals(artifactId)) {
                try {
                    sdkVersion = artifact.getVersion();
                    final File file = artifact.getFile();
                    totalcrossSDKJARPath = file.getAbsolutePath();
                    final URI uri = file.toURI();
                    final URL url = uri.toURL();
                    final ClassRealm realm = (ClassRealm) descriptor.getClassRealm();
                    realm.addURL(url);
                }
                catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void setupArguments() throws MojoExecutionException {
        args = new ArrayList<Element>();
        args.add(element("argument", "-cp")); // exec -classpath argument
        args.add(element("argument", totalcrossSDKJARPath)); // auto generate a classpath
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

        TotalCrossSDKDownloader totalCrossSDKDownloader = new TotalCrossSDKDownloader(sdkVersion);

        Element environmentVariables = element("environmentVariables", "");
        //Setup environment variable
        if(totalcrossHome != null) {
            environmentVariables = element("environmentVariables",
                    element("TOTALCROSS3_HOME", totalcrossHome));
        } else {
            totalCrossSDKDownloader.init();
            environmentVariables = element("environmentVariables",
                    element("TOTALCROSS3_HOME", totalCrossSDKDownloader.getSdkDir()));
        }

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
