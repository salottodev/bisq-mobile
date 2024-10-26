#!/bin/bash

# Run from the project root
echo "Installing Cocoapods dependencies..."
cd iosClient || exit
pod install

echo "Building Kotlin/Native frameworks..."
cd .. || exit
./gradlew :shared:domain:assembleXCFramework
./gradlew :sharedUI:assembleXCFramework
# ./gradlew :shared:presenter:assembleXCFramework
# ./gradlew :shared:utilities:assembleXCFramework

echo "Opening Xcode workspace..."
open iosClient/iosClient.xcworkspace
