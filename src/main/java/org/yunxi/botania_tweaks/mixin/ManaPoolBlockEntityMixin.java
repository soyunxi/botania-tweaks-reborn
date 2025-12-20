package org.yunxi.botania_tweaks.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.yunxi.botania_tweaks.common.config.BotaniaTweaksConfig;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.mana.ManaPoolBlock;

@Mixin(value = ManaPoolBlockEntity.class, remap = false)
public abstract class ManaPoolBlockEntityMixin extends BlockEntity {

    public ManaPoolBlockEntityMixin(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    @ModifyConstant(method = "getCurrentMana", constant = @Constant(intValue = 1000000), remap = false)
    private int modifyAllMaxMana(int original) {
        return Integer.MAX_VALUE;
    }

    @Inject(method = "getMaxMana", at = @At("RETURN"), cancellable = true)
    public void getMaxMana(CallbackInfoReturnable<Integer> cir) {

        switch (((ManaPoolBlock) getBlockState().getBlock()).variant) {
            case DILUTED -> cir.setReturnValue(BotaniaTweaksConfig.Server.getMaxDilutedManaPool());
            case DEFAULT -> cir.setReturnValue(BotaniaTweaksConfig.Server.getMaxManaPool());
            case FABULOUS -> cir.setReturnValue(BotaniaTweaksConfig.Server.getMaxFabulousManaPool());
            case CREATIVE -> cir.setReturnValue(BotaniaTweaksConfig.Server.getEverlastingGuiltyPool());
        }

    }

}
