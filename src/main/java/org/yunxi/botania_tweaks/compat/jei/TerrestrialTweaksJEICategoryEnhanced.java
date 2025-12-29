package org.yunxi.botania_tweaks.compat.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.yunxi.botania_tweaks.BotaniaTweaks;
import org.yunxi.botania_tweaks.api.util.MultiblockStructure;
import org.yunxi.botania_tweaks.data.recipes.TerrestrialTweaksRecipe;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.BotaniaBlocks;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("removal")
public class TerrestrialTweaksJEICategoryEnhanced implements IRecipeCategory<TerrestrialTweaksRecipe> {

    public static final RecipeType<TerrestrialTweaksRecipe> RECIPE_TYPE =
            RecipeType.create(BotaniaTweaks.MODID, "terrestrial_tweaks", TerrestrialTweaksRecipe.class);

    // GUI Dimensions
    private static final int WIDTH = 170;
    private static final int HEIGHT = 160;
    private static final int CENTER_X = WIDTH / 2;

    // Structure Display Constants
    private static final int STRUCTURE_Y = 105;
    private static final int SINGLE_STRUCTURE_X = CENTER_X;

    private static final int DUAL_INPUT_X = 35;
    private static final int DUAL_OUTPUT_X = 135;

    private static final int HITBOX_SIZE = 60;

    private final IDrawable background;
    private final IDrawable overlay;
    private final IDrawable arrow;
    private final IDrawable icon;
    private final Component title;

    // Cache
    private static final Map<MultiblockStructure, Map<BlockPos, Block>> structureCache = new WeakHashMap<>();

    // Rotation Control
    private float inputRotationX = 30.0f;
    private float inputRotationY = -45.0f;
    private float outputRotationX = 30.0f;
    private float outputRotationY = -45.0f;

    // Interaction State
    private double lastMouseX = -1;
    private double lastMouseY = -1;
    private boolean isDragging = false;
    private boolean draggingInput = false;

    // Selection State
    private BlockPos selectedBlockPos = null;
    private Block selectedBlock = null;
    private boolean isInputStructure = true;

    // Rendering State
    private boolean currentlyDrawingInput = true;

