#!/bin/zsh
## Fleet in MacOS might not be able to create the running conf, this handy script allows to deploy on your connected emulator
# or device without having to open Intelij or Android Studio
./gradlew :androidClient:clean :androidClient:assembleDebug
./gradlew :androidClient:installDebug

exit 0