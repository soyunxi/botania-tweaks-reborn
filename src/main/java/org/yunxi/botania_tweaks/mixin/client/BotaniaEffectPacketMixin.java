package org.yunxi.botania_tweaks.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yunxi.botania_tweaks.api.ITerraPlateColorProvider;
import org.yunxi.botania_tweaks.api.util.ColorUtil;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;
import vazkii.botania.common.proxy.Proxy;
import vazkii.botania.network.EffectType;
import vazkii.botania.network.clientbound.BotaniaEffectPacket;

/**
 * 来源：Claude 4.5 Sonnet
 */
@Mixin(value = BotaniaEffectPacket.Handler.class, remap = false)
public class BotaniaEffectPacketMixin {

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void onHandle(BotaniaEffectPacket packet, CallbackInfo ci) {
        if (packet.type() != EffectType.TERRA_PLATE) {
            return;
        }
        ci.cancel();

        var x = packet.x();
        var y = packet.y();
        var z = packet.z();
        var args = packet.args();

        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            Level world = mc.level;
            if (world == null) return;

            BlockEntity te = world.getBlockEntity(BlockPos.containing(x, y, z));
            if (!(te instanceof TerrestrialAgglomerationPlateBlockEntity)) {
                return;
            }

            // 使用接口检查是否是自定义配方
            if (!(te instanceof ITerraPlateColorProvider provider)) {
                return;
            }

            if (!provider.botaniatweaks$hasCustomRecipe()) {
                // 不是自定义配方,让原版逻辑处理
                return;
            }

            // 是自定义配方,使用自定义颜色
            float percentage = Float.intBitsToFloat(args[0]);
            int ticks = (int) (100.0 * percentage);

            int totalSpiritCount = 3;
            double tickIncrement = 360D / totalSpiritCount;
            int speed = 5;
            double wticks = ticks * speed - tickIncrement;

            double r = Math.sin((ticks - 100) / 10D) * 2;
            double g = Math.sin(wticks * Math.PI / 180 * 0.55);

            // 获取自定义颜色
            int color1 = provider.botaniatweaks$getColor1();
            int color2 = provider.botaniatweaks$getColor2();
            int currentColor = ColorUtil.lerpColor(color1, color2, percentage);

            float red = ColorUtil.getRed(currentColor) / 255.0f;
            float green = ColorUtil.getGreen(currentColor) / 255.0f;
            float blue = ColorUtil.getBlue(currentColor) / 255.0f;

            for (int i = 0; i < totalSpiritCount; i++) {
                double wx = x + Math.sin(wticks * Math.PI / 180) * r + 0.5;
                double wy = y + 0.25 + Math.abs(r) * 0.7;
                double wz = z + Math.cos(wticks * Math.PI / 180) * r + 0.5;

                wticks += tickIncrement;

                WispParticleData data = WispParticleData.wisp(0.85F, red, green, blue, 0.25F);
                Proxy.INSTANCE.addParticleForceNear(world, data, wx, wy, wz, 0, (float) (-g * 0.05), 0);

                data = WispParticleData.wisp((float) Math.random() * 0.1F + 0.1F, red, green, blue, 0.9F);
                world.addParticle(data, wx, wy, wz,
                        (float) (Math.random() - 0.5) * 0.05F,
                        (float) (Math.random() - 0.5) * 0.05F,
                        (float) (Math.random() - 0.5) * 0.05F);

                if (ticks >= 100) {
                    for (int j = 0; j < 15; j++) {
                        data = WispParticleData.wisp((float) Math.random() * 0.15F + 0.15F, red, green, blue);
                        world.addParticle(data, x + 0.5, y + 0.5, z + 0.5,
                                (float) (Math.random() - 0.5F) * 0.125F,
                                (float) (Math.random() - 0.5F) * 0.125F,
                                (float) (Math.random() - 0.5F) * 0.125F);
                    }
                }
            }
        });
    }
}