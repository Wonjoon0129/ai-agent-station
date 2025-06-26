package top.kimwonjoon.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.agent.adapter.repository.IAgentRepository;
import top.kimwonjoon.domain.agent.model.valobj.*;
import top.kimwonjoon.infrastructure.dao.*;
import top.kimwonjoon.infrastructure.dao.po.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName AgentRepository
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 19. 15:19
 * @Version 1.0
 */
@Slf4j
@Repository
public class AgentRepository implements IAgentRepository {

    @Resource
    IAiClientModelDao  aiClientModelDao;
    @Resource
    IAiClientModelToolConfigDao aiClientModelToolConfigDao;
    @Resource
    IAiClientToolMcpDao aiClientToolMcpDao;
    @Resource
    IAiClientAdvisorDao  aiClientAdvisorDao;
    @Resource
    IAiClientSystemPromptDao aiClientSystemPromptDao;
    @Resource
    IAiClientSystemPromptConfigDao aiClientSystemPromptConfigDao;
    @Resource
    IAiClientModelConfigDao aiClientModelConfigDao;
    @Resource
    IAiClientToolConfigDao aiClientToolConfigDao;
    @Resource
    IAiClientAdvisorConfigDao aiClientAdvisorConfigDao;
    @Resource
    private IAiAgentTaskScheduleDao aiAgentTaskScheduleDao;
    @Resource
    IAiAgentDao aiAgentDao;
    @Resource
    IAiRagOrderDao aiRagOrderDao;

    @Resource
    RedissonClient redissonClient;
    @Autowired
    private IAiAgentClientDao aiAgentClientDao;

    @Override
    public List<AiClientModelVO> queryAiClientModelVOListByClientIds(List<Long> clientIdList) {
        List<AiClientModel> aiClientModels=aiClientModelDao.queryModelConfigByClientIds(clientIdList);
        if (null == aiClientModels || aiClientModels.isEmpty()) return new ArrayList<>();

        // 使用lambda表达式获取所有AiClientModel的id属性列表
        List<Long> modelIds = aiClientModels.stream().map(AiClientModel::getId).toList();

        // 查询模型工具配置;
        List<AiClientModelToolConfig> aiClientModelToolConfigs = aiClientModelToolConfigDao.queryModelToolConfigByModelIds(modelIds);
        // 将List转换为Map，key为modeId
        Map<Long, List<AiClientModelToolConfig>> toolConfigMap = aiClientModelToolConfigs.stream()
                .collect(Collectors.groupingBy(AiClientModelToolConfig::getModelId));

        List<AiClientModelVO> aiClientModelVOList = new ArrayList<>();
        for (AiClientModel aiClientModel : aiClientModels) {
            AiClientModelVO vo = new AiClientModelVO();
            vo.setId(aiClientModel.getId());
            vo.setModelName(aiClientModel.getModelName());
            vo.setBaseUrl(aiClientModel.getBaseUrl());
            vo.setApiKey(aiClientModel.getApiKey());
            vo.setCompletionsPath(aiClientModel.getCompletionsPath());
            vo.setEmbeddingsPath(aiClientModel.getEmbeddingsPath());
            vo.setModelType(aiClientModel.getModelType());
            vo.setModelVersion(aiClientModel.getModelVersion());
            vo.setTimeout(aiClientModel.getTimeout());

            // 设置工具配置
            List<AiClientModelToolConfig> toolConfigs = toolConfigMap.getOrDefault(aiClientModel.getId(), Collections.emptyList());
            if (!toolConfigs.isEmpty()) {
                List<AiClientModelVO.AiClientModelToolConfigVO> toolConfigVOs = new ArrayList<>();
                for (AiClientModelToolConfig toolConfig : toolConfigs) {
                    AiClientModelVO.AiClientModelToolConfigVO toolConfigVO = new AiClientModelVO.AiClientModelToolConfigVO();
                    toolConfigVO.setId(toolConfig.getId());
                    toolConfigVO.setModelId(toolConfig.getModelId());
                    toolConfigVO.setToolType(toolConfig.getToolType());
                    toolConfigVO.setToolId(toolConfig.getToolId());
                    toolConfigVO.setCreateTime(toolConfig.getCreateTime());
                    toolConfigVOs.add(toolConfigVO);
                }
                vo.setAiClientModelToolConfigs(toolConfigVOs);
            }

            aiClientModelVOList.add(vo);
        }
        return aiClientModelVOList;
    }

