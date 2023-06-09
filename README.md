# Minify Maven Plugin

Minify Maven Plugin combines and minimizes your CSS and JavaScript files for faster page loading. It produces a merged and a minified version of your CSS and JavaScript resources which can be re-used across your project.

Under the hood, it uses the following compressors:

- [YUI Compressor] (https://yui.github.com/yuicompressor)
- [Google Closure Compiler] (https://github.com/google/closure-compiler)
- [UglifyJS 2] (https://github.com/mishoo/UglifyJS2) (terribly slow but procuces the smallest files)

## Benefits

### Reduce HTTP Requests

> 80% of the end-user response time is spent on the front-end. Most of this time is tied up in downloading all the components in the page: images, stylesheets, scripts, etc. Reducing the number of components in turn reduces the number of HTTP requests required to render the page. This is the key to faster pages.

> Combined files are a way to reduce the number of HTTP requests by combining all scripts into a single script, and similarly combining all CSS into a single stylesheet. Combining files is more challenging when the scripts and stylesheets vary from page to page, but making this part of your release process improves response times.

### Compress JavaScript and CSS

> Minification/compression is the practice of removing unnecessary characters from code to reduce its size thereby improving load times. A JavaScript compressor, in addition to removing comments and white-spaces, obfuscates local variables using the smallest possible variable name. This improves response time performance because the size of the downloaded file is reduced.

## Usage

Configure your project's `pom.xml` to run the plugin during the project's build cycle.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>patchpump.minify</groupId>
      <artifactId>minify-maven-plugin</artifactId>
      <version>2.8.0-PATCHPUMP-10</version>
      <executions>
        <execution>
          <id>default-minify</id>
          <configuration>
            <charset>UTF-8</charset>
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
  
Since version 2.8.0-PATCHPUMP-R7, Minify Maven Plugin requires JDK 17 to run.

## License

This distribution is licensed under the terms of the Apache License, Version 2.0.
