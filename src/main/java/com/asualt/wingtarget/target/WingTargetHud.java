package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class WingTargetHud implements HudRenderCallback {

    public static final Identifier TARGET_TEXTURE = Identifier.of("wingtarget", "textures/gui/target_idle.png");

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if( player == null) return;
        // Get current item in chest slot
        ItemStack itemChest = player.getEquippedStack(EquipmentSlot.CHEST);

        // Calculate target position
        Window wd = client.getWindow();
        int screenWidth = wd.getScaledWidth();
        int screenHeight = wd.getScaledHeight();
        int targetSize = 32;
        int x = (screenWidth - targetSize) / 2;
        int y = (screenHeight - targetSize) / 2;

        // Draw current target
        if (itemChest.getItem() == Items.ELYTRA)
            drawContext.drawText( client.textRenderer, "Target: " + SwitchTargetKeybind.currentType, (int)(screenWidth * 0.2163F), (int)(screenHeight * 0.806F), 0x00FF00, false);

        // Draw target
        if(player.isFallFlying())
            drawContext.drawTexture(TARGET_TEXTURE, x, y, 0, 0, targetSize, targetSize, targetSize, targetSize);


    }
}
