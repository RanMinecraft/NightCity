package cc.ranmc.city.util;

import cc.ranmc.city.Main;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import kong.unirest.core.Unirest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static cc.ranmc.city.util.BasicUtil.color;
import static cc.ranmc.city.util.BasicUtil.print;
import static cc.ranmc.city.util.BasicUtil.run;

public class AIUtil {

    private static final int TIMEOUT = 60 * 1000;
    private static final Map<String,Integer> violationsMap = new HashMap<>();
    public static final JSONArray msgArray = new JSONArray();

    public static void aiCheck() {
        if (!Main.getInstance().getConfig().getBoolean("ai.mute")) return;
        long now = System.currentTimeMillis();
        // 无新消息，跳过
        if (msgArray.isEmpty()) return;
        String systemPrompt = """
                你是一个我的世界服务器聊天监控助手。
                我会传入结构化聊天记录JSON数组，每条消息结构：
                {"sender": "玩家名","content": "聊天内容"}
                我会给你最近的聊天记录，请你分析是否有玩家存在以下违规行为：
                1. 辱骂言语
                    注意：拼音缩写如 nm、cao、sb、tmd、wcnm、nmsl、md 等不判定为违规
                    游戏内正常交易/开玩笑/自嘲/招工话术，黑奴、打工、苦力、代肝、代挖、打手等描述不构成歧视
                2. 恶意刷屏（重复发送相同或高度相似内容10次以上）
                3. 发布宣传其他服务器广告——只有确定是宣传其他服务器才判定违规
                    允许宣传领地/商店等服务器内地点
                    只有出现其他服务器IP、域名、群号时才判定违规
                    一串纯数字可能只是QQ号码、金额等，不判定为违规
                    本服务器名：桃花源 、 夜城
                    本服务器IP或域名：ranmc.cc 、 mc9.city
                    本服务器群号：429357720 、 579882505 、 1102120731
                如果有违规行为，请用以下 JSON 格式输出（仅输出 JSON，不要多余文字）：
                {"violations":[{"player":"玩家名","reason":"违规原因描述"}]}
                如果没有违规行为，请输出：{"violations":[]}""";

        AIUtil.chat(systemPrompt, msgArray.toString())
                .thenAccept(result -> {
                    if (result == null || result.isEmpty()) return;
                    try {
                        JSONObject root = JSONObject.parseObject(result);
                        if (root == null || root.containsKey("error")) return;
                        JSONArray choices = root.getJSONArray("choices");
                        if (choices == null || choices.isEmpty()) return;
                        JSONObject first = choices.getJSONObject(0);
                        if (first == null) return;
                        JSONObject message = first.getJSONObject("message");
                        if (message == null) return;
                        String content = message.getString("content");
                        if (content == null || content.isEmpty()) return;
                        // 尝试提取 JSON
                        JSONObject aiResponse;
                        try {
                            aiResponse = JSONObject.parseObject(content);
                        } catch (Exception e) {
                            // 如果 AI 返回了带 markdown 包裹的 JSON
                            int startIdx = content.indexOf('{');
                            int endIdx = content.lastIndexOf('}');
                            if (startIdx != -1 && endIdx > startIdx) {
                                aiResponse = JSONObject.parseObject(content.substring(startIdx, endIdx + 1));
                            } else {
                                return;
                            }
                        }
                        JSONArray violations = aiResponse.getJSONArray("violations");
                        // 未发现违规行为
                        if (violations == null || violations.isEmpty()) return;
                        for (int i = 0; i < violations.size(); i++) {
                            JSONObject v = violations.getJSONObject(i);
                            String playerName = v.getString("player");
                            String reason = v.getString("reason");
                            print("&e检测到违规玩家:" + playerName + ",原因:" + reason);
                            int count = violationsMap.getOrDefault(playerName, 0);
                            if (count >= 6) {
                                // 禁言 1 天
                                run("mute " + playerName + " 1d");
                            } else if (count == 5) {
                                // 禁言 12 小时
                                run("mute " + playerName + " 12h");
                            } else if (count == 4) {
                                // 禁言 3 小时
                                run("mute " + playerName + " 3h");
                            } else if (count == 3) {
                                // 禁言 1 小时
                                run("mute " + playerName + " 1h");
                            } else if (count == 2) {
                                // 禁言 30 分钟
                                run("mute " + playerName + " 30m");
                            } else if (count == 1) {
                                // 禁言 5 分钟
                                run("mute " + playerName + " 5m");
                            } else if (count == 0) {
                                // 警告
                                Player player = Bukkit.getPlayerExact(playerName);
                                if (player != null) {
                                    player.sendMessage(color("&e检测到您的发言中存在不当内容\n文明用语是尊重他人的具体表现\n继续不当发言将面临禁言或封禁"));
                                }
                            }
                            if (Main.getInstance().getConfig().getBoolean("ai.broadcast")) {
                                StringBuilder builder = new StringBuilder();
                                msgArray.forEach(obj -> {
                                    if (obj instanceof JSONObject json) {
                                        if (playerName.equals(json.getString("sender"))) {
                                            builder.append("\n");
                                            builder.append(json.getString("content"));
                                        }
                                    }
                                });
                                BroadcastUtil.sendFeishu("检测到违规玩家:" + playerName +
                                        "\n原因:" + reason +
                                        "\n上下文:" + builder);
                            }
                            violationsMap.put(playerName, count + 1);
                        }
                    } catch (Exception e) {
                        print("AI 违规检测解析错误: " + e.getMessage() + "\n返回：" + result);
                    }
                });
    }

    public static CompletableFuture<String> chat(String systemContext, String messageContext) {

        JSONObject json = new JSONObject();
        json.put("model", Main.getInstance().getConfig().getString("ai.model"));
        json.put("stream", false);
        json.put("temperature", Main.getInstance().getConfig().getDouble("ai.temperature", 0.3));

        JSONArray messages = new JSONArray();

        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", systemContext);
        messages.add(system);

        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", messageContext);
        messages.add(user);

        json.put("messages", messages);

        String url = Main.getInstance().getConfig().getString("ai.url", "");
        return Unirest.post(url + (url.endsWith("/") ? "" : "/") + "chat/completions")
                .requestTimeout(TIMEOUT)
                .header("Authorization", "Bearer " + Main.getInstance().getConfig().getString("ai.key"))
                .header("Content-Type", "application/json")
                .body(json.toString())
                .asStringAsync()
                .thenApply(response -> {
                    if (response.getStatus() >= 200 && response.getStatus() < 300) {
                        return response.getBody();
                    } else {
                        throw new RuntimeException(
                                "HTTP Error: " + response.getStatus() + " Body: " + response.getBody()
                        );
                    }
                })
                .exceptionally(ex -> {
                    JSONObject error = new JSONObject();
                    error.put("error", ex.getMessage());
                    return error.toJSONString();
                });
    }
}