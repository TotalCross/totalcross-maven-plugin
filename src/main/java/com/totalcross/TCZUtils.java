package com.totalcross;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

public class TCZUtils {

    MavenProject mavenProject;
    private String outputDirectory;
    ArrayList<String> totalcrossLibs;
    Logger logger;
    private String pathToFinalJar;
    /**
     * additional files to be added to the new all.pkg created at the target folder
     */
    private String[] additionalFilePaths;

    TCZUtils(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
        this.outputDirectory = mavenProject.getBuild().getDirectory();
        this.totalcrossLibs = new ArrayList<String>();
        this.pathToFinalJar = Paths.get(outputDirectory, mavenProject.getBuild().getFinalName() + ".jar")
                .toAbsolutePath().toString();
    }

    /**
     * If the artifact is a totalcross library, it will extract the library tcz with
     * name 'artifactId'Lib.tcz to output directory inside totalcross-lib folder.
     * 
     * @param artifact
     * @return It returns true, if this library is a totalcross library, otherwise
     *         returns false.
     */
    public boolean extractTCZsFromArtifactDependency(Artifact artifact) {
        String path = artifact.getFile().getAbsolutePath();

        ZipFile zipFile = new ZipFile(path);
        String libName = artifact.getArtifactId();

        try {
            String tczFileName = verifyAndFixLibName(libName) + ".tcz";
            FileHeader fileHeader = zipFile.getFileHeader(tczFileName);
            String libDir = Paths.get(outputDirectory, "totalcross-libs").toAbsolutePath().toString();
            File libDirFile = new File(libDir);
            libDirFile.mkdirs();

            if (fileHeader != null) {
                zipFile.extractFile(tczFileName, libDir);
                String tczPath = Paths.get(libDir, tczFileName).toAbsolutePath().toString();
                totalcrossLibs.add(tczPath);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Create a all.pkg inside outputdirectory with the paths to all files inside
     * totalcross-libs at build directory.
     * 
     * @return true if it included at least one library.
     */
    public boolean includeTCZLibsOnAllPKG() {
        if (totalcrossLibs.size() < 1)
            return false;
        ZipFile zipFile = new ZipFile(pathToFinalJar);

        try {
            FileHeader fileHeader = zipFile.getFileHeader("all.pkg");

            String allPKGPath = Paths.get(outputDirectory, "all.pkg").toAbsolutePath().toString();
            if (fileHeader != null) {
                zipFile.extractFile("all.pkg", outputDirectory);
            }
            File allPKG = new File(allPKGPath);
            FileOutputStream fos = new FileOutputStream(allPKG, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (String tczPath : totalcrossLibs) {
                bw.write("[L]" + tczPath);
                bw.newLine();
            }
            if (additionalFilePaths != null) {
                for (String filePath : additionalFilePaths) {
                    bw.write("[L]" + filePath);
                    bw.newLine();
                }
            }
            bw.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
        return true;
    }

    /**
     * TotalCross tcz files are only loaded by the vm as totalcross libraries when
     * it ends with Lib.tcz, i.e, KnowCodeXMLLib.tcz. This function adds a Lib, to
     * the end of the word if it doesn't ends with Lib already. Used to find tcz
     * inside a jar and to create libraries.
     * 
     * @param libName
     * @return
     */
    static String verifyAndFixLibName(String libName) {
        if (!libName.substring(libName.length() - 3).equals("Lib")) {
            return libName + "Lib";
        }
        return libName;
    }

    /**
     * Add a file to the final build jar
     * 
     * @param path
     */
    public void addFileToJar(String path) {
        addFileToJar(path, pathToFinalJar);
    }

    /**
     * Add a file to a zip file.
     * 
     * @param path
     * @param zipPath
     */
    public void addFileToJar(String path, String zipPath) {
        try {
            ZipFile zipFile = new ZipFile(zipPath);
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.NORMAL);
            zipFile.addFile(new File(path), parameters);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getAdditionalFilePaths() {
        return additionalFilePaths;
    }

    public void setAdditionalFilePaths(String[] additionalFilePaths) {
        this.additionalFilePaths = additionalFilePaths;
    }
}
