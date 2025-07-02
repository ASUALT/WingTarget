package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class TargetClient {
    public static final TargetController tc = new TargetController(new Target());


    public static void init(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (tc.readyToStart())
                findEntity();
            else {
                tc.resetTargetPosition();
                tc.resetTarget();
            }
        });
    }

    private static void findEntity(){
        if (!tc.readyToStart() || SwitchTargetKeybind.getCurrentTypeRaw() == null) {
            tc.resetTargetPosition();
            tc.resetTarget();
            return;
        }

        tc.findNearestEntity(SwitchTargetKeybind.getCurrentTypeRaw());

        if (tc.verifyTarget() && tc.isInView()) tc.projectEntityPositionOnScreen();
        else {
            tc.resetTargetPosition();
            tc.resetTarget();
        }
    }
}

