package com.chen.jatool.common.modal.vo;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.jatool.common.utils.support.sql.PlainWrapper;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author chenwh3
 */
@Data
public class PageVo<T> extends Page<T> {

    public Map<String, Object> entity;

    private String warnMsg;
    private String errorMsg;

    private PageVo(Page page) {
        this.setSearchCount(page.isSearchCount());
        this.setTotal(page.getTotal());
        this.setOrders(page.getOrders());
        this.setRecords(page.getRecords());
        this.setSize(page.getSize());
        this.setMaxLimit(page.getMaxLimit());
        this.setPages(page.getPages());
        this.setCurrent(page.getCurrent());
    }

    public PageVo(Integer index, Integer size) {
        this.setCurrent(index);
        this.setSize(size);
    }

    public PageVo(Integer index, Integer size, boolean searchCount) {
        this.setCurrent(index);
        this.setSize(size);
        this.setSearchCount(searchCount);
    }

    public static <T> PageVo<T> wrapRawData(List<T> list, Integer pageIndex, Integer pageSize) {
        List<T> record
                = CollUtil.page(pageIndex - 1, pageSize, list);
        PageVo<T> res = new PageVo<>(pageIndex, pageSize);
        res.setRecords(record);
        res.setTotal(list.size());
        return res;
    }

    public static <T> PageVo<T> of(Page<T> page) {
        return new PageVo<>(page);
    }

    public PageVo put(String key, Object value) {
        if (entity == null) {
            entity = new HashMap<>(4);
        }
        entity.put(key, value);
        return this;
    }

    public PageVo<T> orderBy(String order){
        setOrders(PlainWrapper.buildOrderItem(order));
        return this;
    }

    public PageVo<T> peek(Consumer<T> consumer) {
        if (getRecords() != null) {
            getRecords().forEach(consumer);
        }
        return this;
    }


}
