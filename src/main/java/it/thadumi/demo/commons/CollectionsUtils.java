package it.thadumi.demo.commons;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import io.vavr.collection.Map;

public class CollectionsUtils {

    public static <K,V> BidiMap<K,V> asBidiMap(Map<K,V> map) {
        return asBidiMap(map.toJavaMap());
    }

    public static <K,V> BidiMap<K,V> asBidiMap(java.util.Map<K,V> map) {
        return new DualHashBidiMap<>(map);
    }

    private CollectionsUtils() {}
}
