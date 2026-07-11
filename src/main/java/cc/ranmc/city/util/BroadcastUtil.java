package cc.ranmc.city.util;

import cc.ranmc.city.Main;
import com.alibaba.fastjson2.JSONObject;
import kong.unirest.core.Unirest;

import static cc.ranmc.city.util.BasicUtil.print;

public class BroadcastUtil {

    public static void sendFeishu(String text) {
        try {
            JSONObject body = new JSONObject();
            body.put("msg_type", "text");
            JSONObject content = new JSONObject();
            content.put("text", text);
            body.put("content", content);

            Unirest.post(Main.getInstance().getConfig().getString("feishu-webhook"))
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .requestTimeout(8000)
                    .asStringAsync()
                    .thenAccept(response -> {
                        if (response.getStatus() < 200 || response.getStatus() >= 300) {
                            print("&c发送飞书提醒失败: " + response.getBody());
                        }
                    });
        } catch (Exception e) {
            print("&c发送飞书提醒错误: " + e.getMessage());
        }
    }
}
