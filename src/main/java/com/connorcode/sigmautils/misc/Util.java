package com.connorcode.sigmautils.misc;

import com.connorcode.sigmautils.SigmaUtils;
import com.connorcode.sigmautils.mixin.ScreenAccessor;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.stream.Collectors;

public class Util {
    public static Object loadNewClass(String classPath) {
        try {
            return SigmaUtils.class.getClassLoader().loadClass(classPath).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException |
                 NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String loadResourceString(String name) {
        return new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(SigmaUtils.class.getClassLoader().getResourceAsStream(name)))).lines()
                .collect(Collectors.joining("\n"));
    }

    public static boolean loadEnabled(NbtCompound config) {
        return config.getBoolean("enabled");
    }

    public static NbtCompound saveEnabled(boolean enabled) {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("enabled", enabled);
        return nbt;
    }

    public static String toSnakeCase(String s) {
        return s.toLowerCase().replace(' ', '_');
    }

    public static void addDrawable(Screen screen, Drawable drawable) {
        ScreenAccessor sa = ((ScreenAccessor) screen);
        sa.getDrawables().add(drawable);
        sa.getChildren().add(drawable);
        sa.getSelectables().add(drawable);
    }

    public static String bestTime(long ms) {
        Pair<String, Integer>[] units = new Pair[]{
                new Pair<>("second", 60),
                new Pair<>("minute", 60),
                new Pair<>("hour", 24),
                new Pair<>("day", 30),
                new Pair<>("month", 12),
                new Pair<>("year", 0)
        };
        return bestTime(ms, units, false, 0);
    }

    public static String bestTime(long ms, Pair<String, Integer>[] units, boolean small, int precision) {
        float seconds = ms / 1000f;
        for (Pair<String, Integer> unit : units) {
            if (unit.getRight() == 0 || seconds < unit.getRight()) {
                float intSeconds = precision == 0 ? Math.round(seconds) : seconds;
                return String.format("%." + precision + "f%s%s%s", intSeconds, small ? "" : " ", unit.getLeft(),
                        (intSeconds > 1 && !small) ? "s" : "");
            }

            seconds /= unit.getRight();
        }

        return String.format("%s years", Math.round(seconds));
    }

    public enum TimeFormat {
        HMS,
        BestFit;

        public String format(long ms) {
            return switch (this) {
                case HMS -> DurationFormatUtils.formatDuration(ms, "HH:mm:ss");
                case BestFit -> Util.bestTime(ms);
            };
        }
    }
}
