package org.yunxi.botania_tweaks.data.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;
import org.yunxi.botania_tweaks.api.util.MultiblockStructure;

public class TerrestrialTweaksRecipeSerializer implements RecipeSerializer<TerrestrialTweaksRecipe> {
    @Override
    public TerrestrialTweaksRecipe fromJson(ResourceLocation p_44103_, JsonObject p_44104_) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        JsonArray ingredientsJson = GsonHelper.getAsJsonArray(p_44104_, "ingredients");
        for (int i = 0; i < ingredientsJson.size(); i++) {
            ingredients.add(Ingredient.fromJson(ingredientsJson.get(i)));
        }

        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(p_44104_, "result"));

        int manaCost = GsonHelper.getAsInt(p_44104_, "mana_cost", 500000);

        int color1 = 0x00FF00; // 默认绿色
        int color2 = 0x00FF00;
        if (p_44104_.has("colors")) {
            JsonObject colors = p_44104_.getAsJsonObject("colors");
            color1 = GsonHelper.getAsInt(colors, "color1", color1);
            color2 = GsonHelper.getAsInt(colors, "color2", color2);
        }

        JsonObject structureJson = GsonHelper.getAsJsonObject(p_44104_, "structure");
        MultiblockStructure multiblock = MultiblockStructure.fromJson(structureJson);

        MultiblockStructure replacement = null;
        if (p_44104_.has("output_structure")) {
            replacement = MultiblockStructure.fromJson(GsonHelper.getAsJsonObject(p_44104_, "output_structure"));
        }

        return new TerrestrialTweaksRecipe(p_44103_, ingredients, result, manaCost, color1, color2, multiblock, replacement);
    }

    @Override
    public @Nullable TerrestrialTweaksRecipe fromNetwork(ResourceLocation p_44105_, FriendlyByteBuf p_44106_) {
        int manaCost = p_44106_.readInt();
        int color1 = p_44106_.readInt();
        int color2 = p_44106_.readInt();

        int ingredientSize = p_44106_.readVarInt();
        NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientSize, Ingredient.EMPTY);
        for (int i = 0; i < ingredientSize; i++) {
            ingredients.set(i, Ingredient.fromNetwork(p_44106_));
        }

        ItemStack result = p_44106_.readItem();

        MultiblockStructure multiblock = MultiblockStructure.fromNetwork(p_44106_);

        boolean hasReplacement = p_44106_.readBoolean();
        MultiblockStructure replacement = hasReplacement ? MultiblockStructure.fromNetwork(p_44106_) : null;

        return new TerrestrialTweaksRecipe(p_44105_, ingredients, result, manaCost, color1, color2, multiblock, replacement);
    }

    @Override
    public void toNetwork(FriendlyByteBuf p_44101_, TerrestrialTweaksRecipe p_44102_) {
        p_44101_.writeInt(p_44102_.getMana());
        p_44101_.writeInt(p_44102_.getColor1());
        p_44101_.writeInt(p_44102_.getColor2());

        p_44101_.writeVarInt(p_44102_.getIngredients().size());
        for (Ingredient ingredient : p_44102_.getIngredients()) {
            ingredient.toNetwork(p_44101_);
        }

        p_44101_.writeItem(p_44102_.getResultItem(null)); // RegistryAccess can be null for simple itemstack serialization

        p_44102_.getStructure().toNetwork(p_44101_);

        p_44101_.writeBoolean(p_44102_.getReplacement() != null);
        if (p_44102_.getReplacement() != null) {
            p_44102_.getReplacement().toNetwork(p_44101_);
        }
    }
}
