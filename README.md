# codingame-java-packager
A maven plugin to package multiple java files into a single class to be uploaded to http://www.codingame.com

## Prerequisite

As this is a maven plugin, this requires your game project to be a maven project.

## Usage

- Clone this repo on your computer and mvn install it, so that the maven plugin is installed in your local maven repository

```
mvn install
```

- In the maven project for your game, add the following plugin:

```
<build>
    ...
    <plugins>
        <plugin>
        <groupId>com.deathpat</groupId>
        <artifactId>codingame-java-packager</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
            <execution>
            <goals>
                <goal>package</goal>
            </goals>
            </execution>
        </executions>
        <configuration>
            <appPackage>com.your.game.package</appPackage>
            <mainClass>com.your.game.package.Main</mainClass>
        </configuration>
        </plugin>
    </plugins>
    ...
</build>
```

- When compiling your project, the plugin will take all the sources in the specified package and pack them into the speficied main class. The resulting java file is copied into target/Player.java in your project.

- (Optional) If you are using eclipse as IDE you can tell it to automatically run the plugin when doing incremental builds, so that when you save a file it will be triggered. To do so add the following in your pom.xml:

```
<build>
    ...
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.eclipse.m2e</groupId>
                <artifactId>lifecycle-mapping</artifactId>
                <version>1.0.0</version>
                <configuration>
                    <lifecycleMappingMetadata>
                        <pluginExecutions>
                            <pluginExecution>
                                <pluginExecutionFilter>
                                    <groupId>com.deathpat</groupId>
                                    <artifactId>codingame-java-packager</artifactId>
                                    <versionRange>1.0-SNAPSHOT</versionRange>
                                    <goals>
                                        <goal>package</goal>
                                    </goals>
                                </pluginExecutionFilter>
                                <action>
                                    <execute>
                                        <runOnIncremental>true</runOnIncremental>
                                    </execute>
                                </action>
                            </pluginExecution>
                        </pluginExecutions>
                    </lifecycleMappingMetadata>
                </configuration>
            </plugin>
        </plugins>
    </pluginManagement>
    ...
</build>
```

- (Optional) You can also install in chrome the coding game extension/app to have  full chain that generates + upload your code each time you save a file. More details here: https://www.codingame.com/forum/t/codingame-sync-beta/614

## Plugin parameters

Here the list of all the parameters of the plugin:

```
<configuration>
    <appPackage>com.your.game.package</appPackage>
    <mainClass>com.your.game.package.Main</mainClass>
    <outputDirectory>${project.build.directory}</outputDirectory>
    <sourcesDirectory>src/main/java</sourcesDirectory>
    <targetClassName>Player</targetClassName>
    <showLineNumbers>false</showLineNumbers>
    <removeComments>false</removeComments>
    <removeEmptyLines>true</removeEmptyLines>
    <sourceFolders>
        <sourceFolder>../lib/src/main/java</sourceFolder>
    </sourceFolders>
</configuration>
```

- **appPackage** (*mandatory*): the package where your code is. It is important that you put your game code in a package (not the default one) and specify it here as this is used to remove the import statements in the output file.

- **mainClass** (*mandatory*): the main class of the game. The main entry point of the game, all the other files will be copied at the begining of this class (as static classes). The name of this class is not important, it will be renamed by the plugin.

- **outputDirectory**: the folder where the resulting java class file is copied. defaults to ${project.build.directory} (target)

- **sourcesDirectory**: the folder where to find the sources. This defaults to src/main/java

- **targetClassName**: the main of the generated class. Defaults to Player as this is what expects codingame.

- **showLineNumbers**: if true, appends the line numbers of the original files to all the lines of the generated file. Can be useful for debuging purposes. Defaults to false.

- **removeComments**: if true, removes all the comments from the source files before packaging. The purpose is to reduce the generated file size. Defaults to false. When this is enabled, the line numbers shown with the option **showLineNumbers** are no longer reliable.

- **removeEmptyLines**: if true, removes all empty lines from the source code before packaging. The purpose is to reduce the generated file size. Defaults to true.

- **trimAllLines**: if true, trim all the lines of the source code before packaging. The purpose is to reduce the generated file size. Defaults to false.

- **sourceFolders**: a list of extra source folders to package with the main sources. Useful to share some code between projects.

## Limitations
- All the classes of your project should have a unique name. As in the end they are all aggregated in the same file it would cause name collision issues. The plugin will generate an error if this happens.

- In your project source files you should not have inner classes (anonymous ones are fine). The parsing of the classes is very basic and having a inner class defined would make it sad. This could be fixed in the future I guess.

- The maximum file size accepted by codingame is 100 Kb. Using multiple files like in a "normal" project can lead rapidly to something close to this number. You can play with **removeComments**, **removeEmptyLines** and **trimAllLines** parameters to reduce the generated file size ... at the expense of the readability of the code in the codingame IDE.