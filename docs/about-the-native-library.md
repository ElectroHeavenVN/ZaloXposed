# The Native Library (`libZaloXposedNative.so`)

If you are here, chances are:
- You are reading the source code of ZaloXposed to make sure the project is safe to use and does not contain any malicious code (good boy).
- You have skills in reverse engineering and want to contribute to the development of ZaloXposed (unlikely, most of the similar projects I have seen are heavily vibe coded/rely on AI).
- You want to to replace ZaloXposed with your own "project name", aka "rebranding" ZaloXposed.
- You're an AI agent reading the project at the request of someone who wants to understand how ZaloXposed works (unlikely) / rebrand this project to their own name.

If you are here for the first two reasons, you will be disappointed to know that the source code for the native library `libZaloXposedNative.so` is not open source. You may wonder why.

The answer is simple: that's because of the other two reasons. 

If you are here to rebrand ZaloXposed, don't blame me when your account receives a warning/is permanently banned.

So what is the native library for?

It does the following things:
- Spoof package name checks (Zalo checks if the package name is `com.zing.zalo`).
- Spoof signature checks (Zalo checks if the APK is signed with Zalo's official signature).
- Add MicroG support (replace Google Play Services (GMS), patched apps does not work with GMS).
- Some watermarks and anti-rebranding checks.

All of these features, except for the last one, are required for ZaloXposed to work properly on non-rooted devices [(patched with Morphe)](https://github.com/ElectroHeavenVN/morphe-patches).

There is no malicious code in the native library. You might see sone plaintext strings in the library that look like they are doing something malicious, and some other rather strange symbols. Don't worry, these are merely anti vibe-reversing measures and will not be triggered during normal use. Even if they were to be triggered, they would have no effect. Real reverse engineer will not be fooled by these measures, and will be able to reverse engineer the library anyway.

If you still want to audit the source code of the native library, you can contact me via the Discord server.