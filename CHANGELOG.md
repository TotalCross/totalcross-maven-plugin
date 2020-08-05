# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


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
