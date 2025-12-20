package org.yunxi.botania_tweaks.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.yunxi.botania_tweaks.api.ITerraPlateColorProvider;
import org.yunxi.botania_tweaks.api.util.ColorUtil;
import org.yunxi.botania_tweaks.data.recipes.TerrestrialTweaksRecipe;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntity;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.network.EffectType;
import vazkii.botania.network.clientbound.BotaniaEffectPacket;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.ArrayList;
import java.util.List;

import static org.yunxi.botania_tweaks.api.util.BotaniaTweaksRecipeUtil.getTerrestrialTweaksRecipe;

@Mixin(value = TerrestrialAgglomerationPlateBlockEntity.class, remap = false)
public abstract class TerrestrialAgglomerationPlateBlockEntityMixin extends BotaniaBlockEntity
        implements ITerraPlateColorProvider {

    @Unique
    private int botaniatweaks$color1 = 0x00FF00; // 默认绿色

    @Unique
    private int botaniatweaks$color2 = 0x00FF00;

    @Unique
    private boolean botaniatweaks$hasCustomRecipe = false;

    public TerrestrialAgglomerationPlateBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow
    public abstract int getCurrentMana();

    @Shadow
    private static ItemStack[] flattenStacks(List<ItemStack> items) {
        return null;
    }

    @Shadow
    protected abstract SimpleContainer getInventory(List<ItemEntity> itemEntities);

    @Override
    public int botaniatweaks$getColor1() {
        return botaniatweaks$color1;
    }

    @Override
    public int botaniatweaks$getColor2() {
        return botaniatweaks$color2;
    }

    @Override
    public boolean botaniatweaks$hasCustomRecipe() {
        return botaniatweaks$hasCustomRecipe;
    }

    @Override
    public void botaniatweaks$setColors(int color1, int color2, boolean hasCustomRecipe) {
        this.botaniatweaks$color1 = color1;
        this.botaniatweaks$color2 = color2;
        this.botaniatweaks$hasCustomRecipe = hasCustomRecipe;
    }

    @Unique
    private static TerrestrialTweaksRecipe botaniatweaks$findCustomRecipe(Level level, BlockPos pos) {
        return getTerrestrialTweaksRecipe(level, pos);
    }

    // --- 修复 Spark 无法输送魔力的问题 ---

    @Inject(method = "getCurrentRecipe", at = @At("RETURN"), cancellable = true)
    private void onGetCurrentRecipe(SimpleContainer items, CallbackInfoReturnable<TerrestrialAgglomerationRecipe> cir) {
        if (!items.isEmpty()) {
            cir.setReturnValue(botaniatweaks$findCustomRecipe(level, worldPosition));
        }
    }

    @Inject(method = "canReceiveManaFromBursts", at = @At("HEAD"), cancellable = true)
    private void onCanReceiveMana(CallbackInfoReturnable<Boolean> cir) {
        if (botaniatweaks$findCustomRecipe(this.getLevel(), this.getBlockPos()) != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isFull", at = @At("HEAD"), cancellable = true)
    private void onIsFull(CallbackInfoReturnable<Boolean> cir) {
        TerrestrialTweaksRecipe recipe = botaniatweaks$findCustomRecipe(this.getLevel(), this.getBlockPos());
        if (recipe != null) {
            cir.setReturnValue(this.getCurrentMana() >= recipe.getMana());
        }
    }

    @Inject(method = "getAvailableSpaceForMana", at = @At("HEAD"), cancellable = true)
    private void onGetAvailableSpace(CallbackInfoReturnable<Integer> cir) {
        TerrestrialTweaksRecipe recipe = botaniatweaks$findCustomRecipe(this.getLevel(), this.getBlockPos());
        if (recipe != null) {
            cir.setReturnValue(Math.max(0, recipe.getMana() - this.getCurrentMana()));
        }
    }

    @Inject(method = "areIncomingTranfersDone", at = @At("HEAD"), cancellable = true)
    private void onTransfersDone(CallbackInfoReturnable<Boolean> cir) {
        if (botaniatweaks$findCustomRecipe(this.getLevel(), this.getBlockPos()) != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getCompletion", at = @At("HEAD"), cancellable = true)
    private void onGetCompletion(CallbackInfoReturnable<Float> cir) {
        TerrestrialTweaksRecipe recipe = botaniatweaks$findCustomRecipe(this.getLevel(), this.getBlockPos());
        if (recipe != null) {
            cir.setReturnValue((float) this.getCurrentMana() / recipe.getMana());
        }
    }

    // --- 核心逻辑接管 ---

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void onServerTick(Level level, BlockPos pos, BlockState state,
                                     TerrestrialAgglomerationPlateBlockEntity self, CallbackInfo ci) {

        TerrestrialTweaksRecipe recipe = botaniatweaks$findCustomRecipe(level, pos);

        if (recipe != null) {
            if (self instanceof ITerraPlateColorProvider provider) {
                provider.botaniatweaks$setColors(
                        recipe.getColor1(),
                        recipe.getColor2(),
                        true
                );
            }

            // 1. Spark 互传逻辑 (原版复制)
            ManaSpark spark = self.getAttachedSpark();
            if (spark != null) {
                var otherSparks = SparkHelper.getSparksAround(level,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        spark.getNetwork());
                for (var otherSpark : otherSparks) {
                    if (spark != otherSpark && otherSpark.getAttachedManaReceiver() instanceof ManaPool) {
                        otherSpark.registerTransfer(spark);
                    }
                }
            }

            // 2. 视觉特效与颜色逻辑
            if (self.getCurrentMana() > 0) {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);

                float progress = (float) self.getCurrentMana() / recipe.getMana();

                // 使用 ColorUtil 进行颜色渐变
                int currentColor = ColorUtil.lerpColor(recipe.getColor1(), recipe.getColor2(), progress);

                // 转换颜色为 0.0-1.0 范围
                float r = ColorUtil.getRed(currentColor) / 255.0f;
                float g = ColorUtil.getGreen(currentColor) / 255.0f;
                float b = ColorUtil.getBlue(currentColor) / 255.0f;

                // 生成彩色粒子效果
                if (level.getRandom().nextInt(2) == 0) {
                    double x = pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.9;
                    double y = pos.getY() + 0.15;
                    double z = pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.9;

                    level.addParticle(
                            new DustParticleOptions(new Vector3f(r, g, b), 1.5f),
                            x, y, z,
                            0.0, 0.05, 0.0
                    );
                }

                // 发送原版特效包(客户端 Mixin 会拦截并使用自定义颜色)
                int proportion = Float.floatToIntBits(progress);
                XplatAbstractions.INSTANCE.sendToNear(level, pos,
                        new BotaniaEffectPacket(EffectType.TERRA_PLATE,
                                pos.getX(), pos.getY(), pos.getZ(), proportion));
            }

            // 3. 完成合成
            if (self.getCurrentMana() >= recipe.getMana()) {
                // 组装物品
                List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class,
                        new AABB(pos, pos.offset(1, 1, 1)));
                List<ItemStack> items = new ArrayList<>();
                for (ItemEntity entity : itemEntities) {
                    if (!entity.getItem().isEmpty()) {
                        items.add(entity.getItem());
                    }
                }
                SimpleContainer inv = new SimpleContainer(items.toArray(new ItemStack[0]));

                ItemStack result = recipe.assemble(inv, level.registryAccess());

                // 消耗物品
                for (ItemEntity entity : itemEntities) {
                    if (!entity.getItem().isEmpty()) {
                        entity.getItem().setCount(0);
                        if (entity.getItem().isEmpty()) {
                            entity.discard();
                        }
                    }
                }

                // 生成产物
                ItemEntity resultEntity = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, result);
                resultEntity.setDeltaMovement(Vec3.ZERO);
                level.addFreshEntity(resultEntity);

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        BotaniaSounds.terrasteelCraft, SoundSource.BLOCKS, 1F, 1F);

                recipe.transformBlocks(level, pos.below());

                // 清空魔力:通过接收负值来实现清零
                self.receiveMana(-self.getCurrentMana());

                // 重置自定义配方标记
                if (self instanceof ITerraPlateColorProvider provider) {
                    provider.botaniatweaks$setColors(0x00FF00, 0x00FF00, false);
                }

                level.updateNeighbourForOutputSignal(pos, state.getBlock());
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);
            }

            // 阻止原版逻辑运行
            ci.cancel();
        } else {
            // 如果没有自定义配方,重置标记
            if (self instanceof ITerraPlateColorProvider provider) {
                provider.botaniatweaks$setColors(0x00FF00, 0x00FF00, false);
            }
        }
    }

    // 保存和加载颜色信息到 NBT
    @Inject(method = "writePacketNBT", at = @At("TAIL"))
    private void onWritePacketNBT(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("botaniatweaks_color1", botaniatweaks$color1);
        tag.putInt("botaniatweaks_color2", botaniatweaks$color2);
        tag.putBoolean("botaniatweaks_hasCustomRecipe", botaniatweaks$hasCustomRecipe);
    }

    @Inject(method = "readPacketNBT", at = @At("TAIL"))
    private void onReadPacketNBT(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("botaniatweaks_color1")) {
            botaniatweaks$color1 = tag.getInt("botaniatweaks_color1");
        }
        if (tag.contains("botaniatweaks_color2")) {
            botaniatweaks$color2 = tag.getInt("botaniatweaks_color2");
        }
        if (tag.contains("botaniatweaks_hasCustomRecipe")) {
            botaniatweaks$hasCustomRecipe = tag.getBoolean("botaniatweaks_hasCustomRecipe");
        }
    }
}