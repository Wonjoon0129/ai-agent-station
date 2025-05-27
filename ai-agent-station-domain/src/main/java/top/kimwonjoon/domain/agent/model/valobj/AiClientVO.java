package top.kimwonjoon.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Ai 对话客户端
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientVO {

    private Long clientId;

    private Long systemPromptId;

    private String modelBeanId;

    private List<String> mcpBeanIdList;

    private List<String> advisorBeanIdList;

    public String getModelBeanName() {
        return "AiClientModel_" + modelBeanId;
    }

    public List<String> getMcpBeanNameList() {
        List<String> beanNameList = new ArrayList<>();
        for (String mcpBeanId : mcpBeanIdList) {
            beanNameList.add("AiClientToolMcp_" + mcpBeanId);
        }
        return beanNameList;
    }

    public List<String> getAdvisorBeanNameList() {
        List<String> beanNameList = new ArrayList<>();
        for (String mcpBeanId : advisorBeanIdList) {
            beanNameList.add("AiClientAdvisor_" + mcpBeanId);
        }
        return beanNameList;
    }

}
