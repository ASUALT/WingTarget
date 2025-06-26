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
import java.util.Objects;

public class FindTarget {
    private static final double IN_VIEW_THRESHOLD = 0.6;
    public static Target targetObject = new Target();

    public static void init(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            transform3Dto2D(client);
            if (client.player != null && client.player.isFallFlying())
                findEntity(client.player, client.world);

            // Set target in center of screen
            if (SwitchTargetKeybind.getCurrentType().equals("none") || targetObject.targetEntity == null){
                targetObject.updatePosition((client.getWindow().getScaledWidth() - Target.targetSize) / 2,
                        (client.getWindow().getScaledHeight() - Target.targetSize) / 2);
            }
        });
    }

    private static void findEntity(PlayerEntity _player, ClientWorld _world){
        if (Objects.equals(SwitchTargetKeybind.getCurrentType(), "none")) return;
        if (_player == null) return;

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

        if (!verifyTarget(_player, targetObject.targetEntity)){ _player.sendMessage(Text.literal("Entity wasn't found"), true); }
        else {
            targetObject.targetEntityDistance = (int)Math.sqrt(_player.squaredDistanceTo(targetObject.targetEntity));
            _player.sendMessage(Text.literal(targetObject.targetEntity.getName().getString() + " | " + targetObject.targetEntityDistance ), true);
        }

    }

    private static boolean verifyTarget(PlayerEntity _player, Entity _entity){
        if(_entity == null || !_entity.isAlive()) return false;
        if(!_player.canSee(_entity)) return false;
        if(_player.squaredDistanceTo(_entity) >= 16384) return false;
        if(_entity == _player) return false;
        return (_entity.getWorld() == _player.getWorld());
    }

    private static void transform3Dto2D(MinecraftClient _client){
        if(targetObject.targetEntity == null || SwitchTargetKeybind.getCurrentType() == "none") return;
        if(!isInView(_client)){
//            _client.player.sendMessage(Text.literal("Can't see the enitity"));
            return;
        }
//        _client.player.sendMessage(Text.literal("See the enitity"));

        // <editor-fold desc="Projection variables">
        Vec3d cameraPos = _client.gameRenderer.getCamera().getPos();
        Vec3d entityWorldPos = targetObject.targetEntity.getLerpedPos(RenderTickCounter.ONE.getTickDelta(true))
                                     .add(0, targetObject.targetEntity.getHeight() / 2.0, 0);

        Quaternionf invCamRot = new Quaternionf(_client.gameRenderer.getCamera().getRotation()).conjugate();
        Vector3f local = entityWorldPos.subtract(cameraPos).toVector3f();
        local.rotate(invCamRot);

        if(local.z >= 0) return;

        int screenW = _client.getWindow().getScaledWidth();
        int screenH = _client.getWindow().getScaledHeight();
        double aspect = (double) screenW/ screenH;
        double f = 1.0 / Math.tan(Math.toRadians(_client.options.getFov().getValue()) / 2.0);
        // </editor-fold>

        targetObject.updatePosition(
                (int) ( ((local.x * (f/aspect) / -local.z) * 0.5 + 0.5) * (screenW - Target.targetSize) ),
                (int) ( ((-local.y * f / -local.z) * 0.5 + 0.5) * (screenH - Target.targetSize) )  );

        _client.player.sendMessage(Text.literal(targetObject.targetPosX + " | " + targetObject.targetPosY));
    }

    private static boolean isInView(MinecraftClient _client){
        Vec3d toEntity = targetObject.targetEntity.getPos().subtract(_client.player.getCameraPosVec(RenderTickCounter.ONE.getTickDelta(true))).normalize();
        Vec3d dir = _client.player.getRotationVec(RenderTickCounter.ONE.getTickDelta(true)).normalize();

        double dot = toEntity.dotProduct(dir);
        return dot > IN_VIEW_THRESHOLD;
    }

}

