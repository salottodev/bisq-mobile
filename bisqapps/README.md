<p align="center">
  <img src="https://bisq.network/images/bisq-logo.svg"/>
</p>

# Bisq Mobile

This project aims to make Bisq Network accesible in Mobile Platforms following the philosofy of Bisq2 - to make it
easier for both, experienced and newcomers, to trade Bitcoin in a decentralized way.

# TODO

## How to contribute

### Project dev requirements

 - Java: 17.0.12.fx-zulu JDK (sdkman env file is avail in project root)
 - Ruby: v3+ (for iOS Cocoapods 1.15+)
 - IDE: We use and recommend Fleet, but you may as well use the IDE of your preference. For iOS testing you will need XCode.

### Set Env known issues

 - Some Apple M chips have trouble with cocoapods, follow [this guide](https://stackoverflow.com/questions/64901180/how-to-run-cocoapods-on-apple-silicon-m1/66556339#66556339) to fix it
 -

# TODO

## Why KMP

- Native Performance
- Allows us to focus on the "easiest" platform first (Because of Apple restrictions on Tor and networking in general)
- Flexibility without the security/privacy concerns of its competitors
- JVM language allows us to port much of the optimised Bisq code already existing in the Desktop apps

For a reference to the sample code used to kickstart this POC, see below