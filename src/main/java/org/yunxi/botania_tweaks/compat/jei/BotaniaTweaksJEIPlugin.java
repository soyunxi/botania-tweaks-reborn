package org.yunxi.botania_tweaks.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;
import org.yunxi.botania_tweaks.BotaniaTweaks;
import org.yunxi.botania_tweaks.data.recipes.TerrestrialTweaksRecipe;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.List;

@SuppressWarnings("removal")
@JeiPlugin
public class BotaniaTweaksJEIPlugin implements IModPlugin {

    private static final ResourceLocation ID = new ResourceLocation(BotaniaTweaks.MODID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new TerrestrialTweaksJEICategoryEnhanced(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        RecipeManager recipeManager = minecraft.level.getRecipeManager();

        // 获取所有 TerrestrialTweaksRecipe
        List<TerrestrialTweaksRecipe> recipes = recipeManager
                .getAllRecipesFor(BotaniaTweaks.TERRESTRIAL_TWEAKS_TYPE.get());

        // 注册配方
        registration.addRecipes(TerrestrialTweaksJEICategoryEnhanced.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(mezz.jei.api.registration.IRecipeCatalystRegistration registration) {
        // 添加催化剂（凝聚板）
        registration.addRecipeCatalyst(
                new ItemStack(BotaniaBlocks.terraPlate),
                TerrestrialTweaksJEICategoryEnhanced.RECIPE_TYPE
        );
    }
}