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
    public static final JSONArray MSG_ARRAY = new JSONArray();

    public static void aiCheck() {
        if (!Main.getInstance().getConfig().getBoolean("ai.mute")) return;
        // 无新消息，跳过
        if (MSG_ARRAY.isEmpty()) return;
        String systemPrompt = """
            你是一个我的世界服务器聊天监控助手。
            我会传入结构化聊天记录JSON数组，每条消息结构：
            {"sender": "玩家名","content": "聊天内容"}
            我会给你最近的聊天记录，请你谨慎宽松分析，非恶意玩笑、吐槽、口语抱怨一律不判定辱骂违规，仅严格识别真正恶意人身攻击，玩家存在以下明确恶意违规行为才标记：
            1. 辱骂言语（极高容错宽松判定，满足全部恶意条件才算违规）
            宽松判定规则：
                (1）纯拼音缩写如 nm、cao、sb、tmd、wcnm、nmsl、md 等，无论单独发还是夹杂句子里，统一不判定辱骂违规；
                (2）单纯吐槽游戏、吐槽物品、吐槽机制、自嘲、抱怨自己运气差、吐槽怪物/服务器卡顿，不含针对他人人身攻击词汇，全部放行；
                (3）朋友间互怼玩笑、阴阳怪气轻度调侃、互损玩梗无恶意人身攻击，不判定辱骂；
                (4）游戏交易、招工相关词汇：黑奴、打工、苦力、代肝、代挖、打手、肝帝等，完全不构成歧视或辱骂；
                (5）仅同时满足两点才算辱骂违规：①带有针对性指向某个/某些玩家 ②使用直白汉字脏话、人身诋毁、诅咒、人身侮辱词汇；
                (6）单独发泄情绪短句（如服了、无语、离谱、烦死）无针对任何人，不算辱骂。
            
            2. 恶意刷屏
            同一玩家短时间重复发送完全相同/高度相似内容，累计8条及以上，才算恶意刷屏；少量重复刷屏、重复2-7次属于正常聊天刷屏，不予判定违规。
            
            3. 跨服广告引流
            仅出现外部其他服务器专属IP、游戏域名、非本服QQ群号，才判定广告违规；
            允许宣传本服领地、商店、副本、活动、公会等内部地点；
            纯一串数字单独出现（金额、手机号、QQ、坐标），不直接判定广告；
            本服务器标识白名单（出现以下内容不算广告）：
            服务器名：桃花源 、 夜城
            服务器IP/域名：ranmc.cc 、 mc9.city
            官方群号：429357720 、 579882505 、 1102120731
            
            输出强制规则
            1. 只要无法100%确定存在恶意违规，一律按无违规处理，禁止过度敏感、轻微口语直接判定违规；
            2. 有明确违规行为，仅输出纯JSON，无任何多余文字、解释、注释：
            {"violations":[{"player":"玩家名","reason":"违规原因描述"}]}
            3. 无任何确定违规，固定输出：{"violations":[]}""";

        AIUtil.chat(systemPrompt, MSG_ARRAY.toString())
                .whenComplete((result, ex) -> {
                    try {
                        if (ex != null || result == null || result.isEmpty()) return;
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
                                    player.sendMessage(color("&e检测发言含不当内容\n文明发言尊重他人\n持续违规将禁言封禁"));
                                }
                            }
                            count++;
                            if (Main.getInstance().getConfig().getBoolean("ai.broadcast")) {
                                StringBuilder builder = new StringBuilder();
                                MSG_ARRAY.forEach(obj -> {
                                    if (obj instanceof JSONObject json) {
                                        if (playerName.equals(json.getString("sender"))) {
                                            builder.append("\n");
                                            builder.append(json.getString("content"));
                                        }
                                    }
                                });
                                BroadcastUtil.sendFeishu("检测到违规玩家:" + playerName +
                                        "\n原因:" + reason +
                                        "\n违规次数:" + count +
                                        "\n上下文:" + builder);
                            }
                            violationsMap.put(playerName, count);
                        }
                    } catch (Exception e) {
                        print("AI 违规检测解析错误: " + e.getMessage() + "\n返回：" + result);
                    } finally {
                        MSG_ARRAY.clear();
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