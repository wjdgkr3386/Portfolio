package com.neighbus.util;

import java.util.List;
import java.util.Map;

public class PagingDTO<T> {

    private final List<T> list;
    private final Map<String, Integer> pagingMap;

    public PagingDTO(List<T> list, Map<String, Integer> pagingMap) {
        this.list = list;
        this.pagingMap = pagingMap;
    }
    public List<T> getList() {
        return list;
    }

    public Map<String, Integer> getPagingMap() {
        return pagingMap;
    }
}
