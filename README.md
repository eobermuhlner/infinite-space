# infinite-space

A 3-D space game in an infinite universe.

All content (stars, planets, space stations, alien races, ...) is procedurally generated.

## Setup Eclipse Development

Prerequisites:
* Install Eclipse
* Install Android Studio
* Set environment variable ANDROID_HOME to the path of the Android SDK

In Eclipse or command line: 
* Clone the repository

In command line:
* Go to the working directory of the git repository
* `./gradlew`
* `./gradlew eclipse`

In Eclipse:
* Import projects from working directory of the git repository
* Add `assets` directory as source directory (if necessary)
* Locate class `DesktopLauncher` and execute context menu: `Run as...` / `Java Application`
