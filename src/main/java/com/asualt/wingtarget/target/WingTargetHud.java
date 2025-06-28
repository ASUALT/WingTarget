package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

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
        boolean isCrossbow = currentItem.getItem() == Items.CROSSBOW;
        boolean isCrossbowCharged = CrossbowItem.isCharged(currentItem);

        // If player holding crossbow draw target_follow
        if( isCrossbow ){
            if (!isCrossbowCharged)
                Target.targetCurrentState = Target.ETargetState.TARGET_FOLLOW;

            // if player locked on target draw target_locked
            if (FindTarget.targetObject.isLocked() && isCrossbowCharged){
                FindTarget.targetObject.playSoundOnLock();
                drawTarget(drawContext, Target.ETargetState.TARGET_LOCKED.value());
            }
            // Draw targe_follow
            else if (FindTarget.targetObject.isFollowing()) {
                FindTarget.targetObject.playSoundOnFollow();
                Target.canPlaySoundOnLock = true;
                drawTarget(drawContext, Target.ETargetState.TARGET_FOLLOW.value());

                // Draw lock timer
                drawText(drawContext, String.format("%.2f", Target.lockTime / 20.0),
                        FindTarget.targetObject.targetPosX -15,
                        FindTarget.targetObject.targetPosY);
            }

        }
        // Else draw target_idle
        else {
            Target.canPlaySound = true;
            Target.canPlaySoundOnLock = true;
            drawTarget(drawContext, Target.ETargetState.TARGET_IDLE.value());
        }

        // Draw targeted entity name
        drawText(drawContext, targetName,
                FindTarget.targetObject.targetPosX + 28,
                FindTarget.targetObject.targetPosY);

        // Draw targeted entity distance
        drawText(drawContext, targetDistance,
                FindTarget.targetObject.targetPosX + 28,
                FindTarget.targetObject.targetPosY + 10);
    }

    private void drawTarget(DrawContext dc, Identifier textrure){
        dc.drawTexture(
                textrure,
                FindTarget.targetObject.targetPosX, FindTarget.targetObject.targetPosY,
                0, 0,
                Target.targetSize, Target.targetSize, Target.targetSize, Target.targetSize);
    }
    private void drawText(DrawContext dc, String text, int x, int y){
        dc.drawText(
                MinecraftClient.getInstance().textRenderer, text,
                x, y,
                0xFFFFFF, true);
    }
}
