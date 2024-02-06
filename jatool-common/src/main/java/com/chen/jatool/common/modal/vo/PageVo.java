package com.chen.jatool.common.modal.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenwh3
 */
@Data
public class PageVo<T>  extends Page<T> {

    public Map<String, Object> entity;

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

    public static PageVo of(Page page){
        return new PageVo(page);
    }

    public PageVo put(String key , Object value){
        if(entity == null){
            entity = new HashMap<>(4);
        }
        entity.put(key,value);
        return this;
    }




}
