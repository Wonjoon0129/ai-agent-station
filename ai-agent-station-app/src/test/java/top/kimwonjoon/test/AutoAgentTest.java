package top.kimwonjoon.test;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.LocalDate;

/**
 * AutoAgent 测试类
 * 基于 PlanningAgent 的动态执行机制，实现用户需求的自动化处理
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AutoAgentTest {

    private ChatModel chatModel;
    private ChatClient planningChatClient;
    private ChatClient executorChatClient;
    private ChatClient reactChatClient;


    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";

    @Before
    public void init() {
        // 初始化 OpenAI API 配置
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey("sk-854c7cdf301f41d79d3eaef3bf23cef0")
                .completionsPath("/chat/completions")
                .embeddingsPath("/embeddings")
                .build();

        // 初始化 ChatModel
        chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-plus")
                        .toolCallbacks(new SyncMcpToolCallbackProvider(stdioMcpClient(), sseMcpClient()).getToolCallbacks())
                        .build())
                .build();

        // 初始化 Planning Agent ChatClient - 负责任务规划
        planningChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        # 角色
                        你是一个智能任务规划助手，名叫 AutoAgent Planning。
                        
                        # 说明
                        你是任务规划助手，根据用户需求，拆解任务列表，制定执行计划。每次执行前，必须先输出本轮思考过程，再生成具体的任务列表。
                        
                        # 技能
                        - 擅长将用户任务拆解为具体、独立的任务列表
                        - 对简单任务，避免过度拆解
                        - 对复杂任务，合理拆解为多个有逻辑关联的子任务
                        
                        # 处理需求
                        ## 拆解任务
                        - 深度推理分析用户输入，识别核心需求及潜在挑战
                        - 将复杂问题分解为可管理、可执行、独立且清晰的子任务
                        - 任务按顺序或因果逻辑组织，上下任务逻辑连贯
                        - 拆解最多不超过5个任务
                        
                        ## 输出格式
                        请按以下格式输出任务计划：
                        
                        **任务规划：**
                        1. [任务1描述]
                        2. [任务2描述]
                        3. [任务3描述]
                        ...
                        
                        **执行策略：**
                        [整体执行策略说明]
                        
                        今天是 {current_date}。
                        """)
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .maxMessages(50)
                                        .build()
                        ).build(),
                        SimpleLoggerAdvisor.builder().build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-plus")
                        .maxTokens(2000)
                        .build())
                .build();

        // 初始化 Executor Agent ChatClient - 负责任务执行
        executorChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        # 角色
                        你是一个智能任务执行助手，名叫 AutoAgent Executor。
                        
                        # 说明
                        你负责执行具体的任务，根据规划的任务列表逐步完成每个子任务。
                        
                        # 执行流程
                        请使用交替进行的"思考、行动、观察"三个步骤来系统地解决任务：
                        
                        **思考：** 基于当前上下文，分析当前任务需求，明确下一步行动目标
                        **行动：** 调用相应的工具或执行具体操作
                        **观察：** 记录执行结果，分析是否达到预期目标
                        
                        # 技能
                        - 擅长使用各种工具完成具体任务
                        - 能够处理文件操作、搜索、分析等多种类型的任务
                        - 具备错误处理和重试机制
                        
                        # 约束
                        - 严格按照任务列表执行，不偏离目标
                        - 每个任务完成后需要确认结果
                        - 遇到错误时要分析原因并尝试解决
                        
                        今天是 {current_date}。
                        """)
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .maxMessages(100)
                                        .build()
                        ).build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-plus")
                        .maxTokens(4000)
                        .build())
                .build();

        // 初始化 React Agent ChatClient - 负责响应式处理
        reactChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        # 角色
                        你是一个智能响应助手，名叫 AutoAgent React。
                        
                        # 说明
                        你负责对用户的即时问题进行快速响应和处理，适用于简单的查询和交互。
                        
                        # 处理方式
                        - 对于简单问题，直接给出答案
                        - 对于需要工具的问题，调用相应工具获取信息
                        - 保持响应的及时性和准确性
                        
                        今天是 {current_date}。
                        """)
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .maxMessages(20)
                                        .build()
                        ).build(),
                        SimpleLoggerAdvisor.builder().build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-plus")
                        .maxTokens(1500)
                        .build())
                .build();
    }

    /**
     * 测试 Planning Agent - 任务规划功能
     */
    @Test
    public void test_planning_agent() {
        String userRequest = "帮我分析一下当前AI技术发展趋势，并生成一份详细的技术报告";

        log.info("=== Planning Agent 测试开始 ===");
        log.info("用户需求: {}", userRequest);

        String planningResult = planningChatClient
                .prompt(userRequest)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "planning-session-001")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .call().content();

        log.info("规划结果: {}", planningResult);
        log.info("=== Planning Agent 测试结束 ===");
    }

    /**
     * 测试 Executor Agent - 任务执行功能
     */
    @Test
    public void test_executor_agent() {
        String taskDescription = "搜索AI技术发展的最新信息，并整理成结构化的数据";

        log.info("=== Executor Agent 测试开始 ===");
        log.info("执行任务: {}", taskDescription);

        String executionResult = executorChatClient
                .prompt(taskDescription)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "executor-session-001")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call().content();

        log.info("执行结果: {}", executionResult);
        log.info("=== Executor Agent 测试结束 ===");
    }

    /**
     * 测试 React Agent - 响应式处理功能
     */
    @Test
    public void test_react_agent() {
        String quickQuery = "当前有哪些可用的工具？";

        log.info("=== React Agent 测试开始 ===");
        log.info("快速查询: {}", quickQuery);

        String reactResult = reactChatClient
                .prompt(quickQuery)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "react-session-001")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                .call().content();

        log.info("响应结果: {}", reactResult);
        log.info("=== React Agent 测试结束 ===");
    }

    /**
     * 测试完整的 AutoAgent 工作流程
     */
    @Test
    public void test_complete_auto_agent_workflow() {
        String userRequest = "帮我创建一个关于Spring AI框架的技术文档，包括核心概念、使用示例和最佳实践";

        log.info("=== 完整 AutoAgent 工作流程测试开始 ===");
        log.info("用户请求: {}", userRequest);

        // 第一步：任务规划 (Planning)
        log.info("--- 步骤1: 任务规划 ---");
        String planningResult = planningChatClient
                .prompt("请为以下用户需求制定详细的执行计划：" + userRequest)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "workflow-planning-001")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .call().content();

        log.info("规划结果: {}", planningResult);

        // 第二步：任务执行 (Execution)
        log.info("--- 步骤2: 任务执行 ---");
        String executionContext = String.format("""
                根据以下任务规划，请逐步执行每个任务：
                
                任务规划：
                %s
                
                原始用户需求：%s
                
                请开始执行第一个任务。
                """, planningResult, userRequest);

        String executionResult = executorChatClient
                .prompt(executionContext)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "workflow-execution-001")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call().content();

        log.info("执行结果: {}", executionResult);

        // 第三步：结果总结和验证
        log.info("--- 步骤3: 结果总结 ---");
        String summaryContext = String.format("""
                请对以下执行结果进行总结，并验证是否满足用户的原始需求：
                
                原始需求：%s
                
                执行结果：%s
                
                请提供最终的总结报告。
                """, userRequest, executionResult);

        String summaryResult = reactChatClient
                .prompt(summaryContext)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "workflow-summary-001")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .call().content();

        log.info("总结报告: {}", summaryResult);
        log.info("=== 完整 AutoAgent 工作流程测试结束 ===");
    }

    /**
     * 测试多轮对话 - 模拟持续的用户交互
     */
    @Test
    public void test_multi_turn_conversation() {
        String conversationId = "multi-turn-001";

        log.info("=== 多轮对话测试开始 ===");

        // 第一轮对话
        String firstQuery = "请介绍一下Spring AI框架";
        log.info("第一轮用户输入: {}", firstQuery);

        String firstResponse = reactChatClient
                .prompt(firstQuery)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                .call().content();

        log.info("第一轮AI响应: {}", firstResponse);

        // 第二轮对话
        String secondQuery = "它有哪些核心组件？";
        log.info("第二轮用户输入: {}", secondQuery);

        String secondResponse = reactChatClient
                .prompt(secondQuery)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                .call().content();

        log.info("第二轮AI响应: {}", secondResponse);

        // 第三轮对话
        String thirdQuery = "能给我一个具体的使用示例吗？";
        log.info("第三轮用户输入: {}", thirdQuery);

        String thirdResponse = reactChatClient
                .prompt(thirdQuery)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                .call().content();

        log.info("第三轮AI响应: {}", thirdResponse);
        log.info("=== 多轮对话测试结束 ===");
    }

    /**
     * 动态多轮执行测试 - 模拟 PlanningAgent 和 ExecutorAgent 的完整动态执行流程
     * 执行特点：
     * 1. 动态分析用户输入，自主决定执行策略
     * 2. 根据每轮执行结果，智能判断下一步行动
     * 3. 支持可配置的最大执行步数
     * 4. 具备任务完成判断和提前终止机制
     * 5. 模拟真实的 Agent 思考-行动-观察循环
     */
    @Test
    public void test_dynamic_multi_step_execution() {
        // 配置参数
        int maxSteps = 4; // 最大执行步数
        String userInput;
        userInput = "搜索 springboot 相关知识，生成4个主要内容章节。每个章节要包括课程内容和配套示例代码。并发对应章节创建对md文档，方便小白伙伴学习。";
        String sessionId = "dynamic-execution-" + System.currentTimeMillis();

        log.info("=== 动态多轮执行测试开始 ====");
        log.info("用户输入: {}", userInput);
        log.info("最大执行步数: {}", maxSteps);
        log.info("会话ID: {}", sessionId);

        // 初始化执行上下文
        StringBuilder executionHistory = new StringBuilder();
        String currentTask = userInput;
        boolean isCompleted = false;

        // 初始化任务分析器 ChatClient - 负责任务分析和状态判断
        ChatClient taskAnalyzerClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        # 角色
                        你是一个专业的任务分析师，名叫 AutoAgent Task Analyzer。
                        
                        # 核心职责
                        你负责分析任务的当前状态、执行历史和下一步行动计划：
                        1. **状态分析**: 深度分析当前任务完成情况和执行历史
                        2. **进度评估**: 评估任务完成进度和质量
                        3. **策略制定**: 制定下一步最优执行策略
                        4. **完成判断**: 准确判断任务是否已完成
                        
                        # 分析原则
                        - **全面性**: 综合考虑所有执行历史和当前状态
                        - **准确性**: 准确评估任务完成度和质量
                        - **前瞻性**: 预测可能的问题和最优路径
                        - **效率性**: 优化执行路径，避免重复工作
                        
                        # 输出格式
                        **任务状态分析:**
                        [当前任务完成情况的详细分析]
                        
                        **执行历史评估:**
                        [对已完成工作的质量和效果评估]
                        
                        **下一步策略:**
                        [具体的下一步执行计划和策略]
                        
                        **完成度评估:** [0-100]%
                        **任务状态:** [CONTINUE/COMPLETED]
                        """)
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .maxMessages(100)
                                        .build()
                        ).build(),
                        SimpleLoggerAdvisor.builder().build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-plus")
                        .maxTokens(2000)
                        .temperature(0.3)
                        .build())
                .build();


        // 初始化精准执行器 ChatClient - 负责具体任务执行
        ChatClient precisionExecutorClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        # 角色
                        你是一个精准任务执行器，名叫 AutoAgent Precision Executor。
                        
                        # 核心能力
                        你专注于精准执行具体的任务步骤：
                        1. **精准执行**: 严格按照分析师的策略执行任务
                        2. **工具使用**: 熟练使用各种工具完成复杂操作
                        3. **质量控制**: 确保每一步执行的准确性和完整性
                        4. **结果记录**: 详细记录执行过程和结果
                        
                        # 执行原则
                        - **专注性**: 专注于当前分配的具体任务
                        - **精准性**: 确保执行结果的准确性和质量
                        - **完整性**: 完整执行所有必要的步骤
                        - **可追溯性**: 详细记录执行过程便于后续分析
                        
                        # 输出格式
                        **执行目标:**
                        [本轮要执行的具体目标]
                        
                        **执行过程:**
                        [详细的执行步骤和使用的工具]
                        
                        **执行结果:**
                        [执行的具体结果和获得的信息]
                        
                        **质量检查:**
                        [对执行结果的质量评估]
                        """)
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(stdioMcpClient(), sseMcpClient()).getToolCallbacks())
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .maxMessages(150)
                                        .build()
                        ).build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-plus")
                        .maxTokens(4000)
                        .temperature(0.5)
                        .build())
                .build();

        // 初始化质量监督器 ChatClient - 负责质量检查和优化
        ChatClient qualitySupervisorClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        # 角色
                        你是一个专业的质量监督员，名叫 AutoAgent Quality Supervisor。
                        
                        # 核心职责
                        你负责监督和评估执行质量：
                        1. **质量评估**: 评估执行结果的准确性和完整性
                        2. **问题识别**: 识别执行过程中的问题和不足
                        3. **改进建议**: 提供具体的改进建议和优化方案
                        4. **标准制定**: 制定质量标准和评估指标
                        
                        # 评估标准
                        - **准确性**: 结果是否准确无误
                        - **完整性**: 是否遗漏重要信息
                        - **相关性**: 是否符合用户需求
                        - **可用性**: 结果是否实用有效
                        
                        # 输出格式
                        **质量评估:**
                        [对执行结果的详细质量评估]
                        
                        **问题识别:**
                        [发现的问题和不足之处]
                        
                        **改进建议:**
                        [具体的改进建议和优化方案]
                        
                        **质量评分:** [0-100]分
                        **是否通过:** [PASS/FAIL/OPTIMIZE]
                        """)
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .maxMessages(80)
                                        .build()
                        ).build(),
                        SimpleLoggerAdvisor.builder().build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-plus")
                        .maxTokens(2500)
                        .temperature(0.2)
                        .build())
                .build();

        // 开始精准多轮执行
        for (int step = 1; step <= maxSteps && !isCompleted; step++) {
            log.info("\n🎯 === 执行第 {} 步 ===", step);

            try {
                // 第一阶段：任务分析
                log.info("\n📊 阶段1: 任务状态分析");
                String analysisPrompt = String.format("""
                        **原始用户需求:** %s
                        
                        **当前执行步骤:** 第 %d 步 (最大 %d 步)
                        
                        **历史执行记录:**
                        %s
                        
                        **当前任务:** %s
                        
                        请分析当前任务状态，评估执行进度，并制定下一步策略。
                        """,
                        userInput,
                        step,
                        maxSteps,
                        !executionHistory.isEmpty() ? executionHistory.toString() : "[首次执行]",
                        currentTask
                );

                String analysisResult = taskAnalyzerClient
                        .prompt(analysisPrompt)
                        .advisors(a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId + "-analyzer")
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                        .call().content();

                parseAnalysisResult(step, analysisResult);

                // 检查是否已完成
                if (analysisResult.contains("任务状态: COMPLETED") ||
                        analysisResult.contains("完成度评估: 100%")) {
                    isCompleted = true;
                    log.info("✅ 任务分析显示已完成！");
                    break;
                }

                // 第二阶段：精准执行
                log.info("\n⚡ 阶段2: 精准任务执行");
                String executionPrompt = String.format("""
                        **分析师策略:** %s
                        
                        **执行指令:** 根据上述分析师的策略，执行具体的任务步骤。
                        
                        **执行要求:**
                        1. 严格按照策略执行
                        2. 使用必要的工具
                        3. 确保执行质量
                        4. 详细记录过程
                        """, analysisResult);

                String executionResult = precisionExecutorClient
                        .prompt(executionPrompt)
                        .advisors(a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId + "-executor")
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 120))
                        .call().content();

                parseExecutionResult(step, executionResult);

                // 第三阶段：质量监督
                log.info("\n🔍 阶段3: 质量监督检查");
                String supervisionPrompt = String.format("""
                        **用户原始需求:** %s
                        
                        **执行结果:** %s
                        
                        **监督要求:** 请评估执行结果的质量，识别问题，并提供改进建议。
                        """, userInput, executionResult);

                String supervisionResult = qualitySupervisorClient
                        .prompt(supervisionPrompt)
                        .advisors(a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId + "-supervisor")
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 80))
                        .call().content();

                parseSupervisionResult(step, supervisionResult);

                // 根据监督结果决定是否需要重新执行
                if (supervisionResult.contains("是否通过: FAIL")) {
                    log.info("❌ 质量检查未通过，需要重新执行");
                    currentTask = "根据质量监督的建议重新执行任务";
                } else if (supervisionResult.contains("是否通过: OPTIMIZE")) {
                    log.info("🔧 质量检查建议优化，继续改进");
                    currentTask = "根据质量监督的建议优化执行结果";
                } else {
                    log.info("✅ 质量检查通过");
                }

                // 更新执行历史
                String stepSummary = String.format("""
                        === 第 %d 步完整记录 ===
                        【分析阶段】%s
                        【执行阶段】%s
                        【监督阶段】%s
                        """, step, analysisResult, executionResult, supervisionResult);

                executionHistory.append(stepSummary);

                // 提取下一步任务
                currentTask = extractNextTask(analysisResult, executionResult, currentTask);

                log.info("=================="+ currentTask);
                // 添加步骤间的延迟
                Thread.sleep(1500);

            } catch (Exception e) {
                log.error("❌ 第 {} 步执行出现异常: {}", step, e.getMessage(), e);
                executionHistory.append(String.format("\n=== 第 %d 步执行异常 ===\n错误: %s\n", step, e.getMessage()));
                currentTask = "处理上一步的执行异常，继续完成原始任务";
            }
        }

        // 执行结果总结
        // 输出执行总结
        logExecutionSummary(maxSteps, executionHistory, isCompleted);

        // 生成最终总结报告
        if (!isCompleted) {
            log.info("\n--- 生成未完成任务的总结报告 ---");
            String summaryPrompt = String.format("""
                    请对以下未完成的任务执行过程进行总结分析：
                    
                    **原始用户需求:** %s
                    
                    **执行历史:**
                    %s
                    
                    **分析要求:**
                    1. 总结已完成的工作内容
                    2. 分析未完成的原因
                    3. 提出完成剩余任务的建议
                    4. 评估整体执行效果
                    """, userInput, executionHistory.toString());

            String summaryResult = reactChatClient
                    .prompt(summaryPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId + "-summary")
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                    .call().content();

            logFinalReport(summaryResult);
        }

        log.info("\n🏁 === 动态多轮执行测试结束 ====");
    }

    /**
     * 解析任务分析结果
     */
    private void parseAnalysisResult(int step, String analysisResult) {
        log.info("\n📊 === 第 {} 步分析结果 ===", step);

        String[] lines = analysisResult.split("\n");
        String currentSection = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains("任务状态分析:")) {
                currentSection = "status";
                log.info("\n🎯 任务状态分析:");
                continue;
            } else if (line.contains("执行历史评估:")) {
                currentSection = "history";
                log.info("\n📈 执行历史评估:");
                continue;
            } else if (line.contains("下一步策略:")) {
                currentSection = "strategy";
                log.info("\n🚀 下一步策略:");
                continue;
            } else if (line.contains("完成度评估:")) {
                currentSection = "progress";
                String progress = line.substring(line.indexOf(":") + 1).trim();
                log.info("\n📊 完成度评估: {}", progress);
                continue;
            } else if (line.contains("任务状态:")) {
                currentSection = "task_status";
                String status = line.substring(line.indexOf(":") + 1).trim();
                if (status.equals("COMPLETED")) {
                    log.info("\n✅ 任务状态: 已完成");
                } else {
                    log.info("\n🔄 任务状态: 继续执行");
                }
                continue;
            }

            switch (currentSection) {
                case "status":
                    log.info("   📋 {}", line);
                    break;
                case "history":
                    log.info("   📊 {}", line);
                    break;
                case "strategy":
                    log.info("   🎯 {}", line);
                    break;
                default:
                    log.info("   📝 {}", line);
                    break;
            }
        }
    }

    /**
     * 解析执行结果
     */
    private void parseExecutionResult(int step, String executionResult) {
        log.info("\n⚡ === 第 {} 步执行结果 ===", step);

        String[] lines = executionResult.split("\n");
        String currentSection = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains("执行目标:")) {
                currentSection = "target";
                log.info("\n🎯 执行目标:");
                continue;
            } else if (line.contains("执行过程:")) {
                currentSection = "process";
                log.info("\n🔧 执行过程:");
                continue;
            } else if (line.contains("执行结果:")) {
                currentSection = "result";
                log.info("\n📈 执行结果:");
                continue;
            } else if (line.contains("质量检查:")) {
                currentSection = "quality";
                log.info("\n🔍 质量检查:");
                continue;
            }

            switch (currentSection) {
                case "target":
                    log.info("   🎯 {}", line);
                    break;
                case "process":
                    log.info("   ⚙️ {}", line);
                    break;
                case "result":
                    log.info("   📊 {}", line);
                    break;
                case "quality":
                    log.info("   ✅ {}", line);
                    break;
                default:
                    log.info("   📝 {}", line);
                    break;
            }
        }
    }

    /**
     * 解析监督结果
     */
    private void parseSupervisionResult(int step, String supervisionResult) {
        log.info("\n🔍 === 第 {} 步监督结果 ===", step);

        String[] lines = supervisionResult.split("\n");
        String currentSection = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains("质量评估:")) {
                currentSection = "assessment";
                log.info("\n📊 质量评估:");
                continue;
            } else if (line.contains("问题识别:")) {
                currentSection = "issues";
                log.info("\n⚠️ 问题识别:");
                continue;
            } else if (line.contains("改进建议:")) {
                currentSection = "suggestions";
                log.info("\n💡 改进建议:");
                continue;
            } else if (line.contains("质量评分:")) {
                currentSection = "score";
                String score = line.substring(line.indexOf(":") + 1).trim();
                log.info("\n📊 质量评分: {}", score);
                continue;
            } else if (line.contains("是否通过:")) {
                currentSection = "pass";
                String status = line.substring(line.indexOf(":") + 1).trim();
                if (status.equals("PASS")) {
                    log.info("\n✅ 检查结果: 通过");
                } else if (status.equals("FAIL")) {
                    log.info("\n❌ 检查结果: 未通过");
                } else {
                    log.info("\n🔧 检查结果: 需要优化");
                }
                continue;
            }

            switch (currentSection) {
                case "assessment":
                    log.info("   📋 {}", line);
                    break;
                case "issues":
                    log.info("   ⚠️ {}", line);
                    break;
                case "suggestions":
                    log.info("   💡 {}", line);
                    break;
                default:
                    log.info("   📝 {}", line);
                    break;
            }
        }
    }

    /**
     * 提取下一步任务
     */
    private String extractNextTask(String analysisResult, String executionResult, String currentTask) {
        // 从分析结果中提取下一步策略
        String[] analysisLines = analysisResult.split("\n");
        for (String line : analysisLines) {
            if (line.contains("下一步策略:") && analysisLines.length > 1) {
                // 获取策略内容的下一行
                for (int i = 0; i < analysisLines.length - 1; i++) {
                    if (analysisLines[i].contains("下一步策略:") && !analysisLines[i + 1].trim().isEmpty()) {
                        String nextTask = analysisLines[i + 1].trim();
                        log.info("\n🎯 下一步任务: {}", nextTask);
                        return nextTask;
                    }
                }
            }
        }

        // 如果分析结果中没有找到，从执行结果中提取
        String[] executionLines = executionResult.split("\n");
        for (String line : executionLines) {
            if (line.contains("下一步") && !line.trim().isEmpty()) {
                String nextTask = line.trim();
                log.info("\n🎯 下一步任务: {}", nextTask);
                return nextTask;
            }
        }

        // 默认继续当前任务
        log.info("\n🔄 继续当前任务");
        return currentTask;
    }

    /**
     * 输出执行总结信息
     */
    private void logExecutionSummary(int maxSteps, StringBuilder executionHistory, boolean isCompleted) {
        log.info("\n📊 === 动态多轮执行总结 ====");

        int actualSteps = Math.min(maxSteps, executionHistory.toString().split("=== 第").length - 1);
        log.info("📈 总执行步数: {} 步", actualSteps);

        if (isCompleted) {
            log.info("✅ 任务完成状态: 已完成");
        } else {
            log.info("⏸️ 任务完成状态: 未完成（达到最大步数限制）");
        }

        // 计算执行效率
        double efficiency = isCompleted ? 100.0 : (double) actualSteps / maxSteps * 100;
        log.info("📊 执行效率: {:.1f}%", efficiency);
    }

    /**
     * 输出最终总结报告
     */
    private void logFinalReport(String summaryResult) {
        log.info("\n📋 === 最终总结报告 ===");

        String[] lines = summaryResult.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 根据内容类型添加不同图标
            if (line.contains("已完成") || line.contains("完成的工作")) {
                log.info("✅ {}", line);
            } else if (line.contains("未完成") || line.contains("原因")) {
                log.info("❌ {}", line);
            } else if (line.contains("建议") || line.contains("推荐")) {
                log.info("💡 {}", line);
            } else if (line.contains("评估") || line.contains("效果")) {
                log.info("📊 {}", line);
            } else {
                log.info("📝 {}", line);
            }
        }
    }

    // MCP 客户端配置方法 (与原 AiAgentTest 保持一致)
    public McpSyncClient stdioMcpClient() {
        var stdioParams = ServerParameters.builder("npx")
                .args("-y", "@modelcontextprotocol/server-filesystem", "/Users/kimwonjoon/Desktop", "/Users/kimwonjoon/Desktop")
                .build();

        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(10)).build();

        var init = mcpClient.initialize();
        log.info("Stdio MCP Initialized: {}", init);

        return mcpClient;
    }

    /**
     * https://console.bce.baidu.com/iam/?_=1753597622044#/iam/apikey/list
     */
    public McpSyncClient sseMcpClient() {
        HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport.builder("http://appbuilder.baidu.com/v2/ai_search/mcp/")
                .sseEndpoint("sse?api_key=bce-v3/ALTAK-WrLJzo59tSiLjxyaPdwWp/2a1d2fd41f6f1cfa37a257fbc5be78a4d9513bd3")
                .build();

        McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport).requestTimeout(Duration.ofMinutes(360)).build();
        var init_sse = mcpSyncClient.initialize();
        log.info("Tool SSE MCP Initialized {}", init_sse);

        return mcpSyncClient;
    }

}