    @Override
    public List<AiClientToolMcpVO> queryAiClientToolMcpVOListByClientIds(List<Long> clientIdList) {
        List<AiClientToolMcp> aiClientToolMcps = aiClientToolMcpDao.queryMcpConfigByClientIds(clientIdList);

        // 将PO对象转换为VO对象
        List<AiClientToolMcpVO> aiClientToolMcpVOList = new ArrayList<>();
        if (null == aiClientToolMcps || aiClientToolMcps.isEmpty()) return aiClientToolMcpVOList;

        for(AiClientToolMcp aiClientToolMcp:aiClientToolMcps){
            AiClientToolMcpVO vo = new AiClientToolMcpVO();
            vo.setId(aiClientToolMcp.getId());
            vo.setMcpName(aiClientToolMcp.getMcpName());
            vo.setTransportType(aiClientToolMcp.getTransportType());
            vo.setRequestTimeout(aiClientToolMcp.getRequestTimeout());

            // 根据传输类型解析JSON配置
            String transportType = aiClientToolMcp.getTransportType();
            String transportConfig = aiClientToolMcp.getTransportConfig();

            try{
                if("sse".equals(transportType)){
                    // 解析SSE配置
                    ObjectMapper objectMapper = new ObjectMapper();
                    AiClientToolMcpVO.TransportConfigSse sseConfig = objectMapper.readValue(transportConfig, AiClientToolMcpVO.TransportConfigSse.class);
                    vo.setTransportConfigSse(sseConfig);
                }else if("stdio".equals(transportType)){
                    // 解析Stdio配置
                    Map<String,AiClientToolMcpVO.TransportConfigStdio.Stdio> stdio= JSON.parseObject(transportConfig,
                            new com.alibaba.fastjson.TypeReference<>() {
                            });
                    AiClientToolMcpVO.TransportConfigStdio stdioConfig = new AiClientToolMcpVO.TransportConfigStdio();
                    stdioConfig.setStdio(stdio);

                    vo.setTransportConfigStdio(stdioConfig);

                }
            }catch (Exception e){
                log.error("解析传输配置失败: {}", e.getMessage(), e);
            }
            aiClientToolMcpVOList.add(vo);

        }
        return aiClientToolMcpVOList;
    }

