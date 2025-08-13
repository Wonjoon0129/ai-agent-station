package top.kimwonjoon.domain.agent.model.valobj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent 通用枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiAgentEnumVO {

    AI_CLIENT_API("对话API", "api", "ai_client_api_"),
    AI_CLIENT_SYSTEM_PROMPT("提示词", "prompt", "ai_client_system_prompt_"),
    AI_CLIENT("客户端", "client", "ai_client_"),

    DATA_BASE_DRIVE("数据库驱动", "database_drive", "DataBaseDrive_"),
    CHAT_CLIENT("聊天客户端", "chat_client", "ChatClient_"),
    AI_CLIENT_MODEL("对话模型", "model", "AiClientModel_"),
    AI_CLIENT_ADVISOR("顾问角色", "advisor", "AiClientAdvisor_"),
    AI_CLIENT_TOOL_MCP("mcp工具", "tool_mcp", "AiClientToolMcp_"),
    AI_CLIENT_EMBEDDING_MODEL("嵌入模型","ai_client_embedding_model","AiClientEmbeddingModel_")
    ;

    /**
     * 名称
     */
    private String name;

    /**
     * code
     */
    private String code;

    /**
     * Bean 对象名称标签
     */
    private String beanNameTag;


    // 静态Map用于O(1)时间复杂度查找
    private static final Map<String, AiAgentEnumVO> CODE_MAP = new HashMap<>();

    // 静态初始化块，在类加载时构建Map
    static {
        for (AiAgentEnumVO enumVO : AiAgentEnumVO.values()) {
            CODE_MAP.put(enumVO.getCode(), enumVO);
        }
    }

    /**
     * 根据code获取对应的枚举 - O(1)时间复杂度
     *
     * @param code 枚举code值
     * @return 对应的枚举，如果未找到则抛出异常
     */
    public static AiAgentEnumVO getByCode(String code) {
        if (code == null) {
            return null;
        }

        AiAgentEnumVO result = CODE_MAP.get(code);
        if (result == null) {
            throw new RuntimeException("code value " + code + " not exist!");
        }
        return result;
    }

    /**
     * 获取Bean名称
     *
     * @param id 传入的参数
     * @return beanNameTag + id 拼接的Bean名称
     */
    public String getBeanName(String id) {
        return this.beanNameTag + id;
    }

}
