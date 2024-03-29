# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.3] - 2021-04-27

### Changed
- Bumps commons-io from 2.6 to 2.7. (@dependabot) 

### Fixed
- Java path on MacOS. (Path on Zulu JDK 11 changed since our last release)

## [2.0.2] - 2020-10-27

### Changed
- Update JDK version to 11 to fix UserAnonymousData bug.

## [2.0.1] - 2020-09-22

### Changed
- Retrolambda no longer uses system Java, now uses the JDK downloaded by the plugin.

## [2.0.0] - 2020-09-18

### Added
- parameter <jdkPath>: by using this parameter, when packaging, it will use the path passed here as the jdk to build.
    ```xml
    <configuration>
        <jdkPath>path/to/jdk</jdkPath>
    </configuration>
    ```

- Support for any Java version to use with TotalCross development, it's important to say we didn't add support to newer bytecode, this is just so the user can download any version of Java and use what we already support, this was done with embedding the latest [Zulu build of version 8](https://www.azul.com/downloads/zulu-community/?version=java-8-lts&architecture=x86-64-bit&package=jre) and using it when packaging. Using it will assume you agree to the [Terms of Use](https://www.azul.com/products/zulu-and-zulu-enterprise/zulu-terms-of-use/) from Azul, if you don't agree to it you can use your JAVA_HOME with the jdkPath parameter:
    ```xml
    <configuration>
        <jdkPath>${env.JAVA_HOME}</jdkPath>
    </configuration>
    ```

## [1.2.0] - 2020-08-05
### Added
- parameter <totalcrossLib>: by setting this parameter to true, one can package a totalcross library that will contain its tcz file inside its final jar, i.e., KnowCodeXML.jar has a KnowCodeXMLLib.tcz inside it.

- Capability of loading TotalCross Java libraries. Using the same example above, when someone adds a library that has its tcz inside it, that is, a TotalCross Java library, now the plugin is charge of copying the library tcz, i.e., `KnowCodeXMLLib.tcz`, and place it inside the final application package.

- parameter <externalResources>: this is a replacement for the file all.pkg. When listing the files inside this, these files will be added to the root of the final application package. Theses files wont be zipped inside the application tcz files, they are accessible inside the final package.
    ```xml
    <externalResources>
        <externalResource>my_exposed_file.png</<externalResource>
    </externalResources>    
    ```
### Changed
- Update retrolambda to version 2.5.7. 

### Removed
- Unecessary dependencies

## [1.1.2] - 2020-07-21
### Added
- Add user's project classpath and etc/libs of the target totalcross home to the classpath of the execution of the totalcross deploy process. 
### Changed
- Update retrolambda to version 2.5.6. 
