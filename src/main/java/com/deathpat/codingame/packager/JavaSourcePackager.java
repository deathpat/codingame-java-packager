package com.deathpat.codingame.packager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.igormaznitsa.jcp.removers.JavaCommentsRemover;

public class JavaSourcePackager {
  private File srcFolder;

  private String targetClass;

  private String targetClassName;

  private String appPackage;

  private File destinationFolder;

  private Set<String> allImports;

  private Set<String> allClassNames;

  private boolean removeEmptyLines = true;

  private boolean addLineNumber = true;

  private boolean removeComments = true;

  private boolean trimAllLines = true;

  public JavaSourcePackager(File srcFolder, File destinationFolder, String targetClass, String targetClassName,
      String appPackage, boolean addLineNumber, boolean removeComments, boolean removeEmptyLines,
      boolean trimAllLines) {
    this.srcFolder = srcFolder;
    this.destinationFolder = destinationFolder;
    this.targetClass = targetClass;
    this.targetClassName = targetClassName;
    this.appPackage = appPackage;
    allImports = new HashSet<String>();
    allClassNames = new HashSet<String>();
    this.addLineNumber = addLineNumber;
    this.removeComments = removeComments;
    this.removeEmptyLines = removeEmptyLines;
    this.trimAllLines = trimAllLines;
  }

  public void execute() throws IOException {
    allImports.clear();
    allClassNames.clear();
    if (!srcFolder.exists() || !srcFolder.isDirectory()) {
      throw new RuntimeException("Invalid src folder");
    }
    String targetClassFilePath = targetClass.replace(".", "/") + ".java";
    File targetSrcFile = new File(srcFolder, targetClassFilePath);
    if (!targetSrcFile.exists()) {
      throw new RuntimeException("Invalid target class");
    }
    if (!destinationFolder.exists()) {
      Files.createDirectories(destinationFolder.toPath());
    }
    File destinationFile = new File(destinationFolder, targetClassName + ".java");

    if (destinationFile.exists()) {
      destinationFile.delete();
    }
    String aggregatedClasses = aggregateClasses(targetSrcFile);
    String resultFileContent = processTargetFileContent(targetSrcFile, aggregatedClasses);

    System.out.println(resultFileContent);

    try (FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
      IOUtils.write(resultFileContent, outputStream, StandardCharsets.UTF_8);
    }
  }

  private String aggregateClasses(File excludedFile) throws IOException {
    StringBuilder res = new StringBuilder();
    Collection<File> allFiles = FileUtils.listFiles(srcFolder, null, true);
    for (File file : allFiles) {
      if (!file.equals(excludedFile)) {
        res.append("// ************************************\n");
        res.append("// ** " + file.getName() + "\n");
        res.append("// ************************************\n\n");
        res.append(processFileContent(file)).append("\n\n");
      }
    }
    return res.toString();
  }

  private String processFileContent(File file) throws IOException {
    String className = FilenameUtils.getBaseName(file.getName());
    if (allClassNames.contains(className)) {
      throw new RuntimeException("Duplicate classname in the project: " + className);
    }
    allClassNames.add(className);

    List<String> content = null;

    if (removeComments) {
      String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
      StringWriter contentRes = new StringWriter();
      JavaCommentsRemover commentRemover = new JavaCommentsRemover(new StringReader(fileContent), contentRes);
      commentRemover.process();
      fileContent = contentRes.toString();
      content = asList(StringUtils.split(fileContent, "\n"));
    } else {
      content = FileUtils.readLines(file, StandardCharsets.UTF_8);
    }

    int nbChars = (content.size() + "").length();
    int initialFileLineNumber = 1;
    for (int i = 0; i < content.size(); i++) {
      String curLine = content.get(i);
      boolean removeLine = false;
      if (curLine.startsWith("package")) {
        removeLine = true;
      } else if (curLine.startsWith("import")) {
        allImports.add(curLine);
        removeLine = true;
      } else if (curLine.trim().isEmpty()) {
        if (removeEmptyLines) {
          removeLine = true;
        } else {
          content.set(i, "");
        }
      } else if (curLine.contains(" class ")) {
        content.set(i, curLine.replace(" class ", " static class "));
      } else if (curLine.contains(" enum ")) {
        content.set(i, curLine.replace(" enum ", " static enum "));
      } else if (curLine.contains(" interface ")) {
        content.set(i, curLine.replace(" interface ", " static interface "));
      }
      if (removeLine) {
        content.remove(i);
        i--;
      } else {
        if (addLineNumber) {
          String lineNumber = StringUtils.leftPad("" + initialFileLineNumber, nbChars);
          content.set(i, content.get(i) + " // line " + lineNumber);
        }
        if (trimAllLines) {
          content.set(i, content.get(i).trim());
        }
      }
      initialFileLineNumber++;
    }

    return StringUtils.join(content.iterator(), "\n");
  }

  private List<String> asList(String[] stringArray) {
    List<String> res = new ArrayList<>();
    res.addAll(Arrays.asList(stringArray));
    return res;
  }

  private String processTargetFileContent(File targetSrcFile, String insideClasses) throws IOException {

    String className = FilenameUtils.getBaseName(targetSrcFile.getName());

    List<String> content = FileUtils.readLines(targetSrcFile, StandardCharsets.UTF_8);

    for (int i = 0; i < content.size(); i++) {
      String curLine = content.get(i);
      boolean removeLine = false;
      if (curLine.startsWith("package")) {
        removeLine = true;
      } else if (curLine.startsWith("import")) {
        allImports.add(curLine);
        removeLine = true;
      } else if (curLine.trim().isEmpty()) {
        removeLine = true;
      } else if (curLine.contains("class " + className)) {
        String newLine = curLine;
        if (newLine.startsWith("public ")) {
          newLine = newLine.replace("public ", "");
        }
        newLine = newLine.replace("class " + className, "class " + targetClassName);
        content.set(i, newLine + "\n" + insideClasses);
      }
      if (removeLine) {
        content.remove(i);
        i--;
      }
    }
    content.add(0, getCleanedImports() + "\n");
    return StringUtils.join(content.iterator(), "\n");
  }

  private String getCleanedImports() {
    List<String> res = new ArrayList<String>();
    for (String importStr : allImports) {
      if (!importStr.startsWith("import " + appPackage)) {
        res.add(importStr);
      }
    }
    return StringUtils.join(res, "\n");
  }

  public static void main(String[] args) throws IOException {
    JavaSourcePackager packager = new JavaSourcePackager(
        new File(args[0], args[1]),
        new File(args[0], args[2]),
        args[3],
        args[4],
        args[5],
        Boolean.parseBoolean(args[6]),
        Boolean.parseBoolean(args[7]),
        Boolean.parseBoolean(args[8]),
        Boolean.parseBoolean(args[9]));

    /*JavaSourcePackager packager = new JavaSourcePackager(
        new File("D:\\data\\coding\\git_clones\\fantastic-bits\\fantastic-bits\\src\\main\\java"),
        new File("D:\\data\\coding\\git_clones\\fantastic-bits\\fantastic-bits\\target"),
        "com.deathpat.codingame.fantasticbits.Main",
        "Player",
        "com.deathpat.codingame",
        false,
        true);*/

    packager.execute();
  }
}
