package top.kimwonjoon.types.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class Constants {

    public final static String SPLIT = ",";

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum ResponseCode {

        SUCCESS("0000", "成功"),
        UN_ERROR("0001", "未知失败"),
        ILLEGAL_PARAMETER("0002", "非法参数"),
        INDEX_EXCEPTION("0003", "唯一索引冲突"),
        UPDATE_ZERO("0004", "更新记录为0"),
        HTTP_EXCEPTION("0005", "HTTP接口调用异常"),
        AGENT_NULL_EXCEPTION("0006", "智能体未装配"),

        ;

        private String code;
        private String info;

    }

}
