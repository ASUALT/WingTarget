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

    public static final Identifier TARGET_TEXTURE = Identifier.of("wingtarget", "textures/gui/target_idle.png");

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {

        MinecraftClient _client = MinecraftClient.getInstance();

        if( _client.player == null) return;
        // Get current item in chest slot
        ItemStack itemChest = _client.player.getEquippedStack(EquipmentSlot.CHEST);

        // Calculate target position
        int screenWidth = _client.getWindow().getScaledWidth();
        int screenHeight = _client.getWindow().getScaledHeight();
        int targetSize = 32;

        // Draw current target
        if (itemChest.getItem() == Items.ELYTRA)
            drawContext.drawText( _client.textRenderer, "Target: " + SwitchTargetKeybind.currentType, (int)(screenWidth * 0.2163F), (int)(screenHeight * 0.806F), 0x00FF00, false);

        // Draw target
        //if( _client.player.isFallFlying() )
        drawContext.drawTexture(TARGET_TEXTURE, FindTarget.targetX, FindTarget.targetY, 0, 0, targetSize, targetSize, targetSize, targetSize);
        drawContext.drawText(_client.textRenderer, FindTarget.entityName, FindTarget.targetX + 25, FindTarget.targetY -5, 0xFFFFFF, false);


    }
}
