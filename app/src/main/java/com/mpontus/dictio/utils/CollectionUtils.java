package com.mpontus.dictio.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CollectionUtils {

    public static <T> List<T> randomMerge(List<T> a, List<T> b) {
        Random random = new Random();
        int aSize = a.size();
        int bSize = b.size();
        int totalSize = aSize + bSize;
        List<T> result = new ArrayList<>(totalSize);

        for (int i = 0; i < totalSize; ++i) {
            float aChance = aSize / (aSize + bSize);

            if (random.nextFloat() < aChance) {
                result.set(i, a.remove(0));
                aSize--;
            } else {
                result.set(i, b.remove(0));
                bSize--;
            }
        }

        return result;
    }
}
