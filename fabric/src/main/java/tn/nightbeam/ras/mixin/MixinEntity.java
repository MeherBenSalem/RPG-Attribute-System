package tn.nightbeam.ras.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tn.nightbeam.ras.platform.impl.IEntityData;
import tn.nightbeam.ras.network.PlayerVariables;

@Mixin(Entity.class)
public class MixinEntity implements IEntityData {
    private final PlayerVariables playerVariables = new PlayerVariables();

    @Override
    public PlayerVariables getPlayerVariables() {
        return playerVariables;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.put("RPGVars", playerVariables.writeNBT());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("RPGVars")) {
            playerVariables.readNBT(tag.get("RPGVars"));
        }
    }
}
