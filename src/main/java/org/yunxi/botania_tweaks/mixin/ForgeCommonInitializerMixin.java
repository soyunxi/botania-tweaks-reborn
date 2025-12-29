package org.yunxi.botania_tweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vazkii.botania.common.block.block_entity.mana.PowerGeneratorBlockEntity;
import vazkii.botania.forge.ForgeCommonInitializer;

@Mixin(ForgeCommonInitializer.class)
public class ForgeCommonInitializerMixin {

    // 保持和另一个 Mixin 里一样的数值
    private static final int NEW_MAX_ENERGY = 256000;

    /**
     * 拦截 attachBeCaps 方法中的 CapabilityUtil.makeProvider 调用。
     * 我们的目标是替换掉那个只能存 12800 FE 的 IEnergyStorage。
     */
    @WrapOperation(
            method = "attachBeCaps",
            at = @At(
                    value = "INVOKE",
                    target = "Lvazkii/botania/forge/CapabilityUtil;makeProvider(Lnet/minecraftforge/common/capabilities/Capability;Ljava/lang/Object;)Lnet/minecraftforge/common/capabilities/ICapabilityProvider;"
            ),
            remap = false
    )
    private ICapabilityProvider replaceEnergyStorage(
            Capability<?> capability,
            Object instance,
            Operation<ICapabilityProvider> original,
            // 使用 @Local(argsOnly = true) 稳健地获取外层方法(attachBeCaps)的参数
            @Local(argsOnly = true) AttachCapabilitiesEvent<BlockEntity> event
    ) {
        BlockEntity be = event.getObject(); // 获取 BlockEntity

        // 1. 检查是否是 "Forge Energy" 能力
        // 2. 检查是否是 "魔力通量发生器" 方块实体
        // 3. 确保 instance 确实是 IEnergyStorage 类型 (原版逻辑生成的匿名对象)
        if (capability == ForgeCapabilities.ENERGY
                && be instanceof PowerGeneratorBlockEntity
                && instance instanceof IEnergyStorage originalStorage) {

            // 使用“装饰器模式” (Wrapper Pattern)
            // 我们不完全重写逻辑，而是创建一个“代理”，只修改 getMaxEnergyStored，
            // 其他所有方法直接委托给原版对象 (originalStorage)。
            // 这样如果原版逻辑以后更新了（比如允许接收能量），我们的代码也不会破坏它。
            IEnergyStorage wrappedStorage = new IEnergyStorage() {
                @Override
                public int getMaxEnergyStored() {
                    return NEW_MAX_ENERGY; // <--- 只有这里是我们修改的！
                }

                // --- 以下所有方法都直接调用原版逻辑 ---

                @Override
                public int getEnergyStored() {
                    return originalStorage.getEnergyStored();
                }

                @Override
                public boolean canExtract() {
                    return originalStorage.canExtract();
                }

                @Override
                public boolean canReceive() {
                    return originalStorage.canReceive();
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    return originalStorage.extractEnergy(maxExtract, simulate);
                }

                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    return originalStorage.receiveEnergy(maxReceive, simulate);
                }
            };

            // 使用我们的 wrappedStorage 替代原版的 instance 传给 makeProvider
            return original.call(capability, wrappedStorage);
        }

        // 对于其他情况（比如 ItemHandler, Exoflame 等），保持原样
        return original.call(capability, instance);
    }
}
