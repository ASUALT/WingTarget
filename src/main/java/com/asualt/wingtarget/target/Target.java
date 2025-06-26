package com.asualt.wingtarget.target;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class Target {

    public enum ETargetState {
        TARGET_IDLE("textures/gui/target_idle.png"),
        TARGET_FOLLOW("textures/gui/target_follow.png"),
        TARGET_LOCKED("textures/gui/target_locked.png");

        ETargetState(String texturePath) { targetTexture = Identifier.of("wingtarget", texturePath); }
        private final Identifier targetTexture;
        public Identifier getTargetTexture(){ return  targetTexture; }
    };

    public static ETargetState currentState = ETargetState.TARGET_IDLE;
    public static final int targetSize = 32;

    public boolean isLocked = false;
    public boolean isFollowing = false;
    public int targetPosX = 0, targetPosY = 0;

    public Entity targetEntity = null;
    public int targetEntityDistance = 0;

    public void updatePosition(int x, int y){ targetPosX = x; targetPosY = y; }


}
