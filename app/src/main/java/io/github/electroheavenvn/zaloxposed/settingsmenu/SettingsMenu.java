package io.github.electroheavenvn.zaloxposed.settingsmenu;

import android.os.Build;

import org.luckypray.dexkit.DexKitBridge;

import java.util.ArrayList;
import java.util.List;

import io.github.electroheavenvn.zaloxposed.MorpheConstants;
import io.github.electroheavenvn.zaloxposed.hooks.custommenu.ListItemSettingHelper;
import io.github.electroheavenvn.zaloxposed.hooks.custommenu.TabMeSettingItemHelper;
import io.github.electroheavenvn.zaloxposed.hooks.custommenu.ZButtonHelper;
import io.github.electroheavenvn.zaloxposed.hooks.functional.ConfigImportExportHook;
import io.github.electroheavenvn.zaloxposed.utilities.Config;
import io.github.electroheavenvn.zaloxposed.utilities.DexKitCache;
import io.github.electroheavenvn.zaloxposed.utilities.Utils;
import io.github.electroheavenvn.zaloxposed.utilities.ZaloViewLauncher;

/*
Why do we need to move all setting items to a separate class?
Up until now, all setting items are constructed directly in the ZaloXposedSettingsMenuHook class, which makes the code messy and hard to maintain.
Furthermore, the ZaloXposedSettingsMenuHook class is currently depending on the feature flag: `features@tabme@fullzins@enable`, which can be removed anytime. Currently, this flag's value is set to true on most account and devices, but we force it to false for keeping the traditional settings menu.
If that happens, the traditional settings menu will be replaced with the "ZInstant" settings menu, which is not easy to add our custom setting items to. 
Therefore, we need to move all setting items to a separate class, so that when that happens, we can easily migrate all setting items to the new settings menu, either by using available views (and defacing them) or by creating one from scratch. 
For now, we will still use the traditional settings menu.
*/

/**
 * Stores all setting items for the settings menu, divided into categories and subcategories. 
 * <p>
 * Each category is represented by a TabMeSettingItem in the main settings menu, open a new page when clicked.
 * <p>
 * Inside the category page, each subcategory and its setting items are displayed.
 */
public final class SettingsMenu
{
    private SettingsMenu() { }

    static Class<?> settingView;
    static Class<?> buildInfoView;
    static Class<?> settingCallLogsView;

    private static final ArrayList<ISettingItem> settingItems = new ArrayList<>();

