package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class WingTargetHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {

    MinecraftClient _client = MinecraftClient.getInstance();
    if (_client == null || _client.player == null) return;

    ItemStack itemChest = _client.player.getEquippedStack(EquipmentSlot.CHEST); // Get current item in chest slot
    int screenWidth = _client.getWindow().getScaledWidth();
    int screenHeight = _client.getWindow().getScaledHeight();
    int targetSize = Target.targetSize;

        // Display current target type
        if (itemChest.getItem() == Items.ELYTRA)
            drawContext.drawText(
                    _client.textRenderer, "Target: " + SwitchTargetKeybind.getCurrentType(),
                    (int)(screenWidth * 0.2163F), (int)(screenHeight * 0.806F),
                    0x00FF00, false);

        if (FindTarget.targetObject == null) return;

        Entity entity = FindTarget.targetObject.targetEntity;
        if (entity == null || FindTarget.targetObject.targetEntityDistance == 0) return;

        String targetName = entity.getName().getString();
        String targetDistance = Integer.toString(FindTarget.targetObject.targetEntityDistance);

        // Draw target
        if(!_client.player.isFallFlying()) return;
        ItemStack currentItem = _client.player.getMainHandStack();

        // If player holding crossbow draw target_follow
        if( currentItem.getItem() == Items.CROSSBOW){
            // if player locked on target draw target_locked
            if (!CrossbowItem.isCharged(currentItem)){ Target.targetCurrentState = Target.ETargetState.TARGET_FOLLOW; }
            if (Target.targetCurrentState == Target.ETargetState.TARGET_LOCKED && CrossbowItem.isCharged(currentItem)){
                FindTarget.targetObject.playSoundOnLock();
                drawContext.drawTexture(
                        Target.ETargetState.TARGET_LOCKED.value(),
                        FindTarget.targetObject.targetPosX, FindTarget.targetObject.targetPosY,
                        0, 0,
                        targetSize, targetSize, targetSize, targetSize);
            }
            if (Target.targetCurrentState == Target.ETargetState.TARGET_FOLLOW) {
                FindTarget.targetObject.playSoundOnFollow();
                Target.canPlaySoundOnLock = true;
                drawContext.drawTexture(
                        Target.ETargetState.TARGET_FOLLOW.value(),
                        FindTarget.targetObject.targetPosX, FindTarget.targetObject.targetPosY,
                        0, 0,
                        targetSize, targetSize, targetSize, targetSize);
            }

        }
        // Else draw target_idle
        else {
            Target.canPlaySound = true;
            Target.canPlaySoundOnLock = true;
            drawContext.drawTexture(
                    Target.ETargetState.TARGET_IDLE.value(),
                    FindTarget.targetObject.targetPosX, FindTarget.targetObject.targetPosY,
                    0, 0,
                    targetSize, targetSize, targetSize, targetSize);
        }

        // Draw targeted entity name
        drawContext.drawText(
                _client.textRenderer, targetName,
                FindTarget.targetObject.targetPosX + 28, FindTarget.targetObject.targetPosY,
                0xFFFFFF, false);

        // Draw targeted entity distance
        drawContext.drawText(
                _client.textRenderer, targetDistance,
                FindTarget.targetObject.targetPosX + 28, FindTarget.targetObject.targetPosY + 10,
                0xFFFFFF, false);
    }
}
