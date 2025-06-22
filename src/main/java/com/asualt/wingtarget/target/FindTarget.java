package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class FindTarget {
    public static Entity entity;
    public static List<PlayerEntity> playerEntities;
    public static PlayerEntity closestPlayerEntity;
    private static int tickCounter = 0;

    public static void init(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter == 5) {
                tickCounter = 0;
                if (client.player != null && client.player.isFallFlying())
                    findEntity(MinecraftClient.getInstance(), SwitchTargetKeybind.currentType);
            }
        });
    }

    private static void findEntity(MinecraftClient client, String entityType){
        if (Objects.equals(entityType, "none")) return;

        PlayerEntity player = client.player;
        if (player == null) return;
        ClientWorld world = MinecraftClient.getInstance().world;

        if(entityType == "hostile"){
            entity = world.getClosestEntity(
                    HostileEntity.class,
                    TargetPredicate.DEFAULT,
                    player,
                    player.getX(), player.getY(), player.getZ(),
                    player.getBoundingBox().expand(128F) );
        }
        else if(entityType == "friendly"){
            entity = world.getClosestEntity(
                    PassiveEntity.class,
                    TargetPredicate.DEFAULT,
                    player,
                    player.getX(), player.getY(), player.getZ(),
                    player.getBoundingBox().expand(128F) );
        }
        else if (entityType == "player"){
            playerEntities = world.getPlayers(TargetPredicate.DEFAULT, player, player.getBoundingBox().expand(128F));
            closestPlayerEntity(player);
            return;
        }
        else return;

        if (!verifyTarget(player)){
            player.sendMessage(Text.literal("Entity wasn't found"), true);
            return;
        }
        else {
            int distance = (int)Math.sqrt(player.squaredDistanceTo(entity));
            player.sendMessage(Text.literal(entity.getName().getString() + " | " + distance ), true);
        }

    }

    private static boolean verifyTarget(PlayerEntity player){
        if(entity == null || !entity.isAlive()) return false;
        if(!player.canSee(entity)) return false;
        if(player.squaredDistanceTo(entity) >= 16384) return false;
        return (entity.getWorld() == player.getWorld());
    }

    private static void closestPlayerEntity(PlayerEntity player){
        double closestDist = Double.MAX_VALUE;

        for (PlayerEntity p : playerEntities){
            if(!player.canSee(p)) continue;
            if(!p.isAlive()) continue;
            if(p.getWorld() != player.getWorld()) continue;
            if(p == player) continue;

            double dist = player.squaredDistanceTo(p);
            if(dist <= 16384 && dist < closestDist){
                closestDist = dist;
                closestPlayerEntity = p;
            }
        if (closestPlayerEntity == null)
            player.sendMessage(Text.literal("Entity wasn't found"), true);
        else
            player.sendMessage(Text.literal(closestPlayerEntity.getName().getString() + " | " + (int)Math.sqrt(dist) ), true);

        }
    }
}
