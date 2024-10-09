<p align="center">
  <img src="https://bisq.network/images/bisq-logo.svg"/>
</p>

# Bisq Mobile (Full/Light-node option)

This project aims to make Bisq Network accesible in Mobile Platforms following the philosofy of Bisq2 - to make it
easier for both, experienced and newcomers, to trade Bitcoin in a decentralized way.

Currently, this project is a playground to start a POC with the following goals:

- Focus on Android first
- Have a general look and feel native to Android and respectful of Bisq2
- Minimal usable functionality for the mobile world: focus on transacting in Bisq using Mobile and just that and its
  UX (e.g. add push notifications).
- Able to connect to Bisq Network **as a node** using clearnet (and soon after, with Tor)

For more info please refer to [Bisq Mobile Discussions](https://github.com/bisq-network/bisq2/discussions/2665)

## Main Goals on this POC

 - [ Y ] Validate CodenameOne to create Android builds from the same Java sourcecode.
 - ~~[ - ] Validate CodenameOne framework to create iOS builds from the same Java sourcecode.~~
 - [ - ] Validate LOCAL Android Builds
 - [ Y ] Validate LOCAL iOS builds
 - [ - ] Test including one of Bisq2 modules into an app

## Requirements to contribute and build

## Contribute

 - Java 11.0.24.fx-zulu
 - IntelliJ Idea (you can use other IDEs according to docs, but found that it only works with Intellij)
 - You can code and test in CodenameOne Java emulator (see Intellij run config "Run in Simulator")
 - Once you are happy and want to test in the actual platforms, use the run config 
   - **Android**: Local Builds -> Gradle Android Project
   - **iOS**: Local Builds -> XCode iOS Project
 - This will generate the corresponding updated project in the platform /target dir. If you have the IDE installed and ready it will also launch it.
 - Then build freom the IDE as you normally would.

### Android Build

 - Java 17.0.12.fx-zulu
 - Android Studio or Intellij with Android plugin
 - Android SDK and virtual or real device

**Note**: we have found that build would hang on Apple CPUs Macbook's. Linux System is recommended to build Android.

### iOS Build

 - Java 11.0.24.fx-zulu
 - Latest XCode installed, license agreement signed and updated
 - Cocoapods installed
 - At least one simulator installed

## App Designs

// TODO - link to doc folder with the selected initial design (figma?)



##########################################################################


= Codename One Project

This is a multi-module maven project for building a Codename One application. Codename One applications written in Java and/or Kotlin, and are built as native apps and can be built and deployed to iOS, Android, Mac, Windows, Linux, and also to the Web.

== Getting Started

=== Java

If you plan to use Java as your primary language, https://shannah.github.io/cn1-maven-archetypes/cn1app-archetype-tutorial/getting-started.html[start here].

=== Kotlin

If you plan to use Kotlin as your primary language, https://shannah.github.io/cn1app-archetype-kotlin-template/getting-started.html[start here].


== Eclipse Users

IMPORTANT: If you use Eclipse as your IDE, **read this first**

The _tools/eclipse_ directory contains eclipse ".launch" files that will add common Maven goals as menu items inside Eclipse.

**After importing this project into Eclipse, you should import the launch files.**

=== Additional Steps for CodeRAD projects

CodeRAD includes an annotation processor that needs to be activated. There are a few additional steps required to enable this in Eclipse.

. Add `org.eclipse.m2e.apt.mode=jdt_apt` to the `./common/.settings/org.eclipse.m2e.apt.prefs`
. Add `target/generated-sources/rad-views` to the .classpath.

See https://github.com/codenameone/CodenameOne/issues/3724[this issue] for more details.

== NetBeans Users

This project is a multi-module Maven project that was generated from a Maven archetype.

== IntelliJ Users

The project should work in IntelliJ out of the box.  No need to copy any files.

== Help and Support

See the https://www.codenameone.com[Codename One Web Site].