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
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.Objects;

public class FindTarget {
    private static final double IN_VIEW_THRESHOLD = 0.6;
    public static Target targetObject = new Target();

    public static void init(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.isFallFlying())
                findEntity(client.player, client.world, client);

            // Set target in center of screen
            if (SwitchTargetKeybind.getCurrentType().equals("none") || targetObject.targetEntity == null)
                targetObject.updatePosition();
        });
    }

    private static void findEntity(PlayerEntity _player, ClientWorld _world, MinecraftClient _client){
        if (Objects.equals(SwitchTargetKeybind.getCurrentType(), "none")) {
            targetObject.stopTargetOnEntity();
            return;
        }

        switch (SwitchTargetKeybind.getCurrentType()){
            case "hostile":
                targetObject.targetEntity = _world.getClosestEntity(
                        HostileEntity.class, TargetPredicate.DEFAULT, _player,
                        _player.getX(), _player.getY(), _player.getZ(),
                        _player.getBoundingBox().expand(128F) );
                break;

            case "friendly":
                targetObject.targetEntity = _world.getClosestEntity(
                        PassiveEntity.class, TargetPredicate.DEFAULT, _player,
                        _player.getX(), _player.getY(), _player.getZ(),
                        _player.getBoundingBox().expand(128F) );
                break;

            case "player":
                targetObject.targetEntity = _world.getClosestEntity(
                        PlayerEntity.class, TargetPredicate.DEFAULT, _player,
                        _player.getX(), _player.getY(), _player.getZ(),
                        _player.getBoundingBox().expand(128F) );
                break;

            default: break;
        }

        if (!verifyTarget(_player, targetObject.targetEntity)){
            targetObject.stopTargetOnEntity();
        }
        else {
            targetObject.targetEntityDistance = (int)Math.sqrt(_player.squaredDistanceTo(targetObject.targetEntity));
            transform3Dto2D(_client);
            Target.targetCurrentState = Target.ETargetState.TARGET_FOLLOW;
        }

    }

    private static boolean verifyTarget(PlayerEntity _player, Entity _entity){
        if(_entity == null || !_entity.isAlive()) return false;
        if(!_player.canSee(_entity)) return false;
        if(_player.squaredDistanceTo(_entity) >= 16384) return false; // Is in radius 128 blocks
        if(_entity == _player) return false; // Check if entity is the player himself
        return (_entity.getWorld() == _player.getWorld());
    }

    private static void transform3Dto2D(MinecraftClient _client){
        if(targetObject.targetEntity == null || _client == null) return;
        if(!isInView(_client)) {
            targetObject.stopTargetOnEntity();
            return;
        }

        // <editor-fold desc="Projection variables">
        Vec3d cameraPos = _client.gameRenderer.getCamera().getPos();
        Vec3d entityWorldPos = targetObject.targetEntity
                .getLerpedPos(RenderTickCounter.ONE.getTickDelta(true))
                .add(0, targetObject.targetEntity.getHeight() / 2.0, 0);

        // Apply camera rotation
        Quaternionf invCamRot = new Quaternionf(_client.gameRenderer.getCamera().getRotation()).conjugate();
        Vector3f local = entityWorldPos.subtract(cameraPos).toVector3f();
        local.rotate(invCamRot);

        // Prevent division by zero
        if(local.z >= 0) return;

        int screenW = _client.getWindow().getScaledWidth();
        int screenH = _client.getWindow().getScaledHeight();
        double aspect = (double) screenW/ screenH;
        double f = 1.0 / Math.tan(Math.toRadians(_client.options.getFov().getValue()) / 2.0);
        // </editor-fold>

        // Project entity world position on player`s screen
        targetObject.updatePosition(
                (int) ( ((local.x * (f/aspect) / -local.z) * 0.5 + 0.5) * (screenW - Target.targetSize) ),
                (int) ( ((-local.y * f / -local.z) * 0.5 + 0.5) * (screenH - Target.targetSize) )  );
    }

    private static boolean isInView(MinecraftClient _client){
        Entity entity = targetObject.targetEntity;
        if(entity == null || _client == null || _client.player == null) return false;
        Vec3d toEntity = entity.getPos().subtract(_client.player.getCameraPosVec(RenderTickCounter.ONE.getTickDelta(true))).normalize();
        Vec3d dir = _client.player.getRotationVec(RenderTickCounter.ONE.getTickDelta(true)).normalize();

        double dot = toEntity.dotProduct(dir); // Dot product shows how directionally aligned vectors are
        return dot > IN_VIEW_THRESHOLD;
    }

}

