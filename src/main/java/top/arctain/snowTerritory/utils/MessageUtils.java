package top.arctain.snowTerritory.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import top.arctain.snowTerritory.config.PluginConfig;

import java.util.logging.Level;

/**
 * 统一的消息工具类
 * 用于美化所有控制台和聊天框输出
 */
public class MessageUtils {

    private static JavaPlugin plugin;
    private static PluginConfig config;
    private static final String CONSOLE_PREFIX = "[SnowTerritory] ";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static boolean consoleBoldEnabled = true;

    /**
     * 初始化消息工具类
     */
    public static void initialize(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    /**
     * 设置配置（用于读取消息）
     */
    public static void setConfig(PluginConfig pluginConfig) {
        config = pluginConfig;
    }
    
    /**
     * 获取消息前缀
     */
    private static String getPrefix() {
        if (config != null) {
            return config.getMessagePrefix();
        }
        return "&{#4a95ff}ST <g:#C97759:#C9AE59>Strengthen</g> &{#4a95ff}&l>&{#99c2ff}&l>&{#ffffff}&l> ";
    }
    
    /**
     * 获取配置的消息，如果不存在则使用默认值
     */
    private static String getMessage(String key, String defaultValue, String... placeholders) {
        String message = defaultValue;
        if (config != null) {
            String configMessage = config.getMessage(key);
            // 如果配置中有消息且不是空字符串，且不等于key（说明找到了配置），则使用配置的消息
            if (configMessage != null && !configMessage.isEmpty() && !configMessage.equals(key) && !configMessage.equals("messages." + key)) {
                message = configMessage;
            }
        }
        
        // 替换占位符
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
            }
        }
        return message;
    }
    
    /**
     * 发送配置消息（从配置文件读取，带前缀）
     */
    public static void sendConfigMessage(CommandSender sender, String key, String defaultValue, String... placeholders) {
        String message = getMessage(key, defaultValue, placeholders);
        sendMessage(sender, message);
    }
    
    /**
     * 发送配置消息（不带前缀）
     */
    public static void sendConfigRaw(CommandSender sender, String key, String defaultValue, String... placeholders) {
        String message = getMessage(key, defaultValue, placeholders);
        sendRaw(sender, message);
    }

    /**
     * 发送成功消息给玩家（使用配置）
     */
    public static void sendSuccess(CommandSender sender, String key, String defaultValue, String... placeholders) {
        sendConfigMessage(sender, key, defaultValue, placeholders);
    }

    /**
     * 发送错误消息给玩家（使用配置）
     */
    public static void sendError(CommandSender sender, String key, String defaultValue, String... placeholders) {
        sendConfigMessage(sender, key, defaultValue, placeholders);
    }

    /**
     * 发送警告消息给玩家（使用配置）
     */
    public static void sendWarning(CommandSender sender, String key, String defaultValue, String... placeholders) {
        sendConfigMessage(sender, key, defaultValue, placeholders);
    }

    /**
     * 发送信息消息给玩家（使用配置）
     */
    public static void sendInfo(CommandSender sender, String key, String defaultValue, String... placeholders) {
        sendConfigMessage(sender, key, defaultValue, placeholders);
    }

    /**
     * 发送普通消息给玩家（带前缀）
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(colorize(getPrefix() + message));
        }
    }

    /**
     * 发送不带前缀的消息
     */
    public static void sendRaw(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(colorize(message));
        }
    }

    /**
     * 发送分隔线
     */
    public static void sendSeparator(CommandSender sender) {
        String separator = getMessage("separator", "&8&m                                        ");
        sendRaw(sender, separator);
    }

    /**
     * 发送标题（带分隔线）
     */
    public static void sendTitle(CommandSender sender, String title) {
        sendSeparator(sender);
        sendRaw(sender, "&6&l" + title);
        sendSeparator(sender);
    }
    
    /**
     * 发送标题（从配置读取）
     */
    public static void sendTitle(CommandSender sender, String key, String defaultTitle) {
        sendSeparator(sender);
        String title = getMessage(key, defaultTitle);
        sendRaw(sender, "&6&l" + title);
        sendSeparator(sender);
    }

    /**
     * 发送帮助信息格式
     */
    public static void sendHelpLine(CommandSender sender, String command, String description) {
        sendRaw(sender, "&e" + command + " &7- &f" + description);
    }
    
    /**
     * 发送帮助信息（从配置读取）
     */
    public static void sendHelp(CommandSender sender) {
        String title = getMessage("help.title", "SnowTerritory 命令帮助");
        sendTitle(sender, title);
        sendConfigRaw(sender, "help.reinforce", "&e/snowterritory reinforce &7- &f打开物品强化界面");
        sendConfigRaw(sender, "help.reload", "&e/snowterritory reload &7- &f重载插件配置");
        sendConfigRaw(sender, "help.checkid", "&e/snowterritory checkID &7- &f查看手中物品的MMOItems ID");
        sendSeparator(sender);
    }

    // ========== 控制台日志方法 ==========

    /**
     * 控制台：信息日志（加粗）
     */
    public static void logInfo(String message) {
        if (plugin != null) {
            plugin.getLogger().info(formatConsoleMessage(message));
        }
    }

    /**
     * 控制台：警告日志（加粗）
     */
    public static void logWarning(String message) {
        if (plugin != null) {
            plugin.getLogger().warning(formatConsoleMessage(message));
        }
    }

    /**
     * 控制台：严重错误日志（加粗）
     */
    public static void logSevere(String message) {
        if (plugin != null) {
            plugin.getLogger().severe(formatConsoleMessage(message));
        }
    }

    /**
     * 控制台：调试日志（加粗）
     */
    public static void logDebug(String message) {
        if (plugin != null) {
            plugin.getLogger().log(Level.FINE, formatConsoleMessage(message));
        }
    }
    
    /**
     * 格式化控制台消息（添加加粗效果）
     */
    private static String formatConsoleMessage(String message) {
        if (consoleBoldEnabled) {
            return ANSI_BOLD + CONSOLE_PREFIX + message + ANSI_RESET;
        }
        return CONSOLE_PREFIX + message;
    }

    /**
     * 控制台：成功消息（绿色）
     */
    public static void logSuccess(String message) {
        logInfo("✓ " + message);
    }

    /**
     * 控制台：错误消息（红色）
     */
    public static void logError(String message) {
        logSevere("✗ " + message);
    }

    // ========== 特殊格式消息 ==========

    /**
     * 发送强化成功消息
     */
    public static void sendReinforceSuccess(Player player, int newLevel) {
        sendSeparator(player);
        sendConfigRaw(player, "reinforce.success-title", "&a&l✓ 强化成功！");
        String levelMsg = getMessage("reinforce.success-level", "&7等级提升至: &a&l+{level}", "level", String.valueOf(newLevel));
        sendRaw(player, levelMsg);
        sendSeparator(player);
    }

    /**
     * 发送强化失败消息
     */
    public static void sendReinforceFail(Player player, int newLevel) {
        sendSeparator(player);
        sendConfigRaw(player, "reinforce.fail-title", "&c&l✗ 强化失败！");
        String levelMsg = getMessage("reinforce.fail-level", "&7等级下降至: &c&l+{level}", "level", String.valueOf(newLevel));
        sendRaw(player, levelMsg);
        sendSeparator(player);
    }

    /**
     * 发送强化维持消息
     */
    public static void sendReinforceMaintain(Player player) {
        sendSeparator(player);
        sendConfigRaw(player, "reinforce.maintain-title", "&e&l⚠ 强化维持不变");
        sendConfigRaw(player, "reinforce.maintain-desc", "&7物品等级未发生变化");
        sendSeparator(player);
    }

    /**
     * 发送物品信息（查看ID命令）
     */
    public static void sendItemInfo(Player player, String type, String id) {
        String title = getMessage("item.info-title", "物品信息");
        sendRaw(player, title);
        sendSeparator(player);
        String typeMsg = getMessage("item.info-type", "&6物品类型: &f{type}", "type", type);
        sendRaw(player, typeMsg);
        String idMsg = getMessage("item.info-id", "&6物品 ID: &e&l{id}", "id", id);
        sendRaw(player, idMsg);
        sendSeparator(player);
    }

    /**
     * 发送启动横幅（加粗）
     */
    public static void sendStartupBanner(JavaPlugin plugin) {
        if (plugin == null) return;
        String bold = consoleBoldEnabled ? ANSI_BOLD : "";
        String reset = consoleBoldEnabled ? ANSI_RESET : "";
        plugin.getLogger().info(bold + "╔══════════════════════════════════════╗" + reset);
        plugin.getLogger().info(bold + "      SnowTerritory 插件已启用          " + reset);
        plugin.getLogger().info(bold + "      版本: " + plugin.getPluginMeta().getVersion() + "                    " + reset);
        plugin.getLogger().info(bold + "      作者: " + String.join(", ", plugin.getPluginMeta().getAuthors()) + "              " + reset);
        plugin.getLogger().info(bold + "╚══════════════════════════════════════╝" + reset);
    }

    /**
     * 发送关闭横幅（加粗）
     */
    public static void sendShutdownBanner(JavaPlugin plugin) {
        if (plugin == null) return;
        String bold = consoleBoldEnabled ? ANSI_BOLD : "";
        String reset = consoleBoldEnabled ? ANSI_RESET : "";
        plugin.getLogger().info(bold + "╔══════════════════════════════════════╗" + reset);
        plugin.getLogger().info(bold + "║     SnowTerritory 插件已禁用          ║" + reset);
        plugin.getLogger().info(bold + "╚══════════════════════════════════════╝" + reset);
    }

    // ========== 工具方法 ==========

    /**
     * 转换颜色代码（支持传统颜色代码、16进制颜色和渐变）
     * 
     * 支持格式：
     * - 传统颜色代码: &a (绿色), &l (粗体), &r (重置) 等
     * - 16进制颜色: &{#FFFFFF} 或 <#FFFFFF>
     * - 渐变颜色: 
     *   - <GRADIENT:#FF6600:#FF6600> 或 <g:#1f2fa6:#91c8ff>
     *   - {#314eb5>}Text{#8fdaff<}
     *   - {#314eb5>}Text1{#c294ff<>} Text2{#8fdaff<} (多层渐变)
     * 
     * 示例：
     * - "&aHello &{#FF0000}World" → 绿色Hello + 红色World
     * - "<#00FF00>Green Text" → 绿色文本
     * - "<GRADIENT:#FF0000:#0000FF>Gradient" → 红到蓝渐变
     * 
     * @param text 包含颜色代码的文本
     * @return 转换后的文本（可直接发送给玩家）
     */
    public static String colorize(String text) {
        if (text == null) return "";
        
        // 先处理渐变颜色（需要在16进制颜色之前处理）
        text = processGradients(text);
        
        // 然后处理16进制颜色格式（转换为Minecraft原生格式）
        text = processHexColors(text);
        
        // 最后处理传统颜色代码（&a, &l 等）
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    /**
     * 处理渐变颜色格式
     * 
     * 支持的格式：
     * - <GRADIENT:#FF6600:#FF6600>Text</GRADIENT> 或 <g:#1f2fa6:#91c8ff>Text</g>
     * - {#314eb5>}Text{#8fdaff<} - 自定义格式，> 表示开始，< 表示结束
     * - {#314eb5>}Text1{#c294ff<>} Text2{#8fdaff<} - 多层渐变，<> 表示中间点
     * 
     * @param text 包含渐变颜色代码的文本
     * @return 转换后的文本
     */
    private static String processGradients(String text) {
        if (text == null || text.isEmpty()) return text;
        
        // 处理标准渐变格式: <GRADIENT:#FF6600:#FF6600>Text</GRADIENT> 或 <g:#1f2fa6:#91c8ff>Text</g>
        text = processStandardGradient(text);
        
        // 处理自定义渐变格式: {#314eb5>}Text{#8fdaff<} 或多层渐变
        text = processCustomGradient(text);
        
        return text;
    }
    
    /**
     * 处理标准渐变格式: <GRADIENT:#FF6600:#FF6600>Text</GRADIENT> 或 <g:#1f2fa6:#91c8ff>Text</g>
     */
    private static String processStandardGradient(String text) {
        // 处理 <GRADIENT:#RRGGBB:#RRGGBB>Text</GRADIENT> 格式
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "<(?:GRADIENT|g):#([0-9A-Fa-f]{6}):#([0-9A-Fa-f]{6})>(.*?)</(?:GRADIENT|g)>",
            java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL
        );
        
        java.util.regex.Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String color1 = matcher.group(1);
            String color2 = matcher.group(2);
            String content = matcher.group(3);
            
            String gradientText = applyGradient(content, color1, color2);
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(gradientText));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 处理自定义渐变格式: {#314eb5>}Text{#8fdaff<} 或 {#314eb5>}Text1{#c294ff<>} Text2{#8fdaff<}
     */
    private static String processCustomGradient(String text) {
        if (text == null || text.isEmpty()) return text;
        
        // 使用更精确的正则表达式匹配所有渐变标记
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "\\{#([0-9A-Fa-f]{6})(>|<>|<)\\}(.*?)(?=\\{#|$)",
            java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL
        );
        
        java.util.regex.Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();
        int lastIndex = 0;
        
        GradientSegment currentSegment = null;
        
        while (matcher.find()) {
            // 添加匹配前的普通文本
            if (matcher.start() > lastIndex) {
                result.append(text.substring(lastIndex, matcher.start()));
            }
            
            String color = matcher.group(1);
            String marker = matcher.group(2);
            String content = matcher.group(3);
            
            if (marker.equals(">")) {
                // 开始新的渐变段
                if (currentSegment != null && currentSegment.endColor == null) {
                    // 如果之前的段没有结束，先处理它（使用开始颜色作为结束颜色）
                    currentSegment.setEndColor(currentSegment.startColor);
                    result.append(currentSegment.applyGradient());
                }
                currentSegment = new GradientSegment(color, content);
            } else if (marker.equals("<>")) {
                // 中间点，添加到当前段
                if (currentSegment != null) {
                    currentSegment.addMiddlePoint(color, content);
                }
            } else if (marker.equals("<")) {
                // 结束点
                if (currentSegment != null) {
                    currentSegment.setEndColor(color);
                    result.append(currentSegment.applyGradient());
                    currentSegment = null;
                }
            }
            
            lastIndex = matcher.end();
        }
        
        // 处理未完成的渐变段
        if (currentSegment != null) {
            if (currentSegment.endColor == null) {
                currentSegment.setEndColor(currentSegment.startColor);
            }
            result.append(currentSegment.applyGradient());
        }
        
        // 添加剩余的文本
        if (lastIndex < text.length()) {
            result.append(text.substring(lastIndex));
        }
        
        return result.toString();
    }
    
    /**
     * 渐变段（用于处理自定义格式的渐变）
     */
    private static class GradientSegment {
        String startColor;
        String endColor;
        java.util.List<String> middleColors = new java.util.ArrayList<>();
        java.util.List<String> middleTexts = new java.util.ArrayList<>();
        String startText;
        
        GradientSegment(String startColor, String startText) {
            this.startColor = startColor;
            this.startText = startText;
        }
        
        void addMiddlePoint(String color, String text) {
            middleColors.add(color);
            middleTexts.add(text);
        }
        
        void setEndColor(String color) {
            this.endColor = color;
        }
        
        String applyGradient() {
            if (endColor == null) {
                return startText; // 没有结束点，返回原文本
            }
            
            // 构建完整的文本和颜色点
            java.util.List<String> allColors = new java.util.ArrayList<>();
            java.util.List<String> allTexts = new java.util.ArrayList<>();
            
            allColors.add(startColor);
            allTexts.add(startText);
            
            for (int i = 0; i < middleColors.size(); i++) {
                allColors.add(middleColors.get(i));
                allTexts.add(middleTexts.get(i));
            }
            
            allColors.add(endColor);
            
            // 合并所有文本
            StringBuilder fullText = new StringBuilder(startText);
            for (String text : middleTexts) {
                fullText.append(text);
            }
            
            // 计算每个颜色点对应的文本位置
            String cleanText = fullText.toString().replaceAll("&[0-9a-fk-or]", "");
            if (cleanText.isEmpty()) {
                return fullText.toString();
            }
            
            // 计算每个颜色点对应的字符位置
            int[] colorPositions = new int[allColors.size()];
            int totalChars = cleanText.length();
            for (int i = 0; i < allColors.size(); i++) {
                colorPositions[i] = (int) Math.round((double) i / (allColors.size() - 1) * (totalChars - 1));
            }
            
            // 应用渐变
            StringBuilder result = new StringBuilder();
            int textIndex = 0;
            
            for (int i = 0; i < fullText.length(); i++) {
                char ch = fullText.charAt(i);
                
                // 跳过颜色代码
                if (ch == '&' && i + 1 < fullText.length()) {
                    char next = fullText.charAt(i + 1);
                    if ("0123456789abcdefklmnor".indexOf(Character.toLowerCase(next)) >= 0) {
                        result.append(ch).append(next);
                        i++;
                        continue;
                    }
                }
                
                // 计算当前字符应该使用的颜色
                int charPos = textIndex;
                if (charPos >= cleanText.length()) {
                    charPos = cleanText.length() - 1;
                }
                
                // 找到字符所在的两个颜色点之间
                int segment = 0;
                for (int j = 0; j < colorPositions.length - 1; j++) {
                    if (charPos >= colorPositions[j] && charPos <= colorPositions[j + 1]) {
                        segment = j;
                        break;
                    }
                }
                if (segment >= colorPositions.length - 1) {
                    segment = colorPositions.length - 2;
                }
                
                // 计算插值
                int startIdx = colorPositions[segment];
                int endIdx = colorPositions[segment + 1];
                double ratio = (endIdx == startIdx) ? 0 : (double)(charPos - startIdx) / (endIdx - startIdx);
                
                String color1 = allColors.get(segment);
                String color2 = allColors.get(segment + 1);
                String interpolatedColor = interpolateColor(color1, color2, ratio);
                
                // 应用颜色
                result.append(hexToMinecraftColor(interpolatedColor));
                result.append(ch);
                
                textIndex++;
            }
            
            return result.toString();
        }
    }
    
    
    /**
     * 应用渐变到文本（两色渐变）
     */
    private static String applyGradient(String text, String color1, String color2) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 移除颜色代码字符，只计算实际显示字符
        String cleanText = text.replaceAll("&[0-9a-fk-or]", "");
        if (cleanText.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        int textIndex = 0;
        
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            
            // 跳过颜色代码
            if (ch == '&' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if ("0123456789abcdefklmnor".indexOf(Character.toLowerCase(next)) >= 0) {
                    result.append(ch).append(next);
                    i++;
                    continue;
                }
            }
            
            // 计算当前字符的渐变比例
            double ratio = cleanText.length() > 1 ? (double) textIndex / (cleanText.length() - 1) : 0;
            String interpolatedColor = interpolateColor(color1, color2, ratio);
            
            // 应用颜色
            result.append(hexToMinecraftColor(interpolatedColor));
            result.append(ch);
            
            textIndex++;
        }
        
        return result.toString();
    }
    
    /**
     * 颜色插值（从color1到color2，ratio从0到1）
     */
    private static String interpolateColor(String color1, String color2, double ratio) {
        int r1 = Integer.parseInt(color1.substring(0, 2), 16);
        int g1 = Integer.parseInt(color1.substring(2, 4), 16);
        int b1 = Integer.parseInt(color1.substring(4, 6), 16);
        
        int r2 = Integer.parseInt(color2.substring(0, 2), 16);
        int g2 = Integer.parseInt(color2.substring(2, 4), 16);
        int b2 = Integer.parseInt(color2.substring(4, 6), 16);
        
        int r = (int) Math.round(r1 + (r2 - r1) * ratio);
        int g = (int) Math.round(g1 + (g2 - g1) * ratio);
        int b = (int) Math.round(b1 + (b2 - b1) * ratio);
        
        // 确保值在0-255范围内
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        
        return String.format("%02X%02X%02X", r, g, b);
    }
    
    /**
     * 将16进制颜色转换为Minecraft格式: &x&R&R&G&G&B&B
     */
    private static String hexToMinecraftColor(String hex) {
        if (hex == null || hex.length() != 6) {
            return "";
        }
        hex = hex.toUpperCase();
        return "&x&" + hex.charAt(0) + "&" + hex.charAt(1) + 
               "&" + hex.charAt(2) + "&" + hex.charAt(3) + 
               "&" + hex.charAt(4) + "&" + hex.charAt(5);
    }
    
    /**
     * 处理16进制颜色格式
     * 
     * 支持的格式：
     * - &{#FFFFFF} → 转换为 &x&F&F&F&F&F&F
     * - <#FFFFFF> → 转换为 &x&F&F&F&F&F&F
     * 
     * Minecraft 1.16+ 使用 &x&R&R&G&G&B&B 格式表示16进制颜色
     * 其中每个字符前都需要 & 符号
     * 
     * @param text 包含16进制颜色代码的文本
     * @return 转换后的文本
     */
    private static String processHexColors(String text) {
        if (text == null || text.isEmpty()) return text;
        
        // 处理 &{#RRGGBB} 格式
        // 示例: &{#FF0000} → &x&F&F&0&0&0&0 (红色)
        text = text.replaceAll("&\\{#([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])\\}", 
            "&x&$1&$2&$3&$4&$5&$6");
        
        // 处理 <#RRGGBB> 格式
        // 示例: <#00FF00> → &x&0&0&F&F&0&0 (绿色)
        text = text.replaceAll("<#([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])>", 
            "&x&$1&$2&$3&$4&$5&$6");
        
        return text;
    }

    /**
     * 移除颜色代码
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(text);
    }

    /**
     * 格式化数字（带颜色）
     */
    public static String formatNumber(int number) {
        return "&e" + number;
    }

    /**
     * 格式化数字（带颜色）
     */
    public static String formatNumber(double number) {
        return "&e" + number;
    }
}

