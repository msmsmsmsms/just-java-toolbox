# Just Java Toolbox [![Build Status](https://travis-ci.org/justsocialapps/just-java-toolbox.svg?branch=master)](https://travis-ci.org/justsocialapps/just-java-toolbox)

common java tools and utilities missing in other tool libraries

# Usage

when you have uploaded artifacts in your repository (see [Release](#release) if not yet done) you can use them in gradle by adding

    dependencies {
        compile 'de.justsoftware.toolbox:just-java-toolbox:+'
        testCompile 'de.justsoftware.toolbox:just-java-test-toolbox:+'
    }

consider replacing + by an explicit version number

# Creating and Uploading Artifacts
## local maven
Checkout a Commit and call

    ./gradlew clean install

## snapshot and releases
If not yet done, add to your ~/.gradle/gradle.properties

    jucoRepositorySnapshots=https://your.nexus.serverers/shapshot-repo
    jucoRepositoryThirdParty=https://your.nexus.serverers/thirdparty-repo
    repositoryUser=yourUserName
    repositoryPassword=yourPassword

Checkout a Commit and call

    ./gradlew clean uploadArchives

# Release

This project uses the jgitver gradle plugin. This creates the artifact version from git tags.
To release you have to
- create an annotated tag

      git tag -a X.Y.Z

  where X.Y.Z is the version number to be released
- push tags to https://github.com/justsocialapps/just-java-toolbox/tree/master (the master branch)

      git push --tags origin master

You can also do this by draft a release through https://github.com/justsocialapps/just-java-toolbox/releases

# License

This software is distributed under the MIT License, see [LICENSE](LICENSE) for more information.
