package io.github.electroheavenvn.zaloxposed.utilities;

import java.util.HashMap;
import java.util.Map;

public final class S
{
    private S() { }

    private static final Map<String, String[]> trans = new HashMap<>();

    static
    {
        // trans.put("conversationAppearanceSubCategoryTitle", new String[] { "Conversation appearance", "Hình thức trò chuyện" });
        trans.put("customBackgroundTitle", new String[] { "Custom background", "Hình nền tuỳ chỉnh" });
        //TODO allow choosing custom background image/folder

        // Master menu
        trans.put("msgAndConverCategoryTitle", new String[] { "Messages & Conversations", "Tin nhắn & Trò chuyện" });
        trans.put("msgAndConverCategoryDesc", new String[] { "Customize message appearance and behavior", "Tùy chỉnh hiển thị và hành vi tin nhắn" });
        trans.put("privacyCategoryTitle", new String[] { "Privacy", "Quyền riêng tư" });
        trans.put("privacyCategoryDesc", new String[] { "Control activity status and messages", "Kiểm soát trạng thái hoạt động và tin nhắn" });
        trans.put("adsAndTrackingCategoryTitle", new String[] { "Ads & tracking", "Quảng cáo & theo dõi" });
        trans.put("adsAndTrackingCategoryDesc", new String[] { "Hide ads and limit tracking", "Ẩn quảng cáo và hạn chế theo dõi" });
        trans.put("uiCategoryTitle", new String[] { "User interface", "Giao diện" });
        trans.put("uiCategoryDesc", new String[] { "Customize user interface", "Tùy giao diện ứng dụng" });
        trans.put("profileCategoryTitle", new String[] { "Profile", "Hồ sơ" });
        trans.put("profileCategoryDesc", new String[] { "Customize profile settings", "Tùy chỉnh cài đặt hồ sơ" });
        trans.put("groupCategoryTitle", new String[] { "Groups", "Nhóm" });
        trans.put("groupCategoryDesc", new String[] { "Customize group features", "Tùy chỉnh các tính năng liên quan đến nhóm" });
        trans.put("advancedFeaturesCategoryTitle", new String[] { "Advanced features", "Tính năng nâng cao" });
        trans.put("advancedFeaturesCategoryDesc", new String[] { "Advanced features and options", "Các tính năng và tuỳ chọn nâng cao" });
        trans.put("backupAndRestoreCategoryTitle", new String[] { "Backup & restore", "Sao lưu & khôi phục" });
        trans.put("backupAndRestoreCategoryDesc", new String[] { "Manage backups and configuration", "Quản lý dữ liệu sao lưu và cấu hình" });
        // Buttons
        trans.put("reloadDexKitCacheBtnTitle", new String[] { "Force reload DexKit cache", "Buộc tải lại bộ nhớ đệm DexKit" });
        trans.put("restartAppBtnTitle", new String[] { "Restart app", "Khởi động lại ứng dụng" });

        //Messages & Conversations
        trans.put("chatHeadSubCategoryTitle", new String[] { "Mini Chat (Chat Head)" });
        trans.put("chatHeadTitle", new String[] { "Replace Android Bubble conversations with Mini Chat", "Thay thế bong bóng hội thoại Android bằng Mini Chat" });
        trans.put("chatHeadDesc", new String[] { "Requires Android 11 or higher", "Yêu cầu Android 11 trở lên" });

        trans.put("disappearingMessagesSubCategoryTitle", new String[] { "Disappearing messages", "Tin nhắn tự xoá" });
        trans.put("ttlOverrideTitle", new String[] { "Override disappearing messages config", "Ghi đè cấu hình tin nhắn tự xoá" });
        trans.put("ttlOverrideDesc", new String[] { "Only works for threads that have disappearing messages enabled", "Chỉ hoạt động với các cuộc trò chuyện đã bật tin nhắn tự xoá" });
        trans.put("ttlTimeTitle", new String[] { "Disappearing message time", "Thời gian tin nhắn tự xoá" });
        trans.put("ttlTimeDesc", new String[] { "Enter value (milliseconds)", "Nhập giá trị (mili giây)" });

        trans.put("antiRecallDeleteSubCategoryTitle", new String[] { "Recalled and deleted messages", "Thu hồi và xoá tin nhắn" });
        trans.put("antiRecallTitle", new String[] { "Anti-Recall", "Chống thu hồi tin nhắn" });
        trans.put("antiDeleteTitle", new String[] { "Anti-Delete", "Chống xoá tin nhắn" });
        trans.put("antiRecallIncludeMeTitle", new String[] { "Include my recalled messages", "Bao gồm tin nhắn tôi đã thu hồi" });
        trans.put("antiDeleteIncludeMyDeletionTitle", new String[] { "Include messages deleted by me", "Bao gồm tin nhắn được tôi xoá" });

        trans.put("effectsAndContentSubCategoryTitle", new String[] { "Effects & content", "Hiệu ứng & nội dung" });
        trans.put("hideFullscreenEffectsTitle", new String[] { "Hide fullscreen effects", "Ẩn hiệu ứng toàn màn hình" });
        trans.put("hideFullscreenEffectsDesc", new String[] { "Hide fullscreen effects when certain messages are received", "Ẩn hiệu ứng toàn màn hình khi nhận được một số tin nhắn nhất định" });
        trans.put("shareHiddenStickerPackTitle", new String[] { "Allow sharing hidden sticker packs", "Cho phép chia sẻ bộ sticker ẩn" });

        trans.put("mediaQualitySubCategoryTitle", new String[] { "Media quality", "Chất lượng nội dung đa phương tiện" });
        trans.put("defaultOriginalQualityTitle", new String[] { "Default to Original quality when selecting images from gallery", "Mặc định chọn chất lượng Original khi chọn ảnh từ thư viện" });
        trans.put("defaultOriginalQualityDesc", new String[] { "Requires the \"Unlock ZCloud\" feature to work", "Yêu cầu tính năng \"Mở khoá ZCloud\" được bật" });
        trans.put("defaultOriginalQualityForPastedImagesTitle", new String[] { "Send pasted images in Original quality", "Gửi ảnh được dán ở chất lượng Original" });
        trans.put("defaultOriginalQualityForPastedImagesDesc", new String[] { "Requires the \"Unlock ZCloud\" feature to work", "Yêu cầu tính năng \"Mở khoá ZCloud\" được bật" });
        trans.put("defaultOriginalQualityShareTitle", new String[] { "Send shared images in Original quality", "Gửi ảnh được chia sẻ ở chất lượng Original" });
        trans.put("defaultOriginalQualityShareDesc", new String[] { "Requires the \"Unlock ZCloud\" feature to work, does not work when forwarding photos", "Yêu cầu tính năng \"Mở khoá ZCloud\" được bật, không hoạt động khi chuyển tiếp ảnh" });
        trans.put("defaultOriginalQualityCameraTitle", new String[] { "Send camera images in Original quality", "Gửi ảnh chụp từ camera ở chất lượng Original" });
        trans.put("defaultOriginalQualityCameraDesc", new String[] { "Requires the \"Unlock ZCloud\" feature to work", "Yêu cầu tính năng \"Mở khoá ZCloud\" được bật" });

        // Privacy
        trans.put("privacyStatusControlSubCategoryTitle", new String[] { "Control activity status and messages", "Kiểm soát trạng thái hoạt động và tin nhắn" });
        trans.put("silentTypingTitle", new String[] { "Hide typing indicators", "Ẩn trạng thái đang nhập" });
        trans.put("blockSendSeenTitle", new String[] { "Hide \"Seen\" status", "Ẩn trạng thái Đã xem" });

        // Ads and tracking
        trans.put("adsSubCategoryTitle", new String[] { "Ads", "Quảng cáo" });
        trans.put("hideMediaBoxTitle", new String[] { "Hide Media Box", "Ẩn Media Box" });
        trans.put("hideBizBoxTitle", new String[] { "Hide Business Box", "Ẩn Business Box" });
        trans.put("hideZInstantAdsTitle", new String[] { "Hide ZInstant Ads", "Ẩn quảng cáo ZInstant" });
        trans.put("hideZInstantAdsDesc", new String[] { "Hide ads in Messages tab", "Ẩn quảng cáo trong tab Tin nhắn" });
        trans.put("hideTimelineAdsTitle", new String[] { "Hide Ads in Timeline", "Ẩn quảng cáo trong Nhật Ký" });
        trans.put("blockAdsEndpointsTitle", new String[] { "Block ads network endpoints", "Chặn yêu cầu mạng quảng cáo" });

        trans.put("logTrackSubCategoryTitle", new String[] { "Logging and tracking", "Ghi nhật ký và theo dõi" });
        trans.put("disableFirebaseLoggingTitle", new String[] { "Disable Firebase logging", "Tắt ghi nhật ký Firebase" });
        trans.put("disableZaloTrackingTitle", new String[] { "Disable Zalo tracking", "Tắt tính năng theo dõi của Zalo" });
        trans.put("blockQosEndpointsTitle", new String[] { "Block QoS network endpoints", "Chặn yêu cầu mạng QoS" });

        // User interface
        trans.put("converMenuSubCategoryTitle", new String[] { "Conversation menu", "Menu trò chuyện" });
        trans.put("extendedGridMenuTitle", new String[] { "Enable extended grid menu", "Kích hoạt chat menu mở rộng" });
        trans.put("extendedGroupRightMenuTitle", new String[] { "Enable extended group right menu", "Kích hoạt menu nhóm mở rộng" });
        trans.put("extendedGroupSettingMenuTitle", new String[] { "Enable extended group settings menu", "Kích hoạt menu cài đặt nhóm mở rộng" });

        trans.put("bottomRowSubCategoryTitle", new String[] { "Bottom row", "Thanh dưới" });
        trans.put("customizeBottomRowTitle", new String[] { "Enable bottom row customization", "Kích hoạt tuỳ chỉnh thanh dưới" });
        trans.put("hideDiscoveryTabTitle", new String[] { "Hide Discovery tab", "Ẩn tab Khám Phá" });
        trans.put("hideNewsfeedTabTitle", new String[] { "Hide Newsfeed tab", "Ẩn tab Tường Nhà" });
        trans.put("showGroupsTabTitle", new String[] { "Show Groups tab", "Hiện tab Nhóm" });
        trans.put("showMoreTabTitle", new String[] { "Show More tab", "Hiện tab Thêm" });

        // Profile
        trans.put("profileSubCategoryTitle", new String[] { "Profile", "Hồ sơ" });
        trans.put("restoreProfileMusicTitle", new String[] { "Restore profile music options", "Phục hồi tuỳ chọn nhạc nền hồ sơ" });
        trans.put("restoreProfileMusicDesc", new String[] { "Replace the ZStyle profile music options", "Thay thế tuỳ chọn chọn nhạc nền hồ sơ bằng ZStyle" });

        // Group
        trans.put("groupRoleSubCategoryTitle", new String[] { "Group roles", "Vai trò trong nhóm" });
        trans.put("fakeGroupRoleTitle", new String[] { "Fake my role in groups", "Giả mạo vai trò trong nhóm" });
        trans.put("fakeRoleTitle", new String[] { "Fake role", "Vai trò giả mạo" });

        // Advanced features
        trans.put("paidFeaturesSubCategoryTitle", new String[] { "Paid features", "Chức năng trả phí" });
        trans.put("unlockZCloudTitle", new String[] { "Unlock ZCloud", "Mở khoá ZCloud" });
        trans.put("unlockZCloudDesc", new String[] { "Does not increase My Documents capacity", "Không tăng dung lượng bộ nhớ My Documents" });
        trans.put("unlockZStyleTitle", new String[] { "Unlock ZStyle", "Mở khoá ZStyle" });
        trans.put("unlockZStyleDesc", new String[] { "Does not unlock the profile customization feature exclusive to ZStyle", "Không mở khóa tính năng tùy chỉnh hồ sơ dành riêng cho ZStyle" });

        // Backup and restore
        trans.put("backupMessagesSubCategoryTitle", new String[] { "Backup messages", "Sao lưu tin nhắn" });
        trans.put("restoreGoogleDriveBackupTitle", new String[] { "Restore Google Drive backup options", "Phục hồi tuỳ chọn sao lưu ảnh bằng Google Drive" });
        trans.put("restoreGoogleDriveBackupDesc", new String[] { "Replace the ZCloud backup options", "Thay thế tuỳ chọn sao lưu bằng ZCloud" });
        //TODO: support MicroG

        trans.put("configBackupRestoreSubCategoryTitle", new String[] { "Configuration backup & restore", "Sao lưu & Khôi phục cấu hình" });
        trans.put("exportConfigFileTitle", new String[] { "Export configuration file", "Xuất tệp cấu hình" });
        trans.put("exportConfigFileDesc", new String[] { "Save to Downloads/ZaloXposed", "Lưu vào thư mục Downloads/ZaloXposed" });
        trans.put("importConfigFileTitle", new String[] { "Import configuration from file", "Nhập cấu hình từ tệp" });
        trans.put("importConfigFileDesc", new String[] { "Choose a JSON file", "Chọn tệp JSON từ bộ nhớ" });
        trans.put("resetConfigTitle", new String[] { "Reset ZaloXposed configuration", "Đặt lại cấu hình ZaloXposed" });

        // Extended settings
        trans.put("extendedSettingsCategoryTitle", new String[] { "Extended Settings", "Cài đặt mở rộng" });
        trans.put("extendedSettingsCategoryDesc", new String[] { "Advanced/hidden settings inside Zalo", "Các cài đặt nâng cao/ẩn bên trong Zalo" });
        trans.put("openSettingViewTitle", new String[] { "Open old Settings view", "Mở giao diện cài đặt cũ" });
        trans.put("openBuildInfoViewTitle", new String[] { "Open Build Info view", "Mở giao diện thông tin phiên bản" });
        trans.put("openBuildInfoViewDesc" , new String[] { "Can be opened by tapping the Zalo version 10 times in Settings > About Zalo", "Có thể mở bằng cách nhấn 10 lần vào phiên bản Zalo trong Cài đặt > Giới thiệu về Zalo" });
        trans.put("openCallLogsViewTitle", new String[] { "Open Call Logs view", "Mở giao diện \"Danh sách log\" cuộc gọi" });


        // Toasts & Messages
        trans.put("exportSuccess", new String[] { "Exported config file to: ", "Đã xuất tệp cấu hình ra: " });
        trans.put("exportSuccessFormat", new String[] { "Exported config file to: %s", "Đã xuất tệp cấu hình ra: %s" });
        trans.put("exportFailed", new String[] { "Export failed (permission denied)!", "Xuất cấu hình thất bại (không có quyền bộ nhớ)!" });
        trans.put("openFilePickerFailed", new String[] { "Cannot find activity to open file picker!", "Không tìm thấy Activity để mở trình chọn file!" });
        trans.put("importSuccess", new String[] { "Configuration imported successfully, restart app to apply.", "Đã nhập cấu hình, khởi động lại app để áp dụng." });
        trans.put("importFailed", new String[] { "Failed to read or parse configuration file!", "Không thể đọc tệp hoặc định dạng JSON không hợp lệ!" });
        trans.put("importError", new String[] { "Error importing: ", "Lỗi khi nhập tệp: " });
        trans.put("importErrorFormatted", new String[] { "Error importing: %s", "Lỗi khi nhập tệp: %s" });

        // Dynamic / headers
        trans.put("moduleSettingsDesc", new String[] { "%s settings", "Cài đặt %s" });
        trans.put("moduleSettingsHeaderTitle", new String[] { "%s Settings", "Cài đặt %s" });

        trans.put("roleAdmin", new String[] { "Admin", "Phó nhóm" });
        trans.put("roleOwner", new String[] { "Owner", "Trưởng nhóm" });
    }

    public static String tr(String key)
    {
        if (key == null || key.isEmpty())
            return "";
        String[] values = trans.get(key);
        if (values == null || values.length == 0)
        {
            Logger.w("Missing string values for key " + key);
            return key;
        }
        if (values.length == 1)
        {
            Logger.i("Using default value for key " + key);
            return values[0];
        }
        return values[Utils.IsEnglish() ? 0 : 1];
    }

    public static String tr(String key, Object... args)
    {
        String str = tr(key);
        if (args == null || args.length == 0)
            return str;
        try
        {
            return String.format(str, args);
        }
        catch (Exception ignored) { }
        return str;
    }
}