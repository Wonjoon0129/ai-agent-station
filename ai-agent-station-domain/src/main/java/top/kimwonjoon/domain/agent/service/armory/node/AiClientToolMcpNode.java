package top.kimwonjoon.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.model.valobj.AiClientToolMcpVO;
import top.kimwonjoon.domain.agent.service.armory.AbstractArmorySupport;
import top.kimwonjoon.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @ClassName Ai
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 19. 15:00
 * @Version 1.0
 */
@Slf4j
@Component
public class AiClientToolMcpNode extends AbstractArmorySupport {

    @Resource
    private AiClientModelNode aiClientModelNode;


    @Override
    protected String doApply(AiAgentEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，tool mcp 节点 {}", JSON.toJSONString(requestParameter));

        List<AiClientToolMcpVO> aiClientToolMcpList = dynamicContext.getValue("aiClientToolMcpList");
        if (aiClientToolMcpList == null || aiClientToolMcpList.isEmpty()) {
            log.warn("没有可用的AI客户端工具配置 MCP");
            return router(requestParameter, dynamicContext);
        }
        for (AiClientToolMcpVO mcpVO : aiClientToolMcpList) {
            // 创建McpSyncClient对象
            McpSyncClient mcpSyncClient = createMcpSyncClient(mcpVO);
            // 使用父类的通用注册方法
            registerBean(beanName(mcpVO.getId()), McpSyncClient.class, mcpSyncClient);
        }
        return router(requestParameter, dynamicContext);
    }

    protected McpSyncClient createMcpSyncClient(AiClientToolMcpVO aiClientToolMcpVO) {
        String transportType = aiClientToolMcpVO.getTransportType();
        switch (transportType){
            case "sse"->{
                AiClientToolMcpVO.TransportConfigSse transportConfigSse = aiClientToolMcpVO.getTransportConfigSse();
                String originalBaseUri = transportConfigSse.getBaseUri();
                String baseUri;
                String sseEndpoint;

                int queryParamStartIndex = originalBaseUri.indexOf("sse");
                if (queryParamStartIndex != -1) {
                    baseUri = originalBaseUri.substring(0, queryParamStartIndex-1);
                    sseEndpoint = originalBaseUri.substring(queryParamStartIndex-1);
                }else{
                    baseUri = originalBaseUri;
                    sseEndpoint = transportConfigSse.getSseEndpoint();
                }

                sseEndpoint = StringUtils.isBlank(sseEndpoint) ? "/sse" : sseEndpoint;

                HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport
                        .builder(baseUri)
                        .sseEndpoint(sseEndpoint).build();

                McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport).requestTimeout(Duration.ofMinutes(aiClientToolMcpVO.getRequestTimeout())).build();
                var init_sse = mcpSyncClient.initialize();
                log.info("Tool SSE MCP Initialized {}", init_sse);
                return mcpSyncClient;

            }
            case "stdio"->{
                AiClientToolMcpVO.TransportConfigStdio transportConfigStdio = aiClientToolMcpVO.getTransportConfigStdio();
                Map<String, AiClientToolMcpVO.TransportConfigStdio.Stdio> stdioMap = transportConfigStdio.getStdio();
                AiClientToolMcpVO.TransportConfigStdio.Stdio stdio = stdioMap.get(aiClientToolMcpVO.getMcpName());

                // https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem
                var stdioParams = ServerParameters.builder(stdio.getCommand())
                        .args(stdio.getArgs())
                        .build();
                var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                        .requestTimeout(Duration.ofSeconds(aiClientToolMcpVO.getRequestTimeout())).build();
                var init_stdio = mcpClient.initialize();
                log.info("Tool Stdio MCP Initialized {}", init_stdio);
                return mcpClient;

            }
        }
        throw new RuntimeException("err! transportType " + transportType + " not exist!");
    }

    @Override
    protected String beanName(Long id) {
        return "AiClientToolMcp_" + id;
    }

    @Override
    public StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(AiAgentEngineStarterEntity aiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientModelNode;
    }
}
