package org.yunxi.botania_tweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.common.block.block_entity.mana.PowerGeneratorBlockEntity;

@Mixin(value = PowerGeneratorBlockEntity.class, remap = false)
public class PowerGeneratorBlockEntityMixin {
    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private static int serverTick(int a, int b) {
        return Math.min(a, 100000);
    }

    // 设定你想要的新最大能量值 (例如：原来的20倍)
    @Unique
    private static final int NEW_MAX_ENERGY = 256000;

    /**
     * 使用 @WrapOperation 拦截对 MAX_ENERGY 字段的读取。
     * * method: 指定在哪些方法里拦截 (isFull 和 receiveMana 都用到了这个字段)
     * at: 指定拦截的位置。
     * value = "FIELD": 拦截字段访问
     * target: 目标字段的全路径 (类名;字段名:类型)
     * opcode = Opcodes.GETSTATIC: 因为 MAX_ENERGY 是 static 的，所以拦截 GETSTATIC
     */
    @WrapOperation(
            method = {"isFull", "receiveMana"},
            at = @At(
                    value = "FIELD",
                    target = "Lvazkii/botania/common/block/block_entity/mana/PowerGeneratorBlockEntity;MAX_ENERGY:I",
                    opcode = Opcodes.GETSTATIC
            ),
            remap = false // 因为是修改 Botania (模组)，通常不需要 remap。如果是修改原版 MC，则为 true
    )
    private int modifyMaxEnergy(Operation<Integer> original) {
        return NEW_MAX_ENERGY;

    }
}
