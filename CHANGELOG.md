# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.2.0] - 2020-08-05
### Added
- <totalcrossLib> param to configuration. By using this parameter, one can deploy a totalcross application that will take place inside the final jar, i.e., KnowCodeXML.jar has a KnowCodeXMLLib.tcz inside it.

- capability to load totalcross java libraries that has its tcz files inside it. Using the same example above, when someone adds a library that has its tcz inside it, the plugin is in charge to take this tcz and place inside the final application.

- parameter <externalResources>. This is a replacement for the file all.pkg. When listing the files inside this, these files will be added to the root of the final application package. Theses files wont be zipped inside the application tcz files, they are accessible inside the final package.
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
