package org.yunxi.botania_tweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.yunxi.botania_tweaks.common.config.BotaniaTweaksConfig;
import vazkii.botania.api.mana.spark.SparkHelper;

@Mixin(value = SparkHelper.class, remap = false)
public class SparkHelperMixin {
    @ModifyConstant(method = "getSparksAround", constant = @Constant(intValue = 12))
    private static int modifySparkScanRange(int constantValue) {
        return BotaniaTweaksConfig.Server.getSparkScanRange();
    }
}
