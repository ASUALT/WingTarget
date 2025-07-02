package com.asualt.wingtarget.target;

import net.minecraft.entity.Entity;

public class Target {

    public int lockTime = 40;
    public boolean justFollowing = false;
    public boolean justLocked = false;

    public TargetController.ETargetState currentState = TargetController.ETargetState.TARGET_IDLE;

    public final int targetSize = 32;
    public int targetPosX = 0;
    public int targetPosY = 0;

    public Entity targetEntity = null;

}
