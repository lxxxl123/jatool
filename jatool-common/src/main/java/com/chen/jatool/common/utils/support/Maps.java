package com.chen.jatool.common.utils.support;

import cn.hutool.core.convert.Convert;
import com.chen.jatool.common.utils.CollUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenwh3
 */
public class Maps {
    private Map<Object, Object> map;

    private Maps() {
    }

    private static Map<Object, Object> buildMapByArr(Object... kv) {
        if(kv.length % 2 != 0) throw new IllegalArgumentException("kv must be even");
        Map<Object, Object> map = new HashMap<>(kv.length / 2);
        for (int i = 0; i < kv.length; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }

    public static Maps of() {
        Maps maps = new Maps();
        maps.map = new HashMap<>();
        return maps;
    }

    public static Maps of(Map map) {
        Maps maps = new Maps();
        maps.map = map;
        return maps;
    }

    public static Maps ofArr(Object... kv) {
        Maps maps = new Maps();
        maps.map = buildMapByArr(kv);
        return maps;
    }

    public String getStr(String key){
        return Convert.toStr(map.get(key));
    }

    public List<String> getStrList(String key, String split) {
        Object o = map.get(key);
        if (o instanceof String) {
            return (List) CollUtils.toTrimStrColl(((String) o).split(split));
        }
        return (List) CollUtils.toStrColl(o);
    }


    public <K,V> Map<K,V> getMap(Class<K> kClass , Class<V> vClass){
        return (Map<K, V>) map;
    }
}
