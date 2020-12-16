# Just Java Toolbox [![Build Status](https://travis-ci.org/justsocialapps/just-java-toolbox.svg?branch=master)](https://travis-ci.org/justsocialapps/just-java-toolbox)

common java tools and utilities missing in other tool libraries

# Usage

when you have uploaded artifacts in your repository (see [snapshot and releases](#snapshot-and-releases) if not yet done) you can use them in gradle by adding

    dependencies {
        implementation 'de.justsoftware.toolbox:just-java-toolbox:+'
        testImplementation 'de.justsoftware.toolbox:just-java-test-toolbox:+'
    }

consider replacing + by an explicit version number

# Creating and Uploading Artifacts

## local maven
Checkout a Commit and call

    ./gradlew clean install

## snapshot and releases
If not yet done, add to your ~/.gradle/gradle.properties

    jucoRepositorySnapshots=https://your.nexus.server/shapshot-repo
    jucoRepositoryThirdParty=https://your.nexus.server/thirdparty-repo
    repositoryUser=yourUserName
    repositoryPassword=yourPassword

Checkout a Commit and call

    ./gradlew clean uploadArchives

Beware, if you check out a commit which is an annotated tag, you will build and upload a release and not a snapshot (see [Release](#release))!

## Release
This project uses the jgitver gradle plugin. This creates the artifact version from annotated git tags.
To release you have to
- create an annotated tag on the master branch

      git fetch origin
      git checkout origin/master
      git tag -a X.Y.Z

  where X.Y.Z is the version number to be released
- push tags to https://github.com/justsocialapps/just-java-toolbox/tree/master (the master branch)

      git push --tags origin master

- build the artifacts and upload the artifacts

      ./gradlew clean uploadArchives

Edit the release informations at https://github.com/justsocialapps/just-java-toolbox/releases and upload the jars and source jars to it.

# License

This software is distributed under the MIT License, see [LICENSE](LICENSE) for more information.
