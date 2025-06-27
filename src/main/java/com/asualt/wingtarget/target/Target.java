package com.asualt.wingtarget.target;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class Target {

    public static int lockTime = 40;
    public static boolean canPlaySound = true;
    public static boolean canPlaySoundOnLock = false;

    public enum ETargetState {
        TARGET_IDLE("textures/gui/target_idle.png"),
        TARGET_FOLLOW("textures/gui/target_follow.png"),
        TARGET_LOCKED("textures/gui/target_locked.png");

        ETargetState(String texturePath) { targetTexture = Identifier.of("wingtarget", texturePath); }
        private final Identifier targetTexture;
        public Identifier value(){ return  targetTexture; }
    };

    public static ETargetState targetCurrentState = ETargetState.TARGET_IDLE;
    public static final int targetSize = 32;
    public int targetPosX = 0;
    public int targetPosY = 0;

    public Entity targetEntity = null;
    public int targetEntityDistance = 0;

    public void updatePosition(int x, int y){ targetPosX = x; targetPosY = y; }
    public void updatePosition(){
        targetPosX = (MinecraftClient.getInstance().getWindow().getScaledWidth() - Target.targetSize) / 2;
        targetPosY = (MinecraftClient.getInstance().getWindow().getScaledHeight() - Target.targetSize) / 2;
    }

    public void stopTargetOnEntity(){
        targetCurrentState = ETargetState.TARGET_IDLE;
        targetEntity = null;
        targetEntityDistance = 0;
        targetPosX = (MinecraftClient.getInstance().getWindow().getScaledWidth() - Target.targetSize) / 2;
        targetPosY = (MinecraftClient.getInstance().getWindow().getScaledHeight() - Target.targetSize) / 2;
    }

    public void playSoundOnFollow(){
        if (!canPlaySound) return;
        MinecraftClient.getInstance().player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_TURTLE.value(), 1.0F, 0.8F);
        canPlaySound = false;
        canPlaySoundOnLock = true;
    }

    public void playSoundOnLock(){
        if (!canPlaySoundOnLock) return;
        MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0F, 1.2F);
        canPlaySound = false;
        canPlaySoundOnLock = false;

    }
}