    public TerrestrialTweaksJEICategoryEnhanced(IGuiHelper guiHelper) {
        ResourceLocation location = new ResourceLocation("botania", "textures/gui/terrasteel_jei_overlay.png");
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.overlay = guiHelper.createDrawable(location, 42, 29, 64, 64);

        ResourceLocation arrowLocation = new ResourceLocation(BotaniaTweaks.MODID, "textures/gui/schedule.png");
        this.arrow = guiHelper.createDrawable(arrowLocation, 0, 0, 19, 15);

        this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(BotaniaBlocks.terraPlate)
        );
        this.title = Component.translatable("jei.botania_tweaks.terrestrial_tweaks");
    }

    @Override
    public @NotNull RecipeType<TerrestrialTweaksRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TerrestrialTweaksRecipe recipe, IFocusGroup focuses) {
        resetRotation();

        List<Ingredient> ingredients = recipe.getIngredients();

        // 物品布局
        int itemCenterX = CENTER_X;
        int itemCenterY = 37;
        int radius = 32;

        double angleBetweenEach = 360.0 / ingredients.size();
        Vec2 point = new Vec2(itemCenterX, itemCenterY - radius);
        Vec2 center = new Vec2(itemCenterX, itemCenterY);

        for (Ingredient ingredient : ingredients) {
            builder.addSlot(RecipeIngredientRole.INPUT, (int) point.x - 9, (int) point.y - 9)
                    .addIngredients(ingredient);
            point = rotatePointAbout(point, center, angleBetweenEach);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, itemCenterX - 9, itemCenterY - 9)
                .addItemStack(recipe.getResultItem(RegistryAccess.EMPTY));

        /*builder.addSlot(RecipeIngredientRole.CATALYST, 150, 10)
                .addItemStack(new ItemStack(BotaniaBlocks.terraPlate));*/

        Map<BlockPos, Block> inputStructure = getStructureMap(recipe.getStructure());
        MultiblockStructure outputStructure = recipe.getReplacement();
        boolean hasOutput = outputStructure != null;

        int slotStartY = 125;

        if (hasOutput) {
            addStructureBlockSlotsWithCount(builder, inputStructure, 10, slotStartY, RecipeIngredientRole.INPUT);
            Map<BlockPos, Block> outputStructureMap = getStructureMap(outputStructure);
            addStructureBlockSlotsWithCount(builder, outputStructureMap, WIDTH - 10 - (18*4), slotStartY, RecipeIngredientRole.OUTPUT);
        } else {
            addStructureBlockSlotsWithCount(builder, inputStructure, CENTER_X - (18*2), slotStartY, RecipeIngredientRole.INPUT);
        }
    }

    private void resetRotation() {
        this.inputRotationX = 30.0f;
        this.inputRotationY = -45.0f;
        this.outputRotationX = 30.0f;
        this.outputRotationY = -45.0f;

        // 重置选中状态
        this.selectedBlockPos = null;
        this.selectedBlock = null;
    }

    private void addStructureBlockSlotsWithCount(IRecipeLayoutBuilder builder, Map<BlockPos, Block> structure,
                                                 int startX, int startY, RecipeIngredientRole role) {
        Map<Block, Integer> blockCounts = new HashMap<>();
        for (Block block : structure.values()) {
            blockCounts.merge(block, 1, Integer::sum);
        }

        List<Map.Entry<Block, Integer>> sortedBlocks = new ArrayList<>(blockCounts.entrySet());
        sortedBlocks.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int slotIndex = 0;
        for (Map.Entry<Block, Integer> entry : sortedBlocks) {
            if (slotIndex >= 8) break;

            int x = startX + (slotIndex % 4) * 18;
            int y = startY + (slotIndex / 4) * 18;

            builder.addSlot(role, x, y)
                    .addItemStack(new ItemStack(entry.getKey(), entry.getValue()));

            slotIndex++;
        }
    }

    private static Vec2 rotatePointAbout(Vec2 point, Vec2 center, double degrees) {
        double rad = degrees * Math.PI / 180.0;
        double newX = Math.cos(rad) * (point.x - center.x) - Math.sin(rad) * (point.y - center.y) + center.x;
        double newY = Math.sin(rad) * (point.x - center.x) + Math.cos(rad) * (point.y - center.y) + center.y;
        return new Vec2((float) newX, (float) newY);
    }

    @Override
    public void draw(TerrestrialTweaksRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.enableBlend();

        overlay.draw(guiGraphics, CENTER_X - 32, 5);

        Map<BlockPos, Block> inputStructure = getStructureMap(recipe.getStructure());
        MultiblockStructure replacement = recipe.getReplacement();
        boolean hasOutput = replacement != null;

        handleMouseDrag(mouseX, mouseY, hasOutput);

        if (hasOutput) {
            Map<BlockPos, Block> outputStructureMap = getStructureMap(replacement);

            currentlyDrawingInput = true;
            drawStructure3D(guiGraphics, inputStructure, recipe.getStructure().getSize(),
                    DUAL_INPUT_X, STRUCTURE_Y, inputRotationX, inputRotationY);

            currentlyDrawingInput = false;
            drawStructure3D(guiGraphics, outputStructureMap, replacement.getSize(),
                    DUAL_OUTPUT_X, STRUCTURE_Y, outputRotationX, outputRotationY);

            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            arrow.draw(guiGraphics, CENTER_X - 9, STRUCTURE_Y - 7);
            RenderSystem.enableDepthTest();

        } else {
            currentlyDrawingInput = true;
            drawStructure3D(guiGraphics, inputStructure, recipe.getStructure().getSize(),
                    SINGLE_STRUCTURE_X, STRUCTURE_Y, inputRotationX, inputRotationY);
        }

        if (selectedBlock != null && selectedBlockPos != null) {
            RenderSystem.disableDepthTest();
            // 传递鼠标坐标以检查悬浮
            drawSelectedBlockInfo(guiGraphics, mc, mouseX, mouseY);
            RenderSystem.enableDepthTest();
        }

        int manaBarY = HEIGHT - 12;
        int manaBarX = CENTER_X - 51;
        HUDHandler.renderManaBar(guiGraphics, manaBarX, manaBarY, 0x0000FF, 0.75F, recipe.getMana(), 1000000);

        RenderSystem.disableBlend();
    }

    private void handleMouseDrag(double mouseX, double mouseY, boolean hasOutput) {
        Minecraft mc = Minecraft.getInstance();
        boolean isLeftMouseDown = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (isLeftMouseDown) {
            if (!isDragging) {
                boolean inInput = isMouseOverStructure(mouseX, mouseY, true, hasOutput);
                boolean inOutput = hasOutput && isMouseOverStructure(mouseX, mouseY, false, hasOutput);

                if (inInput || inOutput) {
                    isDragging = true;
                    draggingInput = inInput;
                    lastMouseX = mouseX;
                    lastMouseY = mouseY;
                }
            } else {
                double deltaX = mouseX - lastMouseX;
                double deltaY = mouseY - lastMouseY;

                if (draggingInput) {
                    inputRotationY += (float) deltaX * 0.5f;
                    inputRotationX += (float) deltaY * 0.5f;
                } else {
                    outputRotationY += (float) deltaX * 0.5f;
                    outputRotationX += (float) deltaY * 0.5f;
                }

                lastMouseX = mouseX;
                lastMouseY = mouseY;
            }
        } else {
            isDragging = false;
        }
    }

    private boolean isMouseOverStructure(double mouseX, double mouseY, boolean checkInput, boolean hasOutput) {
        int targetX;
        if (hasOutput) {
            targetX = checkInput ? DUAL_INPUT_X : DUAL_OUTPUT_X;
        } else {
            if (!checkInput) return false;
            targetX = SINGLE_STRUCTURE_X;
        }

        return mouseX >= targetX - HITBOX_SIZE/2.0 && mouseX <= targetX + HITBOX_SIZE/2.0 &&
                mouseY >= STRUCTURE_Y - HITBOX_SIZE/2.0 && mouseY <= STRUCTURE_Y + HITBOX_SIZE/2.0;
    }

    private void drawStructure3D(GuiGraphics guiGraphics, Map<BlockPos, Block> structure,
                                 int size, int centerX, int centerY,
                                 float rotX, float rotY) {

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        poseStack.translate(centerX, centerY, 100);

        float scale = switch (size) {
            case 3 -> 12.0f;
            case 5 -> 9.0f;
            case 7 -> 7.0f;
            default -> 10.0f;
        };
        poseStack.scale(scale, -scale, scale);

        poseStack.mulPose(new Quaternionf().rotationXYZ(
                (float) Math.toRadians(rotX),
                (float) Math.toRadians(rotY),
                0
        ));

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(0, 1, 0);
        poseStack.translate(-0.5, -0.5, -0.5);
        try {
            blockRenderer.renderSingleBlock(
                    BotaniaBlocks.terraPlate.defaultBlockState(),
                    poseStack,
                    bufferSource,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    null
            );
        } catch (Exception ignored) {}
        poseStack.popPose();

        if (!structure.isEmpty()) {
            List<Map.Entry<BlockPos, Block>> sortedEntries = new ArrayList<>(structure.entrySet());
            sortedEntries.sort(Comparator.comparingInt(e -> e.getKey().getY()));

            for (Map.Entry<BlockPos, Block> entry : sortedEntries) {
                BlockPos pos = entry.getKey();
                Block block = entry.getValue();
                BlockState state = block.defaultBlockState();

                poseStack.pushPose();

                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                poseStack.translate(-0.5, -0.5, -0.5);

                if (selectedBlockPos != null && pos.equals(selectedBlockPos) &&
                        currentlyDrawingInput == isInputStructure) {
                    poseStack.translate(0.5, 0.5, 0.5);
                    poseStack.scale(1.1f, 1.1f, 1.1f);
                    poseStack.translate(-0.5, -0.5, -0.5);
                }

                try {
                    blockRenderer.renderSingleBlock(
                            state,
                            poseStack,
                            bufferSource,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            ModelData.EMPTY,
                            null
                    );
                } catch (Exception ignored) {}

                poseStack.popPose();
            }
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }

    /**
     * 绘制选中方块的物品，悬浮显示坐标
     */
    private void drawSelectedBlockInfo(GuiGraphics guiGraphics, Minecraft mc, double mouseX, double mouseY) {
        // 物品显示在左上角 (10, 10)
        int itemX = 10;
        int itemY = 10;

        if (selectedBlock != null) {
            ItemStack stack = new ItemStack(selectedBlock);

            // 绘制物品
            guiGraphics.renderItem(stack, itemX, itemY);
            guiGraphics.renderItemDecorations(mc.font, stack, itemX, itemY);

            // 检查鼠标是否悬浮在物品上
            if (mouseX >= itemX && mouseX <= itemX + 16 && mouseY >= itemY && mouseY <= itemY + 16) {
                List<Component> tooltip = new ArrayList<>();
                // 1. 物品名称
                tooltip.add(stack.getHoverName());
                // 2. 坐标信息
                tooltip.add(Component.translatable("jei.botania_tweaks.structure_tooltip1", selectedBlockPos.getX(), selectedBlockPos.getY() - 1, selectedBlockPos.getZ())
                        .withStyle(ChatFormatting.GOLD));

                guiGraphics.renderTooltip(mc.font, tooltip, Optional.empty(), (int) mouseX, (int) mouseY);
            }
        }
    }

    @Override
    public boolean handleInput(TerrestrialTweaksRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            boolean hasOutput = recipe.getReplacement() != null;

            BlockPos pickedInput = getBlockAtMouse(mouseX, mouseY, getStructureMap(recipe.getStructure()), recipe.getStructure().getSize(),
                    hasOutput ? DUAL_INPUT_X : SINGLE_STRUCTURE_X, STRUCTURE_Y, inputRotationX, inputRotationY);

            if (pickedInput != null) {
                selectedBlockPos = pickedInput;
                selectedBlock = getStructureMap(recipe.getStructure()).get(pickedInput);
                isInputStructure = true;
                return true;
            }

            if (hasOutput) {
                BlockPos pickedOutput = getBlockAtMouse(mouseX, mouseY, getStructureMap(recipe.getReplacement()), recipe.getReplacement().getSize(),
                        DUAL_OUTPUT_X, STRUCTURE_Y, outputRotationX, outputRotationY);
                if (pickedOutput != null) {
                    selectedBlockPos = pickedOutput;
                    selectedBlock = getStructureMap(recipe.getReplacement()).get(pickedOutput);
                    isInputStructure = false;
                    return true;
                }
            }

            if (isMouseOverStructure(mouseX, mouseY, true, hasOutput) ||
                    (hasOutput && isMouseOverStructure(mouseX, mouseY, false, hasOutput))) {
                // 点击空白处不操作
            }
        }
        return false;
    }

    private BlockPos getBlockAtMouse(double mouseX, double mouseY, Map<BlockPos, Block> structure, int size, int centerX, int centerY, float rotX, float rotY) {
        float scale = switch (size) {
            case 3 -> 12.0f;
            case 5 -> 9.0f;
            case 7 -> 7.0f;
            default -> 10.0f;
        };

        Quaternionf rotation = new Quaternionf().rotationXYZ(
                (float) Math.toRadians(rotX),
                (float) Math.toRadians(rotY),
                0
        );

        BlockPos bestPos = null;
        double bestZ = -Double.MAX_VALUE;
        double maxDistSq = (scale * 0.65) * (scale * 0.65);

        for (BlockPos pos : structure.keySet()) {
            Vector3f vec = new Vector3f(pos.getX(), pos.getY(), pos.getZ());
            vec.rotate(rotation);

            float screenX = centerX + vec.x * scale;
            float screenY = centerY + vec.y * -scale;
            float screenZ = vec.z * scale;

            double distSq = Math.pow(mouseX - screenX, 2) + Math.pow(mouseY - screenY, 2);

            if (distSq < maxDistSq) {
                if (screenZ > bestZ) {
                    bestZ = screenZ;
                    bestPos = pos;
                }
            }
        }
        return bestPos;
    }

    @Override
    public List<Component> getTooltipStrings(TerrestrialTweaksRecipe recipe, IRecipeSlotsView recipeSlotsView,
                                             double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        boolean hasOutput = recipe.getReplacement() != null;

        if (isMouseOverStructure(mouseX, mouseY, true, hasOutput) ||
                (hasOutput && isMouseOverStructure(mouseX, mouseY, false, hasOutput))) {
            tooltip.add(Component.translatable("jei.botania_tweaks.structure_tooltip"));
        }

        return tooltip;
    }

    @SuppressWarnings("unchecked")
    private Map<BlockPos, Block> getStructureMap(MultiblockStructure structure) {
        return structureCache.computeIfAbsent(structure, s -> {
            try {
                Field field = MultiblockStructure.class.getDeclaredField("structureMap");
                field.setAccessible(true);
                return (Map<BlockPos, Block>) field.get(s);
            } catch (Exception e) {
                BotaniaTweaks.LOGGER.error("Failed to access structureMap", e);
                return new HashMap<>();
            }
        });
    }
}