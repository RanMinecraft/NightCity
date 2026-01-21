package cc.ranmc.city.util;

import cc.ranmc.city.bean.InputCallback;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;

import java.util.List;

import static cc.ranmc.city.util.BasicUtil.color;

public class InputUtil {

    public static void open(Player player, String title, String text, InputCallback callback) {
        open(player, title, text, "", callback);
    }

    public static void open(Player player, String title, String text, String initial, InputCallback callback) {
        player.showDialog(Dialog.create(builder -> builder
                .empty()
                .base(DialogBase.builder(Component.text(color("&9&l夜城 &0- &e&l" + title)))
                        .canCloseWithEscape(true)
                        .inputs(List.of(
                                DialogInput
                                        .text("text", Component.text(text))
                                        .initial(initial)
                                        .maxLength(99)
                                        .build()
                        )).build()
                )
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("取消")).build(),
                        ActionButton.builder(Component.text("确认"))
                                .action(DialogAction.customClick((response, _) -> {
                                    String inputText = response.getText("text");
                                    if (inputText == null || inputText.isEmpty()) {
                                        player.sendMessage(color("&c输入的内容不能为空"));
                                        return;
                                    }
                                    callback.onCallback(inputText);
                                }, ClickCallback.Options.builder().lifetime(ClickCallback.DEFAULT_LIFETIME).build())).build()
                ))));
        return;
    }
}
