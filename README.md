# Quest Audio Patcher
This application can patch Android Apps to add the neccessary permissions to play audio in the background on Oculus Quest Devices.

> [!WARNING]  
> This is an experimental piece of software. Not all apps can be patched correctly, some may have issues even if they are, and your success my vary based on the target app's version.

## Installation
Download and install the latest version from [Releases](https://github.com/threethan/QuestAudioPatcher/releases). This app may come to sidequest in the near future.

**Quest 1** - ❌ Patcher works, but system kills background apps

**Quest 2** - ✔️ Tested working

**Quest 3** - ❔ Needs testing

## Usage
0. Install the app you would like to patch onto your Quest
1. Select the app you wish to modify from the "Apps" list
2. Wait for the app to be automatically be patched. This has a few steps and may take a while for larger apps.
3. Uninstall the original app when prompted. WARNING: That app\'s current data may be permanently lost!
4. If you are asked to let the patcher install unknown apps, go to settings and allow it. Then press back to return.
5. Install the modified application as prompted!

Use common sense and only patch applications which would benefit from background audio.
   
## Warning
**Many apps have issues running natively on the Quest. This patcher will not fix them.**
#### Here are a few relevant apps I have tested:
- **Spotify** - Cannot get past the login screen and may fail to install. Use the web app via [LightningLauncher](https://github.com/threethan/LightningLauncher/releases/) instead, or install and patch Spotify Lite (https://apkpure.com/spotify-lite/com.spotify.lite)
- **Discord** - Many UI elements are incorrectly sized and chat gets cut off on the right side of the screen. Fully resolved by using an [pre-137 version of Discord](https://www.apkmirror.com/apk/discord-inc/discord-chat-for-gamers/discord-chat-for-gamers-126-21-stable-release/discord-talk-chat-hang-out-126-21-stable-android-apk-download/download/?key=524e8c97e18586f13183d87e42aaa18914bcbb38).
- **Tidal** - Works flawlessly, including lossless playback!
- Any app with which attempts to detect modification will most likely not work. 

*These issues are not specific to the patcher, and this is by no means a comprehensive list*

---


*This project is heavily based on [Apk Explorer & Editor](https://github.com/apk-editor/APK-Explorer-Editor). Code from AEE is used under GPLv3.*
