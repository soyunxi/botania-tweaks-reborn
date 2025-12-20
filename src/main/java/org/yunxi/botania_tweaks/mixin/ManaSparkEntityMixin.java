package org.yunxi.botania_tweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.yunxi.botania_tweaks.common.config.BotaniaTweaksConfig;
import vazkii.botania.common.entity.ManaSparkEntity;

@Mixin(value = ManaSparkEntity.class, remap = false)
public class ManaSparkEntityMixin {
    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 12), remap = true)
    private double modifyChargingRange(double constant) {
        return BotaniaTweaksConfig.Server.getSparkScanRange();
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 1000))
    private int modifyTransferRate(int constant) {
        return BotaniaTweaksConfig.Server.getSparkTransferRate();
    }
}