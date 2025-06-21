package com.asualt.wingtarget;

import com.asualt.wingtarget.target.WingTargetHud;
import net.fabricmc.api.ClientModInitializer;

public class WingTargetClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WingTargetHud.EVENT.register(new WingTargetHud());

    }
}
