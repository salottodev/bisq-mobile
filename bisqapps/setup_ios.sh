#!/bin/bash

# Run from the project root
echo "Installing Cocoapods dependencies..."
cd iosClient || exit
rm -rf Pods
pod install

# the following is not striclty necessary and often errors
echo "Building Kotlin/Native frameworks..."
cd .. || exit
./gradlew :shared:domain:assembleXCFramework
./gradlew :shared:presentation:assembleXCFramework
# ./gradlew :shared:utilities:assembleXCFramework

echo "Opening Xcode workspace..."
open iosClient/iosClient.xcworkspace
