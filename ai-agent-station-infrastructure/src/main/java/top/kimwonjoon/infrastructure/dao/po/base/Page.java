package top.kimwonjoon.infrastructure.dao.po.base;

import lombok.Data;

/**
 * 分页
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-06 14:34
 */
@Data
public class Page {

    /**
     * 当前页码
     */
    private int pageNum = 1;

    /**
     * 每页条数
     */
    private int pageSize = 10;

    /**
     * 总条数
     */
    private long total;

    /**
     * 总页数
     */
    private int pages;

    /**
     * 获取 MySQL 分页的起始行
     * @return 分页起始行
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取每页记录数
     * @return 每页记录数
     */
    public int getLimit() {
        return pageSize;
    }

}
