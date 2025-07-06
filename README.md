<h1>Chess Game Online Backend</h1>

<h3>Giới thiệu</h3>Chess Game Online Backend là phần backend của dự án Chess Game Online - 1 ứng dụng giả lập chơi Cờ vua trên điện thoại với nhiều chế độ khác nhau:<br>
- PvP : Người với Người , chế độ Đấu thường hoặc Đấu Xếp hạng.<br>
- PvE (AI) : Người với Máy, ứng dụng thuật toán Minimax với Alpha-Beta Pruning, AI Difficult level.<br>
- Local : 2 Người chơi 1 máy, với các tính năng thử nghiệm mới : chế độ Critical Hit (quân cờ có tỉ lệ đi tiếp sau khi ăn quân cờ khác) và Emote System (hệ thống biểu cảm cho các quân cờ).<br>

<h3>Mô hình kiến trúc</h3><br>
![image](src\main\resources\assets\architect.png)


<h3>Quản lý thư mục</h3>

![image](https://github.com/user-attachments/assets/fd7e40e2-f60a-4b53-aa82-ed85fcf21a89)


<h3>Nhiệm vụ</h3>
- Kết nối Firebase - Cloud Firestore cho cơ sở dữ liệu chính của dự án<br>
- Thiết kế RESTfull API cho HTTP request thao tác với dữ liệu.<br>
- Thiết lập, xử lý giao thức Websocket cho Server và Client.<br>