    static
    {
        settingItems.add(new SwitchSettingFeature("customBackgroundTitle", "", Config.KEY_ENABLE_BACKGROUND));

        settingItems.add(new SettingCategory("msgAndConverCategoryTitle", "msgAndConverCategoryDesc", List.of(
            new SettingSubCategory("chatHeadSubCategoryTitle", List.of(
                new SwitchSettingFeature("chatHeadTitle", "chatHeadDesc", Config.KEY_ENABLE_CHAT_HEAD).setEnabled(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
            )),
            new SettingSubCategory("disappearingMessagesSubCategoryTitle", List.of(
                new SwitchSettingFeature("ttlOverrideTitle", "ttlOverrideDesc", Config.KEY_ENABLE_TTL_OVERRIDE),
                new NumberSettingFeature("ttlTimeTitle", "ttlTimeDesc", Config.KEY_MSG_TTL)
            )),
            new SettingSubCategory("antiRecallDeleteSubCategoryTitle", List.of(
                new SwitchSettingFeature("antiRecallTitle", "", Config.KEY_ENABLE_ANTI_RECALL),
                new SwitchSettingFeature("antiDeleteTitle", "", Config.KEY_ENABLE_ANTI_DELETE),
                new SwitchSettingFeature("antiRecallIncludeMeTitle", "", Config.KEY_ANTI_RECALL_INCLUDE_ME),
                new SwitchSettingFeature("antiDeleteIncludeMyDeletionTitle", "", Config.KEY_ANTI_DELETE_INCLUDE_MY_DELETION)
            )),
            new SettingSubCategory("effectsAndContentSubCategoryTitle", List.of(
                new SwitchSettingFeature("hideFullscreenEffectsTitle", "hideFullscreenEffectsDesc", Config.KEY_HIDE_FULLSCREEN_EFFECTS),
                new SwitchSettingFeature("shareHiddenStickerPackTitle", "", Config.KEY_ENABLE_SHARE_HIDDEN_STICKER_PACK)
            ))
        )));
        settingItems.add(new SettingCategory("privacyCategoryTitle", "privacyCategoryDesc", List.of(
            new SettingSubCategory("privacyStatusControlSubCategoryTitle", List.of(
                new SwitchSettingFeature("silentTypingTitle", "", Config.KEY_ENABLE_SILENT_TYPING),
                new SwitchSettingFeature("blockSendSeenTitle", "", Config.KEY_BLOCK_SEND_SEEN)
            ))
        )));
        settingItems.add(new SettingCategory("adsAndTrackingCategoryTitle", "adsAndTrackingCategoryDesc", List.of(
            new SettingSubCategory("adsSubCategoryTitle", List.of(
                new SwitchSettingFeature("hideMediaBoxTitle", "", Config.KEY_HIDE_MEDIA_BOX),
                new SwitchSettingFeature("hideBizBoxTitle", "", Config.KEY_HIDE_BIZ_BOX),
                new SwitchSettingFeature("hideZInstantAdsTitle", "hideZInstantAdsDesc", Config.KEY_HIDE_Z_INSTANT_ADS),
                new SwitchSettingFeature("hideTimelineAdsTitle", "", Config.KEY_HIDE_FEED_ITEM_Z_INSTANT_ADS),
                new SwitchSettingFeature("blockAdsEndpointsTitle", "", Config.KEY_BLOCK_ADS_ENDPOINTS)
            )),
            new SettingSubCategory("logTrackSubCategoryTitle", List.of(
                new SwitchSettingFeature("disableFirebaseLoggingTitle", "", Config.KEY_DISABLE_FIREBASE_LOGGING),
                new SwitchSettingFeature("disableZaloTrackingTitle", "", Config.KEY_DISABLE_ZALO_TRACKING),
                new SwitchSettingFeature("blockQosEndpointsTitle", "", Config.KEY_BLOCK_ZALO_QOS)
            ))
        )));
        settingItems.add(new SettingCategory("uiCategoryTitle", "uiCategoryDesc", List.of(
            new SettingSubCategory("converMenuSubCategoryTitle", List.of(
                new SwitchSettingFeature("extendedGridMenuTitle", "", Config.KEY_ENABLE_EXTENDED_GRID_MENU),
                new SwitchSettingFeature("extendedGroupRightMenuTitle", "", Config.KEY_ENABLE_EXTENDED_GROUP_RIGHT_MENU),
                new SwitchSettingFeature("extendedGroupSettingMenuTitle", "", Config.KEY_ENABLE_EXTENDED_GROUP_SETTING_MENU)
            )),
            new SettingSubCategory("bottomRowSubCategoryTitle", List.of(
                new SwitchSettingFeature("customizeBottomRowTitle", "", Config.KEY_ENABLE_CUSTOMIZE_BOTTOM_ROW),
                new SwitchSettingFeature("hideDiscoveryTabTitle", "", Config.KEY_HIDE_DISCOVERY_TAB),
                new SwitchSettingFeature("hideNewsfeedTabTitle", "", Config.KEY_HIDE_NEWS_FEED_TAB),
                new SwitchSettingFeature("showGroupsTabTitle", "", Config.KEY_SHOW_GROUPS_TAB),
                new SwitchSettingFeature("showMoreTabTitle", "", Config.KEY_SHOW_MORE_TAB)
            ))
        )));
        settingItems.add(new SettingCategory("profileCategoryTitle", "profileCategoryDesc", List.of(
            new SettingSubCategory("profileSubCategoryTitle", List.of(
                new SwitchSettingFeature("restoreProfileMusicTitle", "restoreProfileMusicDesc", Config.KEY_RESTORE_PROFILE_MUSIC)
            ))
        )));
        settingItems.add(new SettingCategory("groupCategoryTitle", "groupCategoryDesc", List.of(
            new SettingSubCategory("groupRoleSubCategoryTitle", List.of(
                new SwitchSettingFeature("fakeGroupRoleTitle", "", Config.KEY_ENABLE_FAKE_GROUP_ROLE),
                new ListSelectionSettingFeature("fakeRoleTitle", "", Config.KEY_FAKE_GROUP_ROLE_LEVEL, List.of(
                    new ListSelectionSettingFeature.Selection("roleAdmin", 0),
                    new ListSelectionSettingFeature.Selection("roleOwner", 1)
                ))
            ))
        )));
        settingItems.add(new SettingCategory("advancedFeaturesCategoryTitle", "advancedFeaturesCategoryDesc", List.of(
            new SettingSubCategory("paidFeaturesSubCategoryTitle", List.of(
                new SwitchSettingFeature("unlockZCloudTitle", "unlockZCloudDesc", Config.KEY_UNLOCK_ZCLOUD),
                new SwitchSettingFeature("unlockZStyleTitle", "unlockZStyleDesc", Config.KEY_UNLOCK_ZSTYLE)
            ))
        )));
        settingItems.add(new SettingCategory("backupAndRestoreCategoryTitle", "backupAndRestoreCategoryDesc", List.of(
            new SettingSubCategory("backupMessagesSubCategoryTitle", List.of(
                new SwitchSettingFeature("restoreGoogleDriveBackupTitle", "restoreGoogleDriveBackupDesc", Config.KEY_RESTORE_GOOGLE_DRIVE_BACKUP)
            )),
            new SettingSubCategory("configBackupRestoreSubCategoryTitle", List.of(
                new OptionSettingFeature("exportConfigFileTitle", "exportConfigFileDesc", (item, context) ->
                    ConfigImportExportHook.ExportToDownloadFolder(context)),
                new OptionSettingFeature("importConfigFileTitle", "importConfigFileDesc", (item, context) ->
                    ConfigImportExportHook.ImportUsingFilePicker(context))
            ))
        )));
        settingItems.add(new Separator());
        settingItems.add(new SettingCategory("extendedSettingsCategoryTitle", "extendedSettingsCategoryDesc", List.of(
            new SettingSubCategory("", List.of(
                new OptionSettingFeature("openSettingViewTitle", "", ((item, context) ->
                    ZaloViewLauncher.Open(settingView, null))),
                new OptionSettingFeature("openBuildInfoViewTitle", "openBuildInfoViewDesc", ((item, context) ->
                    ZaloViewLauncher.Open(buildInfoView, null))),
                new OptionSettingFeature("openCallLogsViewTitle", "", ((item, context) ->
                    ZaloViewLauncher.Open(settingCallLogsView, null)))
            ))
        )));

        settingItems.add(new Button("reloadDexKitCacheBtnTitle", (item, context) -> {
            DexKitCache.ClearCache();
            Utils.RestartApp(context);
        }));
        settingItems.add(new Button("restartAppBtnTitle", (item, context) -> Utils.RestartApp(context)));
    }

    public static void Init(DexKitBridge bridge, ClassLoader classLoader) throws Exception
    {
        settingView = Class.forName("com.zing.zalo.ui.settings.SettingView", false, classLoader);
        buildInfoView = Class.forName("com.zing.zalo.ui.settings.devtool.BuildInfoView", false, classLoader);
        settingCallLogsView = Class.forName("com.zing.zalo.ui.call.SettingCallLogsView", false, classLoader);

        ListItemSettingHelper.Init(classLoader);
        ZButtonHelper.Init(classLoader);
        TabMeSettingItemHelper.Init(bridge, classLoader);
    }

    public static List<ISettingItem> getSettingItems() { return settingItems; }
}
