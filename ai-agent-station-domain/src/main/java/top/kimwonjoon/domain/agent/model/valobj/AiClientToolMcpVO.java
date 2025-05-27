package top.kimwonjoon.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP VO 对象
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientToolMcpVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * MCP名称
     */
    private String mcpName;

    /**
     * 传输类型(sse/stdio)
     */
    private String transportType;

    /**
     * 传输配置 - sse
     */
    private TransportConfigSse transportConfigSse;

    /**
     * 传输配置 - stdio
     */
    private TransportConfigStdio transportConfigStdio;

    /**
     * 请求超时时间(分钟)
     */
    private Integer requestTimeout;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransportConfigSse {
        private String baseUri;
        private String sseEndpoint;
    }

    /**
     * "mcp-server-weixin": {
     * "command": "java",
     * "args": [
     * "-Dspring.ai.mcp.server.stdio=true",
     * "-jar",
     * "/Users/fuzhengwei/Applications/apache-maven-3.8.4/repository/cn/bugstack/mcp/mcp-server-weixin/1.0.0/mcp-server-weixin-1.0.0.jar"
     * ]
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransportConfigStdio {

        private Map<String, Stdio> stdio;

        @Data
        public static class Stdio {
            private String command;
            private List<String> args;
        }
    }

}
