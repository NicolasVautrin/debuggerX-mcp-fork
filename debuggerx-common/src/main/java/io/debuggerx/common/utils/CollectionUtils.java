package io.debuggerx.common.utils;

import java.util.Collection;

/**
 * @author wuou
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }
}
