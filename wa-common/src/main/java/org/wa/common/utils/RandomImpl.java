package org.wa.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

/**
 * @Auther: XF
 * @Date: 2018/10/6 17:44
 * @Description:
 */
public class RandomImpl<E> {
    private static final Random random = new Random();
    public static <E> E getRandomElement(Collection<E> c) {
        return new ArrayList<>(c).get(random.nextInt(c.size()));
    }
}
