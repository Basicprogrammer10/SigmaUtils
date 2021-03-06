package com.connorcode.sigmautils.modules._interface;

import com.connorcode.sigmautils.SigmaUtilsClient;
import com.connorcode.sigmautils.module.BasicModule;
import com.connorcode.sigmautils.module.Category;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class RandomBackground extends BasicModule {
    public static List<String> validBackgrounds = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
            SigmaUtilsClient.class.getClassLoader()
                    .getResourceAsStream("background_blocks.txt"))))
            .lines()
            .toList();

    public RandomBackground() {
        super("random_background", "Random Background", "Uses random textures for the background tessellation",
                Category.Interface);
    }
}
