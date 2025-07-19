package top.kimwonjoon.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.model.valobj.AiClientToolMcpVO;
import top.kimwonjoon.domain.agent.model.valobj.AiVectorDatabaseVO;
import top.kimwonjoon.domain.agent.service.armory.AbstractArmorySupport;
import top.kimwonjoon.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName VectorDatabaseNode
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/7/14 09:55
 * @Version 1.0
 */

@Slf4j
@Component
public class VectorDatabaseNode extends AbstractArmorySupport {


    @Resource
    AiClientAdvisorNode aiClientAdvisorNode;
    @Override
    protected String doApply(AiAgentEngineStarterEntity aiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        List<AiVectorDatabaseVO> aiVectorDatabaseVOList = dynamicContext.getValue("aiVectorDataBaseList");
        for(AiVectorDatabaseVO aiVectorDatabaseVO : aiVectorDatabaseVOList){
            createVectorDatabaseDrive(aiVectorDatabaseVO);
        }
        return router(aiAgentEngineStarterEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(AiAgentEngineStarterEntity aiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientAdvisorNode;
    }

    public void createVectorDatabaseDrive(AiVectorDatabaseVO aiVectorDatabaseVO) {
        String dbType = aiVectorDatabaseVO.getDbType();
        switch (dbType) {
            case "pgvector" -> {
                HikariDataSource dataSource = new HikariDataSource();
                dataSource.setDriverClassName("org.postgresql.Driver");
                dataSource.setJdbcUrl("jdbc:postgresql://"+aiVectorDatabaseVO.getUrl()+":"+aiVectorDatabaseVO.getPort()+"/ai-rag-knowledge");
                dataSource.setUsername(aiVectorDatabaseVO.getUserName());
                dataSource.setPassword(aiVectorDatabaseVO.getPassword());
                // 连接池配置
                dataSource.setMaximumPoolSize(5);
                dataSource.setMinimumIdle(2);
                dataSource.setIdleTimeout(30000);
                dataSource.setConnectionTimeout(30000);
                dataSource.setPoolName("PgVectorHikariPool");
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

                registerBean(beanName(aiVectorDatabaseVO.getId()), JdbcTemplate.class,jdbcTemplate);
                log.info("注册了一个pgvector数据库连接池，beanName：{"+beanName(aiVectorDatabaseVO.getId()));
            }
        }

    }


    @Override
    protected String beanName(Long id) {
        return "DataBaseDrive_" + id;
    }



}
