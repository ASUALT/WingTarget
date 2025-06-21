package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WingTargetHud implements HudRenderCallback {

    public static final Identifier TARGET_TEXTURE = Identifier.of("wingtarget", "textures/gui/target.png");

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;

        // Don't draw target if not in fly state
        if( player == null || !player.isFallFlying()) return;

        // Draw target
        Window wd = mc.getWindow();
        int screenWidth = wd.getScaledWidth();
        int screenHeight = wd.getScaledHeight();
        int targetSize = 16;

        int x = (screenWidth - targetSize) / 2;
        int y = (screenHeight - targetSize) / 2;
        mc.player.sendMessage(Text.literal(x + " " + y), true);

        drawContext.drawTexture(TARGET_TEXTURE, x, y, 0, 0, targetSize, targetSize, targetSize, targetSize);

    }
}
