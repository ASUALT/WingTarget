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
        if (_client == null || _client.player == null) return;

        TargetController tc = TargetClient.tc;

        ItemStack itemChest = _client.player.getEquippedStack(EquipmentSlot.CHEST); // Get current item in chest slot
        // Display current target type
        if (itemChest.getItem() == Items.ELYTRA)
            drawContext.drawText(
                    _client.textRenderer, ("Target: " + SwitchTargetKeybind.getCurrentTypeString()),
                    (int)(_client.getWindow().getScaledWidth() * 0.2163F),
                    (int)(_client.getWindow().getScaledHeight() * 0.806F),
                    0x00FF00, false);

        if(!_client.player.isFallFlying()) return;

        drawTarget(tc, drawContext, tc.getState().value());

        // Draw lock timer
        if (!tc.isIdling())
            drawText(drawContext, String.format("%.2f", tc.getLockTime() / 20.0), tc.getPosX() - 15, tc.getPosY());

        // Draw targeted entity name
        drawText(drawContext, tc.getEntityName(), tc.getPosX() + 28, tc.getPosY());

        // Draw targeted entity distance
        String distance = tc.getEntityDistance() == 0 ? "" : Integer.toString(tc.getEntityDistance());
        drawText(drawContext, distance, tc.getPosX() + 28, tc.getPosY() + 10);
    }

    private void drawTarget(TargetController tc, DrawContext dc, Identifier textrure){
        dc.drawTexture(
                textrure,
                tc.getPosX(), tc.getPosY(),
                0, 0,
                tc.getTargetSize(), tc.getTargetSize(),
                tc.getTargetSize(), tc.getTargetSize());
    }
    private void drawText(DrawContext dc, String text, int x, int y){
        dc.drawText(
                MinecraftClient.getInstance().textRenderer, text,
                x, y,
                0xFFFFFF, true);
    }
}
