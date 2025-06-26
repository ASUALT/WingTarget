package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class WingTargetHud implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {

        MinecraftClient _client = MinecraftClient.getInstance();

        if( _client.player == null) return;
        // Get current item in chest slot
        ItemStack itemChest = _client.player.getEquippedStack(EquipmentSlot.CHEST);

        int screenWidth = _client.getWindow().getScaledWidth();
        int screenHeight = _client.getWindow().getScaledHeight();
        int targetSize = Target.targetSize;

        Identifier TARGET_TEXTURE = Target.currentState.getTargetTexture();

        // Display current target
        if (itemChest.getItem() == Items.ELYTRA)
            drawContext.drawText(
                    _client.textRenderer, "Target: " + SwitchTargetKeybind.getCurrentType(),
                    (int)(screenWidth * 0.2163F), (int)(screenHeight * 0.806F),
                    0x00FF00, false);

        if(FindTarget.targetObject.targetEntity == null) return;

        // Draw idle target
        //if( _client.player.isFallFlying() )
        drawContext.drawTexture(
                TARGET_TEXTURE,
                FindTarget.targetObject.targetPosX, FindTarget.targetObject.targetPosY,
                0, 0,
                targetSize, targetSize, targetSize, targetSize);

        // Draw targeted entity name
        drawContext.drawText(
                _client.textRenderer, FindTarget.targetObject.targetEntity.getName().getString(),
                FindTarget.targetObject.targetPosX + 20, FindTarget.targetObject.targetPosY,
                0xFFFFFF, false);


    }
}
