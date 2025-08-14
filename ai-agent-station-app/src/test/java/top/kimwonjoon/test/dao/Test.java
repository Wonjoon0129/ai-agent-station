package top.kimwonjoon.test.dao;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * @ClassName Test
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/7/16 15:55
 * @Version 1.0
 */

public class Test {
    public static void main(String[] args) throws JsonProcessingException {
        String s = "{\"result\":0,\"code\":\"200\",\"msg\":\"成功\",\"data\":\"[{\\\"wlbm\\\":\\\"80016307\\\",\\\"state\\\":\\\"解禁\\\",\\\"message\\\":\\\"编码为：80016307的物料数据修改成功。\\\",\\\"isSuccess\\\":1}]\",\"tokenMap\":null,\"dataAll\":null}";
        JSONObject jsonObject = JSONObject.parseObject(s);
        String dataJson = jsonObject.getString("data");
        List<MdmMaterialResp.Item> dataList = JSONObject.parseArray(dataJson, MdmMaterialResp.Item.class);

        MdmMaterialResp mdmMaterialResp = new MdmMaterialResp();
        mdmMaterialResp.setResult(jsonObject.getIntValue("result"));
        mdmMaterialResp.setCode(jsonObject.getString("code"));
        mdmMaterialResp.setMsg(jsonObject.getString("msg"));
        mdmMaterialResp.setData(dataList);
        System.out.println(mdmMaterialResp);
    }

    @Data
    public static class MdmMaterialResp {

        private static final String SUCCESS = "200";


        private int result;

        /**
         * 200:成功
         * 其它编码:失败
         */
        private String code;

        private String msg;

        private List<Item> data;

        @Data
        public static class Item {
            private String wlbm;

            private String state;

            private String message;

            private int isSuccess;
        }

        /**
         * 是否成功
         * @return 如果是0 代表true
         */
        public boolean isSuccess() {
            return Objects.equals(code, SUCCESS);
        }


    }
}
