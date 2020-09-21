package com.totalcross;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "retrolambda", requiresDependencyResolution = ResolutionScope.COMPILE)
public class TotalCrossRetroLambdaMojo extends AbstractMojo {

        @Component
        private MavenProject mavenProject;

        @Component
        private MavenSession mavenSession;

        @Component
        private BuildPluginManager pluginManager;

        @Component
        private PluginDescriptor descriptor;

        public void execute() throws MojoExecutionException, MojoFailureException {

                executeMojo(plugin(groupId("net.orfjackal.retrolambda"), artifactId("retrolambda-maven-plugin"),
                                version("2.5.7")), goal("process-main"), configuration(),
                                executionEnvironment(mavenProject, mavenSession, pluginManager));
        }
}
