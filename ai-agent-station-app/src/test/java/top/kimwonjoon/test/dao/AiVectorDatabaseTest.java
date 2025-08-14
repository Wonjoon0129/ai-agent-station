package top.kimwonjoon.test.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.kimwonjoon.infrastructure.dao.IAiVectorDatabaseDao;
import top.kimwonjoon.infrastructure.dao.po.AiVectorDatabase;

import java.util.List;

/**
 * @ClassName AiVectorDatabaseTest
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/7/14 11:16
 * @Version 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AiVectorDatabaseTest {

    @Autowired
    IAiVectorDatabaseDao aiVectorDatabaseDao;

    @Test
    public void test(){
        List<AiVectorDatabase> aiVectorDatabases = aiVectorDatabaseDao.queryAiVectorDatabase();
        log.info(aiVectorDatabases.toString());
    }
}
