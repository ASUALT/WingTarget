package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class SwitchTargetKeybind {

    public static int typeNumber = 0;
    public static String currentType = "none";

    public static KeyBinding switchType = KeyBindingHelper.registerKeyBinding( new KeyBinding(
        "Switch target",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "Wing Target"
    ));

    public static void init(){
        ClientTickEvents.END_CLIENT_TICK.register( client -> {
            while (switchType.wasPressed())
                selectType(client);
        });
    }

    private static void selectType(@NotNull MinecraftClient _client){
        if (_client.player != null)
        {
            typeNumber = (typeNumber + 1) % 4;
            switch (typeNumber){
                case 0:
                    currentType = "none";
                    _client.player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(), 1.0F, 1.2F);
                    break;
                case 1:
                    currentType = "player";
                    _client.player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(), 1.0F, 1.2F);
                    break;
                case 2:
                    currentType = "hostile";
                    _client.player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(), 1.0F, 1.2F);
                    break;
                case 3:
                    currentType = "friendly";
                    _client.player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(), 1.0F, 1.2F);
                    break;
                default:
                    currentType = "UNDETECTABLE";
                    break;
            };
//            _client.player.sendMessage(Text.literal("Targeted to " + currentType + " entities"), true);
        }
    }

}
