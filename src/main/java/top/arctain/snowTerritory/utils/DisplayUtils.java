package top.arctain.snowTerritory.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class DisplayUtils {

    private DisplayUtils() {}

    // 解析 "(3/87)"、"3/87"、"  ( 3 / 87 )  " 之类
    private static final Pattern FRACTION = Pattern.compile("\\s*\\(?\\s*(\\d+)\\s*/\\s*(\\d+)\\s*\\)?\\s*");

    /** 进度条样式 */
    public enum BarStyle {
        BLOCKS("█", "░"),
        BARS("|", "|"),
        SQUARES("■", "□"),
        DOTS("●", "○");

        private final String full;
        private final String empty;

        BarStyle(String full, String empty) {
            this.full = full;
            this.empty = empty;
        }
    }

    /**
     * 将 "(3/87)" 转进度条
     */
    public static String progressBar(BarStyle style,
                                     String lowColor, String midColor, String highColor,
                                     String fractionText,
                                     int length) {
        int[] frac = parseFraction(fractionText);
        return progressBar(style, lowColor, midColor, highColor, frac[0], frac[1], length);
    }

    /**
     * 将 current/total 转进度条
     */
    public static String progressBar(BarStyle style,
                                     String lowColor, String midColor, String highColor,
                                     int current, int total,
                                     int length) {
        if (length <= 0) throw new IllegalArgumentException("length must be > 0");
        if (total <= 0) total = 1;
        current = Math.max(0, Math.min(current, total));

        double p = current / (double) total; // 0..1
        int filled = (int) Math.round(p * length);
        filled = Math.max(0, Math.min(filled, length));

        String color = pickColor(p, lowColor, midColor, highColor);

        StringBuilder sb = new StringBuilder();
        sb.append(ColorUtils.colorize(color));
        sb.append(repeat(style.full, filled));
        sb.append(ColorUtils.colorize("&7")); // 空白部分默认灰色（可按需改成参数）
        sb.append(repeat(style.empty, length - filled));
        sb.append(ColorUtils.colorize("&r")); // reset
        return sb.toString();
    }

    private static String pickColor(double p, String low, String mid, String high) {
        if (p < 1.0 / 3.0) return low;
        if (p < 2.0 / 3.0) return mid;
        return high;
    }

    private static int[] parseFraction(String s) {
        if (s == null) throw new IllegalArgumentException("fractionText is null");
        Matcher m = FRACTION.matcher(s);
        if (!m.matches()) throw new IllegalArgumentException("Invalid fraction: " + s);
        int a = Integer.parseInt(m.group(1));
        int b = Integer.parseInt(m.group(2));
        return new int[]{a, b};
    }

    private static String repeat(String unit, int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder(unit.length() * n);
        for (int i = 0; i < n; i++) sb.append(unit);
        return sb.toString();
    }

    /**
     * 将毫秒数转换为 HH:MM:SS 格式
     * @param milliseconds 毫秒数
     * @return 格式化的时间字符串，例如 "01:23:45"
     */
    public static String formatTime(long milliseconds) {
        if (milliseconds < 0) milliseconds = 0;
        
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}