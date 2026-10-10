# ZaloXposed
<!-- TODO: translate to Vietnamese -->

An **experimental** Xposed module for [Zalo](https://play.google.com/store/apps/details?id=com.zing.zalo) that provides various functionalities and enhancements.

> [!NOTE]
> **This project is in active development.** Some features may not work as expected. Please report any issues you encounter.

> [!WARNING]
> Using this module may **violate [Zalo's Terms of Service](https://zalo.vn/dieukhoan/).** Use at your own risk. The author is not responsible for any account bans or other consequences.

## Download
Visit the **[Latest](https://github.com/ElectroHeavenVN/ZaloXposed/releases/latest) / [Nightly](https://github.com/ElectroHeavenVN/ZaloXposed/releases/tag/nightly) Releases page** to download ZaloXposed.
<!-- TODO: register the repository to the Xposed Module Repository when a stable version is released -->

## Features

Most features can be toggled from the **ZaloXposed** entry in the **Me** tab. Some features require a restart to take effect.

Some features include:
- **Anti-Recall and Anti-Delete:** Prevents messages from being recalled or deleted.
- **Disable tracking, logging and analytics:** Blocks Zalo's tracking, logging, and analytics attempts.
- **Hide ads:** Removes various advertisements and promotional content from the app.
- **Hide typing indicators and "Seen" status:** Blocks sending typing and read receipts.
- **Restore hidden features:** Brings back removed features like profile music, Google Drive backup, etc.
- **UI/UX enhancements:** Custom backgrounds, extended menus, Mini Chat, and more.
- **Unlock premium features:** Unlocks certain premium features without payment. 

## How to use

1. Make sure you have a rooted Android device (permanent root access or temporary root via exploit, it's up to you).
2. Install an Xposed framework, preferably [Vector](https://github.com/JingMatrix/Vector).
3. Install the module APK and enable it, with the scope set to **Zalo** (`com.zing.zalo`).
4. Force-stop and reopen Zalo.
5. Open Zalo and find the **ZaloXposed** entry at the top of the **Me** tab.
6. Configure the settings as desired. Select **Restart app** button at the bottom of the settings page to apply changes that require a restart.

Settings are stored in the Zalo's app-specific external storage directory (Android/data/com.zing.zalo/). You can backup and restore the settings by copying the JSON configuration file.

## Credits

- [libxposed API](https://github.com/libxposed/api) — modern Xposed API.
- [DexKit](https://github.com/luckypray/DexKit) — runtime dex querying.
- [smali/dexlib2](https://github.com/google/smali) — dex disassembly.