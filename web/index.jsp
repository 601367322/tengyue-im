<%--
  Created by IntelliJ IDEA.
  User: bingbing
  Date: 2017/7/25
  Time: 下午6:42
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>$Title$</title>
    <script src="http://code.jquery.com/jquery-1.4.1.min.js"></script>
    <script type="text/javascript">

        var ws;

        function WebSocketTest() {
            if ("WebSocket" in window) {

                // 打开一个 web socket
                ws = new WebSocket($("#address").val());

                ws.onopen = function () {
                    // Web Socket 已连接上，使用 send() 方法发送数据


                    ws.send("{'cmd':'connect','fromId':'" + $("#userId").val() + "'}");
                    console.log("数据发送中...");
                };

                ws.onmessage = function (evt) {
                    var received_msg = evt.data;
                    console.log("数据已接收...\n" + received_msg);

                    $("#receive").append(received_msg+"</br>");
                };

                ws.onclose = function () {
                    // 关闭 websocket
                    console.log("连接已关闭...");
                };

                ws.onerror = function (evt) {
                    console.log("连接异常...\n" + evt.data);
                }
            }

            else {
                // 浏览器不支持 WebSocket
                console.log("您的浏览器不支持 WebSocket!");
            }
        }

        function sendMessage() {
            $.ajax({
                url: "http://localhost:8082/msg/send",
                data: {text: $("#message").val(), fromId: $("#userId").val(), toId: $("#otherUserId").val()}
            });
        }

    </script>
</head>
<body>
<div>
    <div>用户ID：<input id="userId" type="text"/></div>
    <div>服务器地址：<input id="address" type="text" style="width:200px" value="ws://localhost:8083/"/></div>
    <a href="javascript:WebSocketTest()">连接</a>
    <div>对方的用户ID：<input type="text" id="otherUserId"/></div>
    <div>输入消息内容：<input type="text" value="Hello world!" id="message"/></div>
    <a href="javascript:sendMessage()">给他发消息</a>
    <div id="receive"></div>
</div>
</body>
</html>
