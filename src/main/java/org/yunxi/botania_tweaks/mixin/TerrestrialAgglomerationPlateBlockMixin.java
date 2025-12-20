package org.yunxi.botania_tweaks.mixin;


import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.yunxi.botania_tweaks.BotaniaTweaks;
import org.yunxi.botania_tweaks.api.util.BotaniaTweaksRecipeUtil;
import org.yunxi.botania_tweaks.data.recipes.TerrestrialTweaksRecipe;
import vazkii.botania.common.block.BotaniaWaterloggedBlock;
import vazkii.botania.common.block.mana.TerrestrialAgglomerationPlateBlock;

@Mixin(value = TerrestrialAgglomerationPlateBlock.class, remap = false)
public class TerrestrialAgglomerationPlateBlockMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = true)
    private void onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);

        boolean canUse = false;

        for (TerrestrialTweaksRecipe terrestrialTweaksRecipe : BotaniaTweaksRecipeUtil.getTerrestrialTweaksRecipeNoContainer(world, pos)) {
            for (Ingredient ingredient : terrestrialTweaksRecipe.getIngredients()) {
                if (ingredient.test(stack)) {
                    canUse = true;
                    break;
                }
            }
            if (canUse) break;
        }


        if (!stack.isEmpty() && canUse) {
            if (!world.isClientSide) {
                ItemStack target = stack.split(1);
                ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, target);
                item.setPickUpDelay(40);
                item.setDeltaMovement(Vec3.ZERO);
                world.addFreshEntity(item);
            }
            cir.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide()));
        }
    }
}
