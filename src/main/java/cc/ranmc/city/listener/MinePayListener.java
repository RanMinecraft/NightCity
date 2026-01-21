package cc.ranmc.city.listener;

import cc.ranmc.city.util.TitleUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import top.minepay.api.events.MinePayPreTradingEvent;
import top.minepay.bean.TradeInfo;
import top.minepay.common.enums.TradeType;

public class MinePayListener implements Listener {

    /**
     * 开始交易
     *
     * @param event 事件
     */
    @EventHandler
    public void onMinePayPreTradingEvent(MinePayPreTradingEvent event) {
        TradeInfo info = event.getTradeInfo();
        if (info.getTradeType() == TradeType.POINT) {
            info.setPrice(TitleUtil.getPrice(info.getPlayerName()) * 100);
        }
    }
}
