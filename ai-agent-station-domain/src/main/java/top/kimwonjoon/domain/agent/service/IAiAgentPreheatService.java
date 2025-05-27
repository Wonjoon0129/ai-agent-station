package top.kimwonjoon.domain.agent.service;

/**
 * AiAgent 装配服务接口
 */
public interface IAiAgentPreheatService {

    /**
     * 服务预热，启动时触达
     */
    void preheat() throws Exception;

    void preheat(Long aiClientId) throws Exception;

}
