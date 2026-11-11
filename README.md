<div align="center"> 

# ZaloXposed
[**English version**](./README_EN.md)

</div>


Module Xposed cung cấp nhiều chức năng và cải tiến khác nhau cho [Zalo](https://play.google.com/store/apps/details?id=com.zing.zalo).

Tham gia **[Server Discord](https://discord.gg/33cqtZU7Ux)** của chúng tôi để thảo luận về ZaloXposed, chia sẻ ý tưởng, trao đổi kiến ​​thức và kết nối với các thành viên khác trong cộng đồng!

<div align="center"> 

[![Server Discord (lời mời với vai trò liên kết)](https://discord.com/api/guilds/1115634791321190420/widget.png?style=banner3)](https://discord.gg/33cqtZU7Ux)

</div>

---

<br>

> [!NOTE]
> **Dự án này đang trong quá trình phát triển tích cực.** Một số tính năng có thể không hoạt động như mong đợi. Vui lòng báo cáo bất kỳ vấn đề nào mà bạn gặp phải.

> [!WARNING]
> Việc sử dụng module này có thể **vi phạm [Điều khoản Dịch vụ của Zalo](https://zalo.vn/dieukhoan/)**. Bạn tự chịu rủi ro khi sử dụng. Tác giả không chịu trách nhiệm cho bất kỳ trường hợp khoá tài khoản nào hoặc các hậu quả khác.

## Tải xuống
Vào trang **[phát hành mới nhất](https://github.com/ElectroHeavenVN/ZaloXposed/releases/latest) / [phát hành trước](https://github.com/ElectroHeavenVN/ZaloXposed/releases/tag/nightly)** để tải ZaloXposed.

## Chức năng

Hầu hết các tính năng có thể được bật hoặc tắt thông qua mục **ZaloXposed** trong tab **Cá nhân**. Một số tính năng yêu cầu khởi động lại ứng dụng để có hiệu lực.

Một số tính năng bao gồm:
- **Chống thu hồi và xoá tin nhắn:** Ngăn chặn việc tin nhắn bị thu hồi hoặc xoá bỏ.
- **Vô hiệu hoá theo dõi, ghi nhật ký và phân tích:** Chặn các hoạt động theo dõi, ghi nhật ký và phân tích dữ liệu của Zalo.
- **Ẩn quảng cáo:** Loại bỏ các quảng cáo và nội dung khuyến mãi khỏi ứng dụng.
- **Ẩn trạng thái đang soạn tin và đã xem:** Ngăn gửi thông báo khi bạn đang soạn tin nhắn hoặc đã đọc tin nhắn.
- **Khôi phục các tính năng bị ẩn:** Phục hồi những tính năng đã bị loại bỏ như nhạc trên trang cá nhân, sao lưu qua Google Drive, v.v.
- **Cải tiến giao diện/trải nghiệm người dùng (UI/UX):** Tùy chỉnh hình nền, mở rộng menu, tính năng Mini Chat, và nhiều tiện ích khác.
- **Mở khoá các tính năng cao cấp:** Cho phép sử dụng một số tính năng trả phí mà không cần thanh toán.

## Cách sử dụng

1. Đảm bảo thiết bị Android của bạn đã được root (vĩnh viễn hoặc tạm thời (temp root)).
2. Cài đặt Xposed framework, khuyến nghị dùng [Vector](https://github.com/JingMatrix/Vector).
3. Cài đặt tệp APK của module và kích hoạt nó, với phạm vi áp dụng được thiết lập cho ứng dụng **Zalo** (`com.zing.zalo`).
4. Buộc dừng ứng dụng Zalo, sau đó mở lại.
5. Truy cập Zalo và tìm mục **ZaloXposed** nằm ở phía trên cùng của tab **Cá nhân**.
6. Cấu hình các thiết lập theo ý muốn. Chọn nút **Khởi động lại ứng dụng** ở cuối trang cài đặt để áp dụng các thay đổi yêu cầu khởi động lại.

## Giấy phép

Dự án này được cấp phép theo Giấy phép AGPL-3.0. Xem tệp [LICENSE](https://github.com/ElectroHeavenVN/ZaloXposed/blob/main/LICENSE) để biết thêm chi tiết.

## Credit

- [libxposed API](https://github.com/libxposed/api) ([Mirror](https://github.com/JingMatrix/libxposed-api)) — modern Xposed API.
- [DexKit](https://github.com/luckypray/DexKit) — runtime dex querying.
- [smali/dexlib2](https://github.com/google/smali) — dex disassembly.
- [NPatch](https://github.com/7723mod/NPatch) - Rootless Xposed framework powered by Vector core.