    @Override
    public List<AiClientAdvisorVO> queryAdvisorConfigByClientIds(List<Long> clientIdList) {
        List<AiClientAdvisor> aiClientAdvisors = aiClientAdvisorDao.queryAdvisorConfigByClientIds(clientIdList);
        if (null == aiClientAdvisors || aiClientAdvisors.isEmpty()) return Collections.emptyList();

        return aiClientAdvisors.stream().map(advisor -> {
            AiClientAdvisorVO vo = AiClientAdvisorVO.builder()
                    .id(advisor.getId())
                    .advisorName(advisor.getAdvisorName())
                    .advisorType(advisor.getAdvisorType())
                    .orderNum(advisor.getOrderNum())
                    .build();

            // 根据 advisorType 类型转换 extParam
            if (StringUtils.isNotEmpty(advisor.getExtParam())) {
                try {
                    if ("ChatMemory".equals(advisor.getAdvisorType())) {
                        AiClientAdvisorVO.ChatMemory chatMemory = JSON.parseObject(advisor.getExtParam(), AiClientAdvisorVO.ChatMemory.class);
                        vo.setChatMemory(chatMemory);
                    } else if ("RagAnswer".equals(advisor.getAdvisorType())) {
                        AiClientAdvisorVO.RagAnswer ragAnswer = JSON.parseObject(advisor.getExtParam(), AiClientAdvisorVO.RagAnswer.class);
                        vo.setRagAnswer(ragAnswer);
                    }
                } catch (Exception e) {
                    log.error("解析 extParam 失败，advisorId={}，extParam={}", advisor.getId(), advisor.getExtParam(), e);
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<Long, AiClientSystemPromptVO> querySystemPromptConfigByClientIds(List<Long> clientIdList) {
        // 从DAO层查询系统提示词配置
        List<AiClientSystemPrompt> aiClientSystemPrompts = aiClientSystemPromptDao.querySystemPromptConfigByClientIds(clientIdList);

        if(null==aiClientSystemPrompts||aiClientSystemPrompts.isEmpty()){
            return Collections.emptyMap();
        }
        /*Map<Long, AiClientSystemPromptVO> resultMap = new HashMap<>();
        for (AiClientSystemPrompt prompt : aiClientSystemPrompts) {
            AiClientSystemPromptVO vo = new AiClientSystemPromptVO();
            vo.setId(prompt.getId());
            vo.setPromptContent(prompt.getPromptContent());

            // 使用putIfAbsent实现保留第一个元素的逻辑
            resultMap.putIfAbsent(vo.getId(), vo);
        }
        return resultMap;*/
        // 将PO对象转换为VO对象，并构建Map结构
        return aiClientSystemPrompts.stream()
                .map(prompt -> AiClientSystemPromptVO.builder()
                        .id(prompt.getId())
                        .promptContent(prompt.getPromptContent())
                        .build())
                .collect(Collectors.toMap(
                        AiClientSystemPromptVO::getId,  // key: id
                        prompt -> prompt,               // value: AiClientSystemPromptVO对象
                        (existing, replacement) -> existing  // 如果有重复key，保留第一个
                ));
    }

    @Override
    public List<AiClientVO> queryAiClientByClientIds(List<Long> clientIdList) {
        if (null == clientIdList || clientIdList.isEmpty()) {
            return Collections.emptyList();
        }
        //查询系统提示词配置
        List<AiClientSystemPromptConfig> systemPromptConfigs = aiClientSystemPromptConfigDao.querySystemPromptConfigByClientIds(clientIdList);
        Map<Long, AiClientSystemPromptConfig> systemPromptConfigMap=systemPromptConfigs.stream().collect(Collectors.toMap(AiClientSystemPromptConfig::getClientId,prompt->prompt,(a,b)->a));

        //查询模型配置
        List<AiClientModelConfig> modeConfigs = aiClientModelConfigDao.queryModelConfigByClientIds(clientIdList);
        Map<Long,AiClientModelConfig> modelConfigMap=modeConfigs.stream().collect(Collectors.toMap(AiClientModelConfig::getClientId,model->model,(a,b)->a));

        // 查询MCP工具配置，暂时只有 mcp，无 function call
        List<AiClientToolConfig> toolConfigs =aiClientToolConfigDao.queryToolConfigByClientIds(clientIdList);
        Map<Long,List<AiClientToolConfig>> mcpMap =toolConfigs.stream().filter(config->"mcp".equals(config.getToolType()))
                .collect(Collectors.groupingBy(AiClientToolConfig::getClientId));

        // 查询顾问配置
        List<AiClientAdvisorConfig> advisorConfigs=aiClientAdvisorConfigDao.queryClientAdvisorConfigByClientIds(clientIdList);
        Map<Long, List<AiClientAdvisorConfig>> advisorConfigMap = advisorConfigs.stream()
                .collect(Collectors.groupingBy(AiClientAdvisorConfig::getClientId));

        // 构建AiClientVO列表
        List<AiClientVO> result = new ArrayList<>();
        for (Long clientId : clientIdList) {
            AiClientVO clientVO = AiClientVO.builder()
                    .clientId(clientId)
                    .build();

            // 设置系统提示词ID
            if (systemPromptConfigMap.containsKey(clientId)) {
                clientVO.setSystemPromptId(systemPromptConfigMap.get(clientId).getSystemPromptId());
            }

            // 设置模型ID
            if (modelConfigMap.containsKey(clientId)) {
                clientVO.setModelBeanId(String.valueOf(modelConfigMap.get(clientId).getModelId()));
            }

            // 设置MCP工具ID列表
            if (mcpMap.containsKey(clientId)) {
                List<String> mcpBeanIdList = mcpMap.get(clientId).stream()
                        .map(mcp -> String.valueOf(mcp.getToolId()))
                        .collect(Collectors.toList());
                clientVO.setMcpBeanIdList(mcpBeanIdList);
            } else {
                clientVO.setMcpBeanIdList(new ArrayList<>());
            }

            // 设置顾问ID列表
            if (advisorConfigMap.containsKey(clientId)) {
                List<String> advisorBeanIdList = advisorConfigMap.get(clientId).stream()
                        .map(advisor -> String.valueOf(advisor.getAdvisorId()))
                        .collect(Collectors.toList());
                clientVO.setAdvisorBeanIdList(advisorBeanIdList);
            } else {
                clientVO.setAdvisorBeanIdList(new ArrayList<>());
            }

            result.add(clientVO);
        }

        return result;
    }

    @Override
    public List<Long> queryAiClientIdsByAiAgentId(Long aiAgentId) {
        return aiAgentDao.queryClientIdsByAgentId(aiAgentId);
    }

    @Override
    public Long queryAiClientModelIdByAgentId(Long aiAgentId) {
        return aiClientModelConfigDao.queryAiClientModelIdByAgentId(aiAgentId);
    }

    @Override
    public String queryRagKnowledgeTag(Long ragId) {
        AiRagOrder aiRagOrder = aiRagOrderDao.queryRagOrderById(ragId);
        return aiRagOrder.getKnowledgeTag();
    }

    @Override
    public void createTagOrder(AiRagOrderVO aiRagOrderVO) {
        AiRagOrder aiRagOrder = new AiRagOrder();
        aiRagOrder.setRagName(aiRagOrderVO.getRagName());
        aiRagOrder.setKnowledgeTag(aiRagOrderVO.getKnowledgeTag());
        aiRagOrder.setStatus(1);
        aiRagOrder.setCreateTime(new Date());
        aiRagOrder.setUpdateTime(new Date());
        aiRagOrderDao.insert(aiRagOrder);

    }

    @Override
    public List<Long> queryAiClientIds() {
        // 查询所有有效的智能体关联的客户端ID
        return aiAgentDao.queryValidClientIds();
    }

    @Override
    public List<AiAgentTaskScheduleVO> queryAllValidTaskSchedule() {
        List<AiAgentTaskSchedule> aiAgentTaskSchedules = aiAgentTaskScheduleDao.queryAllValidTaskSchedule();

        // 检查查询结果是否为空
        if (null == aiAgentTaskSchedules || aiAgentTaskSchedules.isEmpty()) {
            return Collections.emptyList();
        }

        // 将PO对象转换为VO对象
        return aiAgentTaskSchedules.stream()
                .map(schedule -> {
                    AiAgentTaskScheduleVO vo = new AiAgentTaskScheduleVO();
                    vo.setId(schedule.getId());
                    vo.setAgentId(schedule.getAgentId());
                    vo.setDescription(schedule.getDescription());
                    vo.setCronExpression(schedule.getCronExpression());
                    vo.setTaskParam(schedule.getTaskParam());
                    return vo;
                })
                .collect(Collectors.toList());

    }

    @Override
    public List<Long> queryAllInvalidTaskScheduleIds() {
        return aiAgentTaskScheduleDao.queryAllInvalidTaskScheduleIds();    }

    @Override
    public List<AiClientModelVO> queryAiClientModelVOList() {
        List<AiClientModel> aiClientModels=aiClientModelDao.queryClientModelList();

        if (null == aiClientModels || aiClientModels.isEmpty()) return new ArrayList<>();

        // 使用lambda表达式获取所有AiClientModel的id属性列表
        List<Long> modelIds = aiClientModels.stream().map(AiClientModel::getId).toList();

        List<AiClientModelVO> aiClientModelVOList = new ArrayList<>();
        for (AiClientModel aiClientModel : aiClientModels) {
            AiClientModelVO vo = new AiClientModelVO();
            vo.setId(aiClientModel.getId());
            vo.setModelName(aiClientModel.getModelName());
            vo.setBaseUrl(aiClientModel.getBaseUrl());
            vo.setApiKey(aiClientModel.getApiKey());
            vo.setCompletionsPath(aiClientModel.getCompletionsPath());
            vo.setEmbeddingsPath(aiClientModel.getEmbeddingsPath());
            vo.setModelType(aiClientModel.getModelType());
            vo.setModelVersion(aiClientModel.getModelVersion());
            vo.setTimeout(aiClientModel.getTimeout());
            vo.setAiClientModelToolConfigs(null);
            aiClientModelVOList.add(vo);
        }
        return aiClientModelVOList;
    }

    @Override
    public List<AiAgentClientVO> queryAgentClientConfigByAgentId(Long agentId) {
        List<AiAgentClientLine> aiAgentClientLines = aiAgentClientDao.queryAgentClientConfigByAgentId(agentId);
        if(aiAgentClientLines==null||aiAgentClientLines.isEmpty()){
            return Collections.emptyList();
        }
        return aiAgentClientLines.stream().map(agentClientLine -> {
            AiAgentClientVO vo = new AiAgentClientVO();
            vo.setId(agentClientLine.getId());
            vo.setAiAgentId(agentClientLine.getAgentId());
            vo.setClientIdFrom(agentClientLine.getClientIdFrom());
            vo.setClientIdTo(agentClientLine.getClientIdTo());
            vo.setCondition(agentClientLine.getCondition());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Long queryHeadClientByAgentId(Long agentId) {
       return aiAgentDao.queryHeadClientByAgentId(agentId);
    }

}

