# Minify Maven Plugin

## 2.8.0-PATCHPUMP-R15

* Use zstd for both JS and CSS files 

## 2.8.0-PATCHPUMP-R14

* Add zstd option for leaving Zstandard compressed file

## 2.8.0-PATCHPUMP-R13

* Switch from UglfyJS and Nashorn to Terser and GraalVM

## 2.8.0-PATCHPUMP-R11

* Update Google Closure Compiler to v20240317

## 2.8.0-PATCHPUMP-R10

* Add plexus-utils dependency for Maven 3.9

## 2.8.0-PATCHPUMP-R7

* Add Nashorn 15.4 for JDK 17
* Update Google Closure Compiler to v20230502

## 2.8.0-PATCHPUMP-R6

* Update Google Closure Compiler to v20200517
* Update UglifyJS 3 to v3.9.4

## 2.8.0-PATCHPUMP-R5

* Fix gzip run before source file is closed

## 2.8.0-PATCHPUMP-R4

* Add UglifyJS 3
* Make CLOSURE the default JS engine
* Make ScriptEngine initialization JDK 10 compatible

## 2.8.0-PATCHPUMP-R2

* Add incrementalBuild property for source file change detection

## 2.8.0-PATCHPUMP-R1

* Make ECMASCRIPT5 the default closureLanguage
* Update Google Closure Compiler to v20181125
* Update yuicompressor to 2.4.8
* Update maven-plugin-annotations to allow for ASM 5+ and OpenJDK 10

## 2.8.0-PATCHPUMP-R0

* Fix relative path issue on linux
* Remove default paths, paths now relative to webapp dir
* Remove all deprecated options
* Update Google Closure Compiler to v20160208
* Update uglifyjs to 2.7.3
* Require Java 8

## 2.7.5-PATCHPUMP-R5

* Add closureWarningLevels option
* Update Google Closure Compiler to v20151015

## 2.7.5-PATCHPUMP-R4

* Add UGLIFY engine (terribly slow)
* Update Google Closure Compiler to v20150609

## 2.7.5-PATCHPUMP-R3

* Add cssSourceIncludeDir and jsSourceIncludeDir options
* Add gzip option for leaving zipped file

## 1.7.5

* Add support for external bundle configuration (#57).

## 1.7.4

* Update Google Closure Compiler to v20140814 (#71).
* Add support for Closure Library dependencies sorting (#70).
* Add option to use default externs provided with the Google Closure Compiler (#67).

## 1.7.3

* Improve docs: missing information about file order (#45).
* Select the JVM default charset as the value for the charset option when none is defined (#48).
* Add a warning message to nosuffix option Javadoc (#50).
* Add support for JavaScript Source Maps (#41).
* Update Google Closure Compiler to v20140625.
* Add support for Google Closure Compiler angular_pass option (#60).
* Rename yuiLinebreak option to yuiLineBreak.
* Rename yuiMunge option to yuiNoMunge.
* Rename yuiPreserveAllSemiColons option to yuiPreserveSemicolons.
* Fail build when a specified source file is not found (#53).

## 1.7.2

* Update default charset value to ${project.build.sourceEncoding}.
* Deprecate the option debug. verbose should be used instead.
* Change YUI option's names to clearly indicate that they are specific to YUI Compressor.
* Update Google Closure Compiler to v20130823.
* Add support for Google Closure Compiler language option (#24).
* Add support for Google Closure Compiler compilation_level option.
* Add support for Google Closure Compiler externs option (#22).
* Fail build with Google Closure Compiler on parse errors.

## 1.7.1

* Update Google Closure Compiler to v20130722.
* Preserve sub-directory structure when only minifying (#29).
* Delete transient .tmp file on spot in case of nosuffix = true (#32).
* Use annotations to generate the plugin descriptor file.

## 1.7

* Added nosuffix option to avoid the suffix .min on the minified output file name (#16).
* Option to use same subdirectory on target as in source (#17).
* Build should fail if compiler can't parse/compile source files (#19).
* Add UTF-8 as the default charset.
* Log compression gains.
* Require Java SE 7 for better resource management. See [AutoCloseable](http://docs.oracle.com/javase/7/docs/api/java/lang/AutoCloseable.html) interface and [try-with-resources](http://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) statements.

## 1.6

* Add support for [Google Closure Compiler](https://developers.google.com/closure/compiler/) for JavaScript compression (#14).

## 1.5.2

* New goal parameter to log full source file paths and new FAQ entry pointing to the plugin goal parameters (#5).
* Option to skip the minify step (#11).
* Option to skip the merge step (#13).

## 1.5.1

* Cannot process the same file name of files in different directories (#2).
* CSS minification fails for base64 encoded background images (#3).

## 1.5

* Fix charset issue (#1).
* Update Maven site skin.
* Use ExecutorService to wait for all tasks to finish.
* Add support for CLI-based configuration and Maven 2.2.1. From [Configuring Plugin Goals in Maven 3](http://www.sonatype.com/people/2011/03/configuring-plugin-goals-in-maven-3/):

  > For many plugin parameters it is occasionally convenient to specify their values from the command line via system properties. In the past, this was limited to parameters of simple types like String or Boolean. The latest Maven release finally allows plugin users to configure collections or arrays from the command line via comma-separated strings. Take for example a plugin parameter like this:
  >
  >     /** @parameter expression="${includes}" */
  >     String[] includes;
  >
  > This can be configured from the command line as follows:
  >
  >     mvn <goal> -Dincludes=Foo,Bar
  >
  > Plugin authors that wish to enable CLI-based configuration of arrays/collections just need to add the expression tag to their parameter annotation. Note that if compatibility with older Maven versions is to be kept, the parameter type must not be an interface but a concrete collection class or an array to avoid another shortcoming in the old configurator.

## 1.4

* Move from http://code.google.com/p/maven-samaxes-plugin/ to https://github.com/samaxes/minify-maven-plugin.
* Add Maven Integration for Eclipse (M2E) lifecycle mapping metadata.
* Rename project from Maven Minify Plugin to Minify Maven Plugin:

  > Artifact Ids of the format maven-___-plugin are reserved for  
  > plugins in the Group Id org.apache.maven.plugins  
  > Please change your artifactId to the format ___-maven-plugin  
  > In the future this error will break the build.

## 1.3.5

* Lift restriction that prevented the final file name to be the same as an existing source file name.

## 1.3.4

* Update YUI Compressor to version 2.4.6.

## 1.3.3

* Add debug messages for wrong source file names and source directory paths.

## 1.3.2

* Add cssTargetDir, jsTargetDir, suffix, and charset parameters.

## 1.3.1

* Class java.util.List cannot be instantiated while running Maven minify goal with versions previous to 3.0.

## 1.3

* Change exclude/include patterns from a comma separated String to List<String>. Also included a custom file comparator that only compares the file name instead of the full file path.
* Update [YUI Compressor](http://yui.github.com/yuicompressor/) dependency to version 2.4.2.

## 1.2.1

* Don't crash with an IndexOutOfBoundsException when a source file does not exist.
* More accurate logging.
* Configure POM to inherit from Sonatype OSS Parent POM.

## 1.2

* Add exclude/include patterns, with the caveat that the developer must name their source files so their lexicographical order is correct for minifying.
* Don't minify a file type if the list of files to process is empty.
* Make JavaScript minify error messages clearer.
* Make file extensions configurable (e.g. it's now possible to save a JavaScript file as *.jsp or *.php).
* Compile against JDK 1.5 instead of JDK 1.6.
