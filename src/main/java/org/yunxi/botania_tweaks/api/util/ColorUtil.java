package org.yunxi.botania_tweaks.api.util;

/**
 * 颜色渐变工具类    来源：Claude 4.5 Sonnet
 */
public class ColorUtil {
    
    /**
     * 在两个颜色之间进行线性插值（渐变）
     * 
     * @param colorA 起始颜色 (16进制, 例如: 0xFF0000 为红色)
     * @param colorB 结束颜色 (16进制, 例如: 0x00FF00 为绿色)
     * @param progress 进度值 (0.0 ~ 1.0, 0.0 返回 colorA, 1.0 返回 colorB)
     * @return 插值后的颜色值 (16进制)
     */
    public static int lerpColor(int colorA, int colorB, float progress) {
        // 限制进度在 0.0 到 1.0 之间
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        
        // 提取 colorA 的 RGB 分量
        int redA = (colorA >> 16) & 0xFF;
        int greenA = (colorA >> 8) & 0xFF;
        int blueA = colorA & 0xFF;
        
        // 提取 colorB 的 RGB 分量
        int redB = (colorB >> 16) & 0xFF;
        int greenB = (colorB >> 8) & 0xFF;
        int blueB = colorB & 0xFF;
        
        // 线性插值每个颜色通道
        int red = (int) (redA + (redB - redA) * progress);
        int green = (int) (greenA + (greenB - greenA) * progress);
        int blue = (int) (blueA + (blueB - blueA) * progress);
        
        // 组合成最终的颜色值
        return (red << 16) | (green << 8) | blue;
    }
    
    /**
     * 带 Alpha 通道的颜色插值（支持透明度）
     * 
     * @param colorA 起始颜色 (ARGB 格式, 例如: 0xFF0000 为不透明红色)
     * @param colorB 结束颜色 (ARGB 格式, 例如: 0x8000FF00 为半透明绿色)
     * @param progress 进度值 (0.0 ~ 1.0)
     * @return 插值后的颜色值 (ARGB 格式)
     */
    public static int lerpColorWithAlpha(int colorA, int colorB, float progress) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        
        // 提取 ARGB 分量
        int alphaA = (colorA >> 24) & 0xFF;
        int redA = (colorA >> 16) & 0xFF;
        int greenA = (colorA >> 8) & 0xFF;
        int blueA = colorA & 0xFF;
        
        int alphaB = (colorB >> 24) & 0xFF;
        int redB = (colorB >> 16) & 0xFF;
        int greenB = (colorB >> 8) & 0xFF;
        int blueB = colorB & 0xFF;
        
        // 插值所有通道
        int alpha = (int) (alphaA + (alphaB - alphaA) * progress);
        int red = (int) (redA + (redB - redA) * progress);
        int green = (int) (greenA + (greenB - greenA) * progress);
        int blue = (int) (blueA + (blueB - blueA) * progress);
        
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
    
    /**
     * 从 RGB 分量创建颜色值
     */
    public static int fromRGB(int red, int green, int blue) {
        return ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }
    
    /**
     * 从 ARGB 分量创建颜色值
     */
    public static int fromARGB(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }
    
    /**
     * 提取红色分量
     */
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }
    
    /**
     * 提取绿色分量
     */
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }
    
    /**
     * 提取蓝色分量
     */
    public static int getBlue(int color) {
        return color & 0xFF;
    }
    
    /**
     * 提取 Alpha 分量
     */
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }
}
