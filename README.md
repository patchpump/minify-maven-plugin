# Minify Maven Plugin

Minify Maven Plugin combines and minimizes CSS and JavaScript files.

Under the hood, it uses the following compressors:

- [YUI Compressor] (https://yui.github.com/yuicompressor)
- [Google Closure Compiler] (https://github.com/google/closure-compiler)
- [terser] (https://github.com/terser/terser)

## Usage

Configure your project's `pom.xml` to run the plugin during the project's build cycle.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>patchpump.minify</groupId>
      <artifactId>minify-maven-plugin</artifactId>
      <version>2.8.0-PATCHPUMP-16</version>
      <executions>
        <execution>
          <id>default-minify</id>
          <configuration>
            <charset>UTF-8</charset>
            <verbose>false</verbose>
            <gzip>true</gzip>
            <zstd>12</zstd>
            <zstdDirectoryDir>dict</zstdDirectoryDir>
            <cssSourceFiles>
              <cssSourceFile>file-1.css</cssSourceFile>
              <!-- ... -->
              <cssSourceFile>file-n.css</cssSourceFile>
            </cssSourceFiles>
            <jsSourceFiles>
              <jsSourceFile>file-1.js</jsSourceFile>
              <!-- ... -->
              <jsSourceFile>file-n.js</jsSourceFile>
            </jsSourceFiles>
            <jsEngine>CLOSURE</jsEngine>
            <closureLanguage>UNSTABLE</closureLanguage>
            <closureWarningLevels>
                <misplacedTypeAnnotation>OFF</misplacedTypeAnnotation>
                <nonStandardJsDocs>OFF</nonStandardJsDocs>
            </closureWarningLevels>
          </configuration>
          <goals>
            <goal>minify</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

For more information, check https://github.com/patchpump/minify-maven-plugin

## System Requirements
  
JDK 17

## Enable JVMCI (experimental feature)

MAVEN_OPTS=-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -Dpolyglot.engine.WarnInterpreterOnly=false

## License

The source code of this distribution is licensed under the terms of the Apache License, Version 2.0.
 