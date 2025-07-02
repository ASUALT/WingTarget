package com.asualt.wingtarget.target;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TargetController {
    private final double IN_VIEW_THRESHOLD = 0.6;
    private final double IN_LOCK_THRESHOLD = 0.96;
    private MinecraftClient _client;

    public Target target;

    public enum ETargetState {
        TARGET_IDLE("textures/gui/target_idle.png"),
        TARGET_FOLLOW("textures/gui/target_follow.png"),
        TARGET_LOCKED("textures/gui/target_locked.png");

        ETargetState(String texturePath) { targetTexture = Identifier.of("wingtarget", texturePath); }
        private final Identifier targetTexture;
        public Identifier value(){ return  targetTexture; }
    };

    public TargetController(Target target){
        this.target = target;
        _client = MinecraftClient.getInstance();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            _client = client;
            updateState();
            updateSound();
        });
    }
    public void updateState(){
        Entity entity = target.targetEntity;
        if (entity == null) {
            resetTarget();
            return;
        }

        ItemStack itemMainHand = _client.player.getMainHandStack();
        ItemStack itemOffHand = _client.player.getOffHandStack();
        boolean isCrossbow = (itemMainHand.getItem() == Items.CROSSBOW) || (itemOffHand.getItem() == Items.CROSSBOW);
        boolean isCrossbowCharged = CrossbowItem.isCharged(itemMainHand) || CrossbowItem.isCharged(itemOffHand);

        if (isCrossbow && isLocked() && (!isCrossbowCharged || !isInLockView())){
            setState(ETargetState.TARGET_FOLLOW);
            return;
        }

        if (!isCrossbow && isLocked() && (!isCrossbowCharged || !isInLockView())){
            setState(ETargetState.TARGET_IDLE);
            return;
        }

        if (tryLockOnTarget(isCrossbowCharged, isInLockView())){
            setState(ETargetState.TARGET_LOCKED);
            return;
        }
        if (isCrossbow && !isLocked()){
            setState(ETargetState.TARGET_FOLLOW);
            return;
        }
        if (!isLocked()) setState(ETargetState.TARGET_IDLE);
    }
    public void updateSound() {
        if (wasLocked())
            playSoundOnLock();
        else if (wasFollowing())
            playSoundOnFollow();
    }

    public boolean readyToStart(){
        return _client != null &&
               _client.player != null &&
               _client.world != null &&
               !_client.isPaused() &&
               _client.player.isAlive() &&
               _client.player.isFallFlying();
    }

    public int getTargetSize() { return target.targetSize; }

    // Target postion
    public void updateTargetPosition(int x, int y){ target.targetPosX = x; target.targetPosY = y; }
    public void resetTargetPosition(){
        target.targetPosX = (_client.getWindow().getScaledWidth() - target.targetSize) / 2;
        target.targetPosY = (_client.getWindow().getScaledHeight() - target.targetSize) / 2;
    }
    public int getPosX(){ return target.targetPosX; }
    public int getPosY(){ return target.targetPosY; }

    // Targeted entity
    public Entity getEntity(){ return target.targetEntity; }
    public String getEntityName() {
        Entity entity = target.targetEntity;
        return entity == null ? "" : entity.getName().getString();
    }

    public int getEntityDistance(){
        Entity entity = target.targetEntity;
        return entity == null ? 0 : (int) Math.sqrt(_client.player.squaredDistanceTo(entity));
    }
    public void resetTarget(){
        setState(ETargetState.TARGET_IDLE);
        target.targetEntity = null;
        target.targetPosX = (_client.getWindow().getScaledWidth() - target.targetSize) / 2;
        target.targetPosY = (_client.getWindow().getScaledHeight() - target.targetSize) / 2;
        target.justLocked = false;
        target.justFollowing = false;
        resetLockTime();
    }

    public boolean verifyTarget(){
        if(target.targetEntity == null || !target.targetEntity.isAlive()) { return false; }
        if(!_client.player.canSee(target.targetEntity)) return false;
        if(_client.player.squaredDistanceTo(target.targetEntity) >= 16384) return false; // Is in radius 128 blocks
        if(target.targetEntity == _client.player) return false; // Check if entity is the player himself
        return (target.targetEntity.getWorld() == _client.player.getWorld());
    }
    public void findNearestEntity(Class<? extends LivingEntity> entityClass){
        if (entityClass == null) return;
        target.targetEntity = _client.world.getClosestEntity(
                entityClass, TargetPredicate.DEFAULT, _client.player,
                _client.player.getX(), _client.player.getY(), _client.player.getZ(),
                _client.player.getBoundingBox().expand(128F));
    }

    // Target sounds
    public void playSoundOnFollow(){ _client.player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_TURTLE.value(), 1.0F, 0.8F); }
    public void playSoundOnLock(){ _client.player.playSound(SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0F, 1.2F); }

    // Target states
    public void setState(ETargetState state) {
        if (getState() == state) return;

        target.currentState = state;
        target.justFollowing = getState() == ETargetState.TARGET_FOLLOW;
        target.justLocked = getState() == ETargetState.TARGET_LOCKED;
    }
    public ETargetState getState() { return target.currentState; }

    public boolean isIdling() { return getState() == ETargetState.TARGET_IDLE; }
    public boolean isFollowing() { return getState() == ETargetState.TARGET_FOLLOW; }
    public boolean isLocked() { return getState() == ETargetState.TARGET_LOCKED; }

    public boolean wasFollowing(){
        if(target.justFollowing) {
            resetLockTime();
            target.justFollowing = false;
            return true;
        }
        return false;
    }
    public boolean wasLocked(){
        if(target.justLocked) {
            resetLockTime();
            target.justLocked = false;
            return true;
        }
        return false;
    }

    // Projects entity world position on player's screen
    public void projectEntityPositionOnScreen(){
        if(target.targetEntity == null) return;


        Vec3d cameraPos = _client.gameRenderer.getCamera().getPos();
        Vec3d entityWorldPos = target.targetEntity
                .getLerpedPos(RenderTickCounter.ONE.getTickDelta(true))
                .add(0, target.targetEntity.getHeight() / 2.0, 0);

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

        // Calculate target position on screen
        updateTargetPosition(
                (int) ( ((local.x * (f/aspect) / -local.z) * 0.5 + 0.5) * (screenW - target.targetSize) ),
                (int) ( ((-local.y * f / -local.z) * 0.5 + 0.5) * (screenH - target.targetSize) )  );
    }
        // Checks if entity is on player's screen
    public boolean isInView(){
        Entity entity = target.targetEntity;
        if(entity == null) return false;

        Vec3d toEntity = entity.getPos().subtract(_client.player.getCameraPosVec(RenderTickCounter.ONE.getTickDelta(true))).normalize();
        Vec3d dir = _client.player.getRotationVec(RenderTickCounter.ONE.getTickDelta(true)).normalize();

        // Dot product shows how directionally aligned vectors are
        return toEntity.dotProduct(dir) > IN_VIEW_THRESHOLD;
    }

    // Lock logic
    public int getLockTime(){ return target.lockTime; }
    public void resetLockTime(){ target.lockTime = 40; }

    public boolean tryLockOnTarget(boolean _isCrossBowCharged, boolean _isInLockView){
        if (target.targetEntity == null) {
            resetTarget();
            return false;
        }

        if (!_isInLockView || !_isCrossBowCharged || isLocked()) {
            resetLockTime();
            return false;
        }

        if (target.lockTime > 0) {
            target.lockTime -= 1;
            return false;
        }

        resetLockTime();
        return true;
    }
        // Same as isInView but FOV is narrower
    public boolean isInLockView(){
        Entity entity = target.targetEntity;
        if(entity == null) return false;

        Vec3d toEntity = entity.getPos().subtract(_client.player.getCameraPosVec(RenderTickCounter.ONE.getTickDelta(true))).normalize();
        Vec3d dir = _client.player.getRotationVec(RenderTickCounter.ONE.getTickDelta(true)).normalize();

        // Dot product shows how directionally aligned vectors are
        return toEntity.dotProduct(dir) > IN_LOCK_THRESHOLD;
    }
}
