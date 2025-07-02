package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SwitchTargetKeybind {

    private static int typeNumber = -1;
    private static final List<Class<? extends LivingEntity>> entityTypes = List.of(HostileEntity.class, PassiveEntity.class, PlayerEntity.class);
    private static Class<? extends LivingEntity> currentType = null;

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

    private static void selectType(MinecraftClient _client){
        if (_client.player == null) return;

        typeNumber = (typeNumber + 1) % (entityTypes.size() + 1);
        currentType = typeNumber == entityTypes.size() ? null : entityTypes.get(typeNumber);
        _client.player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(), 1.0F, 1.2F);
    }

    public static Class<? extends LivingEntity> getCurrentTypeRaw() { return currentType; }
    public static String getCurrentTypeString(){
        if (currentType == null) return "none";
        if (currentType == HostileEntity.class) return "hostile";
        if (currentType == PassiveEntity.class) return "friendly";
        if (currentType == PlayerEntity.class) return "player";
        return "UNDEFINED";
    }

}
