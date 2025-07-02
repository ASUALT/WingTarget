package com.asualt.wingtarget;

import com.asualt.wingtarget.target.TargetClient;
import com.asualt.wingtarget.target.SwitchTargetKeybind;
import com.asualt.wingtarget.target.WingTargetHud;
import net.fabricmc.api.ClientModInitializer;

public class WingTargetClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SwitchTargetKeybind.init();
        TargetClient.init();
        WingTargetHud.EVENT.register(new WingTargetHud());
    }
}
