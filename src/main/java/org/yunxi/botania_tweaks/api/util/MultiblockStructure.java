package org.yunxi.botania_tweaks.api.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.*;

@SuppressWarnings("removal")
public class MultiblockStructure {
    private final Map<BlockPos, Block> structureMap;
    private final int size;

    public MultiblockStructure(Map<BlockPos, Block> structureMap, int size) {
        this.structureMap = structureMap;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean matches(Level level, BlockPos centerPos) {
        for (int i = 0; i < 4; i++) {
            if (checkRotation(level, centerPos, i)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesWithOutputCheck(Level level, BlockPos centerPos, int outputSize) {
        boolean inputMatches = false;
        for (int i = 0; i < 4; i++) {
            if (checkRotation(level, centerPos, i)) {
                inputMatches = true;
                break;
            }
        }

        if (!inputMatches) {
            return false;
        }

        return checkOutputSpace(level, centerPos, outputSize);
    }

    private boolean checkOutputSpace(Level level, BlockPos centerPos, int outputSize) {
        int halfOutputSize = outputSize / 2;
        int halfInputSize = size / 2;

        for (int x = -halfOutputSize; x <= halfOutputSize; x++) {
            for (int z = -halfOutputSize; z <= halfOutputSize; z++) {
                if (Math.abs(x) <= halfInputSize && Math.abs(z) <= halfInputSize) {
                    continue;
                }

                BlockPos checkPos = centerPos.offset(x, 0, z);
                BlockState state = level.getBlockState(checkPos);
                if (!state.isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkRotation(Level level, BlockPos centerPos, int rotation) {
        for (Map.Entry<BlockPos, Block> entry : structureMap.entrySet()) {
            BlockPos rotatedOffset = rotate(entry.getKey(), rotation);
            BlockPos targetPos = centerPos.offset(rotatedOffset);
            BlockState state = level.getBlockState(targetPos);

            if (!state.is(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private BlockPos rotate(BlockPos pos, int rotation) {
        int x = pos.getX();
        int z = pos.getZ();
        return switch (rotation) {
            case 1 -> new BlockPos(-z, pos.getY(), x);
            case 2 -> new BlockPos(-x, pos.getY(), -z);
            case 3 -> new BlockPos(z, pos.getY(), -x);
            default -> pos;
        };
    }

    public void placeBlocks(Level level, BlockPos centerPos) {
        for (Map.Entry<BlockPos, Block> entry : structureMap.entrySet()) {
            BlockPos targetPos = centerPos.offset(entry.getKey());
            BlockState oldState = level.getBlockState(targetPos);
            level.setBlock(targetPos, entry.getValue().defaultBlockState(), 3);
            level.levelEvent(2001, targetPos, Block.getId(oldState));
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, targetPos);
        }
    }

    /**
     * 从JSON加载结构
     */
    public static MultiblockStructure fromJson(JsonObject json) {
        Map<Character, Block> keyMap = new HashMap<>();
        if (json.has("key")) {
            JsonObject keyObj = json.getAsJsonObject("key");
            for (Map.Entry<String, JsonElement> entry : keyObj.entrySet()) {
                String k = entry.getKey();
                if (k.length() != 1) throw new JsonSyntaxException("无效的键: " + k);
                ResourceLocation rl = new ResourceLocation(entry.getValue().getAsString());
                Block block = BuiltInRegistries.BLOCK.get(rl);
                keyMap.put(k.charAt(0), block);
            }
        }

        Map<BlockPos, Block> structure = new HashMap<>();
        JsonArray pattern = json.getAsJsonArray("pattern");

        if (pattern.isEmpty()) {
            throw new JsonSyntaxException("结构模式不能为空");
        }

        int structureSize = processLayer(structure, keyMap, pattern);

        if (structureSize != 3 && structureSize != 5 && structureSize != 7) {
            throw new JsonSyntaxException("结构大小必须为3x3、5x5或7x7，发现错误: " + structureSize + "x" + structureSize);
        }

        return new MultiblockStructure(structure, structureSize);
    }

    private static int processLayer(Map<BlockPos, Block> structure, Map<Character, Block> keyMap, JsonArray rows) {
        int height = rows.size();
        int maxWidth = 0;

        for (int z = 0; z < height; z++) {
            String row = rows.get(z).getAsString();
            if (row.length() > maxWidth) {
                maxWidth = row.length();
            }
        }

        if (height != maxWidth) {
            throw new JsonSyntaxException("结构必须是正方形，发现错误: " + maxWidth + "x" + height);
        }

        int midZ = height / 2;
        int midX = maxWidth / 2;

        for (int z = 0; z < height; z++) {
            String row = rows.get(z).getAsString();
            int width = row.length();

            for (int x = 0; x < width; x++) {
                char c = row.charAt(x);
                if (c != ' ' && keyMap.containsKey(c)) {
                    structure.put(new BlockPos(x - midX, 0, z - midZ), keyMap.get(c));
                }
            }
        }

        return height;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(size);
        buffer.writeVarInt(structureMap.size());
        for (Map.Entry<BlockPos, Block> entry : structureMap.entrySet()) {
            buffer.writeBlockPos(entry.getKey());
            buffer.writeId(BuiltInRegistries.BLOCK, entry.getValue());
        }
    }

    public static MultiblockStructure fromNetwork(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        int mapSize = buffer.readVarInt();
        Map<BlockPos, Block> structure = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            BlockPos pos = buffer.readBlockPos();
            Block block = buffer.readById(BuiltInRegistries.BLOCK);
            structure.put(pos, block);
        }
        return new MultiblockStructure(structure, size);
    }
}