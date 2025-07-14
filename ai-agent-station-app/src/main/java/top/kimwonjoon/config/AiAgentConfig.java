package top.kimwonjoon.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
public class AiAgentConfig {


    /**
     * 为 MyBatis 创建主数据源
     */
    @Bean("mybatisDataSource")
    @Primary
    public DataSource mybatisDataSource(@Value("${spring.datasource.driver-class-name}") String driverClassName,
                                        @Value("${spring.datasource.url}") String url,
                                        @Value("${spring.datasource.username}") String username,
                                        @Value("${spring.datasource.password}") String password,
                                        @Value("${spring.datasource.hikari.maximum-pool-size:10}") int maximumPoolSize,
                                        @Value("${spring.datasource.hikari.minimum-idle:5}") int minimumIdle,
                                        @Value("${spring.datasource.hikari.idle-timeout:30000}") long idleTimeout,
                                        @Value("${spring.datasource.hikari.connection-timeout:30000}") long connectionTimeout,
                                        @Value("${spring.datasource.hikari.max-lifetime:1800000}") long maxLifetime) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        // 连接池配置
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setMaxLifetime(maxLifetime);
        dataSource.setPoolName("MainHikariPool");
        return dataSource;
    }

    /**
     * 配置 MyBatis 的 SqlSessionFactory
     */
    @Bean("sqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactory(@Qualifier("mybatisDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);

        // 设置MyBatis配置文件位置
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setConfigLocation(resolver.getResource("classpath:/mybatis/config/mybatis-config.xml"));

        // 设置Mapper XML文件位置
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mybatis/mapper/*.xml"));

        return sqlSessionFactoryBean;
    }

    /**
     * 配置 SqlSessionTemplate
     */
    @Bean("sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactoryBean sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory.getObject());
    }

    /**
     * 为 PgVector 创建专用的数据源
     */
    @Bean("pgVectorDataSource")
    public DataSource pgVectorDataSource(@Value("${spring.ai.vectorstore.pgvector.datasource.driver-class-name}") String driverClassName,
                                         @Value("${spring.ai.vectorstore.pgvector.datasource.url}") String url,
                                         @Value("${spring.ai.vectorstore.pgvector.datasource.username}") String username,
                                         @Value("${spring.ai.vectorstore.pgvector.datasource.password}") String password,
                                         @Value("${spring.ai.vectorstore.pgvector.datasource.hikari.maximum-pool-size:5}") int maximumPoolSize,
                                         @Value("${spring.ai.vectorstore.pgvector.datasource.hikari.minimum-idle:2}") int minimumIdle,
                                         @Value("${spring.ai.vectorstore.pgvector.datasource.hikari.idle-timeout:30000}") long idleTimeout,
                                         @Value("${spring.ai.vectorstore.pgvector.datasource.hikari.connection-timeout:30000}") long connectionTimeout) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        // 连接池配置
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setPoolName("PgVectorHikariPool");
        return dataSource;
    }

    /**
     * 为 PgVector 创建专用的 JdbcTemplate
     */
    @Bean("pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    /**
     * -- 删除旧的表（如果存在）
     * DROP TABLE IF EXISTS public.vector_store_ollama;
     * -- 创建新的表，使用UUID作为主键
     * CREATE TABLE public.vector_store_ollama (
     *     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     *     content TEXT NOT NULL,
     *     metadata JSONB,
     *     embedding VECTOR(768)
     * );
     * SELECT * FROM vector_store_ollama
     */
    @Bean("vectorStore")
    public PgVectorStore pgVectorStore(@Value("${spring.ai.ollama.base-url}") String baseUrl,@Qualifier("pgVectorJdbcTemplate") JdbcTemplate jdbcTemplate) {

        OllamaApi ollamaApi=new OllamaApi.Builder()
                .baseUrl(baseUrl)
                .build();
        OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel
                .builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaOptions.builder().model("nomic-embed-text").build())
                .build();
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("vector_store_ollama")
                .build();

    }


}
