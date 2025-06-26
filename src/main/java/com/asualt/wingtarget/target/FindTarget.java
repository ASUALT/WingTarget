package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class FindTarget {
    public static Entity entity;
    public static List<PlayerEntity> playerEntities;
    public static PlayerEntity closestPlayerEntity;

    public static int targetX = 0;
    public static int targetY = 0;
    public static String entityName = "none";

    public static void init(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            transform3Dto2D(client);
            if (client.player != null && client.player.isFallFlying())
                findEntity(client.player, client.world);

            if (SwitchTargetKeybind.currentType == "none" || entity == null){
                targetX = (client.getWindow().getScaledWidth() - 32) / 2;
                targetY = (client.getWindow().getScaledHeight() - 32) / 2;
            }
        });
    }

    private static void findEntity(PlayerEntity _player, ClientWorld _world){
        if (Objects.equals(SwitchTargetKeybind.currentType, "none")) return;

        if (_player == null) return;

        if(SwitchTargetKeybind.currentType == "hostile"){
            entity = _world.getClosestEntity(
                    HostileEntity.class,
                    TargetPredicate.DEFAULT,
                    _player,
                    _player.getX(), _player.getY(), _player.getZ(),
                    _player.getBoundingBox().expand(128F) );
        }
        else if(SwitchTargetKeybind.currentType == "friendly"){
            entity = _world.getClosestEntity(
                    PassiveEntity.class,
                    TargetPredicate.DEFAULT,
                    _player,
                    _player.getX(), _player.getY(), _player.getZ(),
                    _player.getBoundingBox().expand(128F) );
        }
        else if (SwitchTargetKeybind.currentType == "_player"){
            playerEntities = _world.getPlayers(TargetPredicate.DEFAULT, _player, _player.getBoundingBox().expand(128F));
            closestPlayerEntity(_player);
            return;
        }
        else return;

        if (!verifyTarget(_player)){
            _player.sendMessage(Text.literal("Entity wasn't found"), true);
            return;
        }
        else {
            int distance = (int)Math.sqrt(_player.squaredDistanceTo(entity));
            _player.sendMessage(Text.literal(entity.getName().getString() + " | " + distance ), true);
        }

    }

    private static boolean verifyTarget(PlayerEntity _player){
        if(entity == null || !entity.isAlive()) return false;
        if(!_player.canSee(entity)) return false;
        if(_player.squaredDistanceTo(entity) >= 16384) return false;
        return (entity.getWorld() == _player.getWorld());
    }

    private static void closestPlayerEntity(PlayerEntity _player){
        double closestDist = Double.MAX_VALUE;

        for (PlayerEntity p : playerEntities){
            if(!_player.canSee(p)) continue;
            if(!p.isAlive()) continue;
            if(p.getWorld() != _player.getWorld()) continue;
            if(p == _player) continue;

            double dist = _player.squaredDistanceTo(p);
            if(dist <= 16384 && dist < closestDist){
                closestDist = dist;
                closestPlayerEntity = p;
            }
        if (closestPlayerEntity == null)
            _player.sendMessage(Text.literal("Entity wasn't found"), true);
        else
            _player.sendMessage(Text.literal(closestPlayerEntity.getName().getString() + " | " + (int)Math.sqrt(dist) ), true);

        }
    }

    private static void transform3Dto2D(MinecraftClient _client){
        if(entity == null || SwitchTargetKeybind.currentType == "none") return;
        if(!isInView(_client)){
//            _client.player.sendMessage(Text.literal("Can't see the enitity"));
            return;
        }
//        _client.player.sendMessage(Text.literal("See the enitity"));

        entityName = entity.getName().getString();

        // <editor-fold desc="Projection variables">
        Vec3d cameraPos = _client.gameRenderer.getCamera().getPos();
        Vec3d entityWorldPos = entity.getLerpedPos(RenderTickCounter.ONE.getTickDelta(true)).add(0, entity.getHeight() / 2.0, 0);

        Quaternionf invCamRot = new Quaternionf(_client.gameRenderer.getCamera().getRotation()).conjugate();
        Vector3f local = entityWorldPos.subtract(cameraPos).toVector3f();
        local.rotate(invCamRot);

        if(local.z >= 0) return;

        int screenW = _client.getWindow().getScaledWidth();
        int screenH = _client.getWindow().getScaledHeight();
        double aspect = (double) screenW/ screenH;
        double f = 1.0 / Math.tan(Math.toRadians(_client.options.getFov().getValue()) / 2.0);
        // </editor-fold>

        targetX = (int) ( ((local.x * (f/aspect) / -local.z) * 0.5 + 0.5) * (screenW - 32) );
        targetY = (int) ( ((-local.y * f / -local.z) * 0.5 + 0.5) * (screenH - 32) );

        _client.player.sendMessage(Text.literal(targetX + " | " + targetY));
    }

    private static boolean isInView(MinecraftClient _client){
        Vec3d toEntity = entity.getPos().subtract(_client.player.getCameraPosVec(RenderTickCounter.ONE.getTickDelta(true))).normalize();
        Vec3d dir = _client.player.getRotationVec(RenderTickCounter.ONE.getTickDelta(true)).normalize();

        double dot = toEntity.dotProduct(dir);
        return dot > 0.6F;
    }

}









