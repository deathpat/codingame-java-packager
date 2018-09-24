package com.deathpat.codingame.packager;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 
 * @author Patrice Meneguzzi
 *
 */
@Mojo(name = "package", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MyMojo extends AbstractMojo {

  @Parameter(property = "mainClass", required = true)
  private String mainClass;

  @Parameter(property = "appPackage", required = true)
  private String appPackage;

  @Parameter(property = "outputDirectory", required = true, defaultValue = "${project.build.directory}")
  private File outputDirectory;

  @Parameter(property = "sourcesDirectory", required = true, defaultValue = "src/main/java")
  private File sourcesDirectory;

  @Parameter(property = "targetClassName", required = true, defaultValue = "Player")
  private String targetClassName;

  @Parameter(property = "showLineNumbers", required = true, defaultValue = "false")
  private boolean showLineNumbers;

  @Parameter(property = "removeComments", required = true, defaultValue = "false")
  private boolean removeComments;

  @Parameter(property = "removeEmptyLines", required = true, defaultValue = "true")
  private boolean removeEmptyLines;

  @Parameter(property = "trimAllLines", required = true, defaultValue = "false")
  private boolean trimAllLines;

  @Override
  public void execute() throws MojoExecutionException {
    JavaSourcePackager packager = new JavaSourcePackager(
        sourcesDirectory,
        outputDirectory,
        mainClass,
        targetClassName,
        appPackage,
        showLineNumbers,
        removeComments,
        removeEmptyLines,
        trimAllLines);

    try {
      packager.execute();
    } catch (Exception e) {
      throw new MojoExecutionException("Error during packaging: " + e.getMessage(), e);
    }
  }
}
