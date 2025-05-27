# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20050
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: 127.0.0.1 (MySQL 5.6.39)
# 数据库: ai-agent-station
# 生成时间: 2025-05-16 13:09:35 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE database if NOT EXISTS `ai-agent-station` default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use `ai-agent-station`;

# 转储表 ai_agent
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_agent`;

CREATE TABLE `ai_agent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_name` varchar(50) NOT NULL COMMENT '智能体名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `channel` varchar(32) DEFAULT NULL COMMENT '渠道类型(agent，chat_stream)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_name` (`agent_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI智能体配置表';

LOCK TABLES `ai_agent` WRITE;
/*!40000 ALTER TABLE `ai_agent` DISABLE KEYS */;

INSERT INTO `ai_agent` (`id`, `agent_name`, `description`, `channel`, `status`, `create_time`, `update_time`)
VALUES
	(1,'自动发帖服务01','CSDN自动发帖，微信公众号通知。','agent',1,'2025-05-04 19:48:05','2025-05-07 09:20:40'),
	(2,'自动发帖服务02','CSDN自动发帖，微信公众号通知。','agent',1,'2025-05-05 12:36:27','2025-05-07 09:20:37'),
	(3,'文件服务测试01','操作本地文件','agent',1,'2025-05-05 13:15:34','2025-05-07 09:20:37'),
	(4,'智能对话体（MCP）','自动发帖，工具服务','chat_stream',1,'2025-05-07 09:18:57','2025-05-07 09:20:32');

/*!40000 ALTER TABLE `ai_agent` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_agent_client
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_agent_client`;

CREATE TABLE `ai_agent_client` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_id` bigint(20) NOT NULL COMMENT '智能体ID',
  `client_id` bigint(20) NOT NULL COMMENT '客户端ID',
  `sequence` int(11) NOT NULL COMMENT '序列号(执行顺序)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_client_seq` (`agent_id`,`client_id`,`sequence`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体-客户端关联表';

LOCK TABLES `ai_agent_client` WRITE;
/*!40000 ALTER TABLE `ai_agent_client` DISABLE KEYS */;

INSERT INTO `ai_agent_client` (`id`, `agent_id`, `client_id`, `sequence`, `create_time`)
VALUES
	(1,1,1,1,'2025-05-04 07:34:46'),
	(2,1,2,2,'2025-05-04 07:34:46'),
	(3,2,2,1,'2025-05-05 12:36:41'),
	(4,3,3,1,'2025-05-05 12:36:41'),
	(5,4,4,1,'2025-05-07 10:28:59');

/*!40000 ALTER TABLE `ai_agent_client` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_agent_task_schedule
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_agent_task_schedule`;

CREATE TABLE `ai_agent_task_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_id` bigint(20) NOT NULL COMMENT '智能体ID',
  `task_name` varchar(64) DEFAULT NULL COMMENT '任务名称',
  `description` varchar(255) DEFAULT NULL COMMENT '任务描述',
  `cron_expression` varchar(50) NOT NULL COMMENT '时间表达式(如: 0/3 * * * * *)',
  `task_param` text COMMENT '任务入参配置(JSON格式)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:无效,1:有效)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent_id` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体任务调度配置表';

LOCK TABLES `ai_agent_task_schedule` WRITE;
/*!40000 ALTER TABLE `ai_agent_task_schedule` DISABLE KEYS */;

INSERT INTO `ai_agent_task_schedule` (`id`, `agent_id`, `task_name`, `description`, `cron_expression`, `task_param`, `status`, `create_time`, `update_time`)
VALUES
	(1,1,'自动发帖','自动发帖和通知','0 0/30 * * * ?','发布CSDN文章',1,'2025-05-05 15:58:58','2025-05-07 12:09:33');

/*!40000 ALTER TABLE `ai_agent_task_schedule` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_client
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_client`;

CREATE TABLE `ai_client` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `client_name` varchar(50) NOT NULL COMMENT '客户端名称',
  `description` varchar(1024) DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_client_name` (`client_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客户端配置表';

LOCK TABLES `ai_client` WRITE;
/*!40000 ALTER TABLE `ai_client` DISABLE KEYS */;

INSERT INTO `ai_client` (`id`, `client_name`, `description`, `status`, `create_time`, `update_time`)
VALUES
	(1,'提示词优化','提示词优化，分为角色、动作、规则、目标等。',1,'2025-05-04 19:47:56','2025-05-05 10:02:55'),
	(2,'自动发帖和通知','自动生成CSDN文章，发送微信公众号消息通知',1,'2025-05-05 10:05:43','2025-05-05 10:09:20'),
	(3,'文件操作服务','文件操作服务',1,'2025-05-05 13:15:57','2025-05-05 13:16:03'),
	(4,'流式对话客户端','流式对话客户端',1,'2025-05-07 09:21:04','2025-05-07 09:21:04');

/*!40000 ALTER TABLE `ai_client` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_client_advisor
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_client_advisor`;

CREATE TABLE `ai_client_advisor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `advisor_name` varchar(50) NOT NULL COMMENT '顾问名称',
  `advisor_type` varchar(50) NOT NULL COMMENT '顾问类型(PromptChatMemory/RagAnswer/SimpleLoggerAdvisor等)',
  `order_num` int(11) DEFAULT '0' COMMENT '顺序号',
  `ext_param` varchar(2048) DEFAULT NULL COMMENT '扩展参数配置，json 记录',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾问配置表';

LOCK TABLES `ai_client_advisor` WRITE;
/*!40000 ALTER TABLE `ai_client_advisor` DISABLE KEYS */;

INSERT INTO `ai_client_advisor` (`id`, `advisor_name`, `advisor_type`, `order_num`, `ext_param`, `status`, `create_time`, `update_time`)
VALUES
	(1,'记忆','ChatMemory',1,'{\n    \"maxMessages\": 200\n}',1,'2025-05-04 08:23:25','2025-05-05 09:01:58'),
	(5,'知识库','RagAnswer',1,'{\n    \"topK\": \"4\",\n    \"filterExpression\": \"knowledge == \'知识库名称\'\"\n}',1,'2025-05-04 08:23:25','2025-05-04 08:58:31');

/*!40000 ALTER TABLE `ai_client_advisor` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_client_advisor_config
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_client_advisor_config`;

CREATE TABLE `ai_client_advisor_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `client_id` bigint(20) NOT NULL COMMENT '客户端ID',
  `advisor_id` bigint(20) NOT NULL COMMENT '顾问ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_client_advisor` (`client_id`,`advisor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户端-顾问关联表';

LOCK TABLES `ai_client_advisor_config` WRITE;
/*!40000 ALTER TABLE `ai_client_advisor_config` DISABLE KEYS */;

INSERT INTO `ai_client_advisor_config` (`id`, `client_id`, `advisor_id`, `create_time`)
VALUES
	(1,1,1,'2025-05-04 08:36:38'),
	(2,1,5,'2025-05-04 08:36:38'),
	(3,2,1,'2025-05-04 08:36:38');

/*!40000 ALTER TABLE `ai_client_advisor_config` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_client_model
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_client_model`;

CREATE TABLE `ai_client_model` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `model_name` varchar(50) NOT NULL COMMENT '模型名称',
  `base_url` varchar(255) NOT NULL COMMENT '基础URL',
  `api_key` varchar(255) NOT NULL COMMENT 'API密钥',
  `completions_path` varchar(100) DEFAULT 'v1/chat/completions' COMMENT '完成路径',
  `embeddings_path` varchar(100) DEFAULT 'v1/embeddings' COMMENT '嵌入路径',
  `model_type` varchar(50) NOT NULL COMMENT '模型类型(openai/azure等)',
  `model_version` varchar(50) DEFAULT 'gpt-4.1' COMMENT '模型版本',
  `timeout` int(11) DEFAULT '180' COMMENT '超时时间(秒)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI接口模型配置表';

LOCK TABLES `ai_client_model` WRITE;
/*!40000 ALTER TABLE `ai_client_model` DISABLE KEYS */;

INSERT INTO `ai_client_model` (`id`, `model_name`, `base_url`, `api_key`, `completions_path`, `embeddings_path`, `model_type`, `model_version`, `timeout`, `status`, `create_time`, `update_time`)
VALUES
	(1,'智能体对话','https://apis.itedus.cn','sk-d4lJnWOkhyLwmcirE3DeBeB1744d48Bf9e394e4e3578C0Bd','v1/chat/completions','v1/embeddings','openai','gpt-4.1-mini',30,1,'2025-05-02 07:30:51','2025-05-07 09:22:46'),
	(2,'流式对话','https://apis.itedus.cn','sk-d4lJnWOkhyLwmcirE3DeBeB1744d48Bf9e394e4e3578C0Bd','v1/chat/completions','v1/embeddings','openai','gpt-4.1-mini',30,1,'2025-05-02 07:30:51','2025-05-07 09:21:59');

/*!40000 ALTER TABLE `ai_client_model` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_client_model_config
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_client_model_config`;

CREATE TABLE `ai_client_model_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `client_id` bigint(20) NOT NULL COMMENT '客户端ID',
  `model_id` bigint(20) NOT NULL COMMENT '模型ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客户端，零部件；模型配置';

LOCK TABLES `ai_client_model_config` WRITE;
/*!40000 ALTER TABLE `ai_client_model_config` DISABLE KEYS */;

INSERT INTO `ai_client_model_config` (`id`, `client_id`, `model_id`, `create_time`)
VALUES
	(1,1,1,'2025-05-02 17:23:22'),
	(2,2,1,'2025-05-02 17:23:22'),
	(3,3,1,'2025-05-05 13:16:18'),
	(4,4,2,'2025-05-07 09:22:25');

/*!40000 ALTER TABLE `ai_client_model_config` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_client_model_tool_config
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_client_model_tool_config`;

CREATE TABLE `ai_client_model_tool_config` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `model_id` bigint(20) DEFAULT NULL COMMENT '模型ID',
  `tool_type` varchar(20) DEFAULT NULL COMMENT '工具类型(mcp/function call)',
  `tool_id` bigint(20) DEFAULT NULL COMMENT 'MCP ID/ function call ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客户端，零部件；模型工具配置';

LOCK TABLES `ai_client_model_tool_config` WRITE;
/*!40000 ALTER TABLE `ai_client_model_tool_config` DISABLE KEYS */;

INSERT INTO `ai_client_model_tool_config` (`id`, `model_id`, `tool_type`, `tool_id`, `create_time`)
VALUES
	(1,2,'mcp',1,'2025-05-02 17:23:22'),
	(2,2,'mcp',2,'2025-05-02 17:23:22'),
	(3,2,'mcp',3,'2025-05-02 17:23:22'),
	(5,2,'mcp',4,'2025-05-02 17:23:22');

/*!40000 ALTER TABLE `ai_client_model_tool_config` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_client_system_prompt
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_client_system_prompt`;

CREATE TABLE `ai_client_system_prompt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `prompt_name` varchar(50) NOT NULL COMMENT '提示词名称',
  `prompt_content` text NOT NULL COMMENT '提示词内容',
  `description` varchar(1024) DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prompt_name` (`prompt_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统提示词配置表';

LOCK TABLES `ai_client_system_prompt` WRITE;
/*!40000 ALTER TABLE `ai_client_system_prompt` DISABLE KEYS */;

INSERT INTO `ai_client_system_prompt` (`id`, `prompt_name`, `prompt_content`, `description`, `status`, `create_time`, `update_time`)
VALUES
	(1,'提示词优化','你是一个专业的AI提示词优化专家。请帮我优化以下prompt，并按照以下格式返回：\n\n# Role: [角色名称]\n\n## Profile\n\n- language: [语言]\n- description: [详细的角色描述]\n- background: [角色背景]\n- personality: [性格特征]\n- expertise: [专业领域]\n- target_audience: [目标用户群]\n\n## Skills\n\n1. [核心技能类别]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n2. [辅助技能类别]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n\n## Rules\n\n1. [基本原则]：\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n2. [行为准则]：\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n3. [限制条件]：\n   - [具体限制]: [详细说明]\n   - [具体限制]: [详细说明]\n   - [具体限制]: [详细说明]\n   - [具体限制]: [详细说明]\n\n## Workflows\n\n- 目标: [明确目标]\n- 步骤 1: [详细说明]\n- 步骤 2: [详细说明]\n- 步骤 3: [详细说明]\n- 预期结果: [说明]\n\n## Initialization\n\n作为[角色名称]，你必须遵守上述Rules，按照Workflows执行任务。\n请基于以上模板，优化并扩展以下prompt，确保内容专业、完整且结构清晰，注意不要携带任何引导词或解释，不要使用代码块包围。','提示词优化，拆分执行动作',1,'2025-05-04 21:14:24','2025-05-05 10:04:25'),
	(3,'发帖和消息通知介绍','你是一个 AI Agent 智能体，可以根据用户输入信息生成文章，并发送到 CSDN 平台以及完成微信公众号消息通知，今天是 {current_date}。\n\n你擅长使用Planning模式，帮助用户生成质量更高的文章。\n\n你的规划应该包括以下几个方面：\n1. 分析用户输入的内容，生成技术文章。\n2. 提取，文章标题（需要含带技术点）、文章内容、文章标签（多个用英文逗号隔开）、文章简述（100字）将以上内容发布文章到CSDN\n3. 获取发送到 CSDN 文章的 URL 地址。\n4. 微信公众号消息通知，平台：CSDN、主题：为文章标题、描述：为文章简述、跳转地址：为发布文章到CSDN获取 URL地址 CSDN文章链接 https 开头的地址。','提示词优化，拆分执行动作',1,'2025-05-04 21:14:24','2025-05-05 10:10:42'),
	(4,'CSDN发布文章','我需要你帮我生成一篇文章，要求如下；\n                                \n                1. 场景为互联网大厂java求职者面试\n                2. 面试管提问 Java 核心知识、JUC、JVM、多线程、线程池、HashMap、ArrayList、Spring、SpringBoot、MyBatis、Dubbo、RabbitMQ、xxl-job、Redis、MySQL、Linux、Docker、设计模式、DDD等不限于此的各项技术问题。\n                3. 按照故事场景，以严肃的面试官和搞笑的水货程序员谢飞机进行提问，谢飞机对简单问题可以回答，回答好了面试官还会夸赞。复杂问题胡乱回答，回答的不清晰。\n                4. 每次进行3轮提问，每轮可以有3-5个问题。这些问题要有技术业务场景上的衔接性，循序渐进引导提问。最后是面试官让程序员回家等通知类似的话术。\n                5. 提问后把问题的答案，写到文章最后，最后的答案要详细讲述出技术点，让小白可以学习下来。\n                                \n                根据以上内容，不要阐述其他信息，请直接提供；文章标题、文章内容、文章标签（多个用英文逗号隔开）、文章简述（100字）\n                                \n                将以上内容发布文章到CSDN。','CSDN发布文章',1,'2025-05-07 12:05:36','2025-05-07 12:05:36'),
	(5,'文章操作测试','在 /Users/fuzhengwei/Desktop 创建文件 file01.txt','文件操作测试',1,'2025-05-07 12:06:03','2025-05-07 12:06:08');

/*!40000 ALTER TABLE `ai_client_system_prompt` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_client_system_prompt_config
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_client_system_prompt_config`;

CREATE TABLE `ai_client_system_prompt_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `client_id` bigint(20) NOT NULL COMMENT '客户端ID',
  `system_prompt_id` bigint(20) NOT NULL COMMENT '系统提示词ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客户端，零部件；模型配置';

LOCK TABLES `ai_client_system_prompt_config` WRITE;
/*!40000 ALTER TABLE `ai_client_system_prompt_config` DISABLE KEYS */;

INSERT INTO `ai_client_system_prompt_config` (`id`, `client_id`, `system_prompt_id`, `create_time`)
VALUES
	(1,1,1,'2025-05-04 20:59:46'),
	(2,2,3,'2025-05-04 20:59:46');

/*!40000 ALTER TABLE `ai_client_system_prompt_config` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_client_tool_config
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_client_tool_config`;

CREATE TABLE `ai_client_tool_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `client_id` bigint(20) NOT NULL COMMENT '客户端ID',
  `tool_type` varchar(20) NOT NULL COMMENT '工具类型(mcp/function call)',
  `tool_id` bigint(20) NOT NULL COMMENT 'MCP ID/ function call ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_client_mcp` (`client_id`,`tool_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户端-MCP关联表';

LOCK TABLES `ai_client_tool_config` WRITE;
/*!40000 ALTER TABLE `ai_client_tool_config` DISABLE KEYS */;

INSERT INTO `ai_client_tool_config` (`id`, `client_id`, `tool_type`, `tool_id`, `create_time`)
VALUES
	(1,2,'mcp',1,'2025-05-04 07:31:54'),
	(2,2,'mcp',2,'2025-05-04 07:31:54'),
	(3,3,'mcp',3,'2025-05-05 13:16:31'),
	(5,4,'mcp',4,'2025-05-05 13:16:31');

/*!40000 ALTER TABLE `ai_client_tool_config` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_client_tool_mcp
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_client_tool_mcp`;

CREATE TABLE `ai_client_tool_mcp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mcp_name` varchar(50) NOT NULL COMMENT 'MCP名称',
  `transport_type` varchar(20) NOT NULL COMMENT '传输类型(sse/stdio)',
  `transport_config` varchar(1024) DEFAULT NULL COMMENT '传输配置(sse/stdio)',
  `request_timeout` int(11) DEFAULT '180' COMMENT '请求超时时间(分钟)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mcp_name` (`mcp_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP客户端配置表';

LOCK TABLES `ai_client_tool_mcp` WRITE;
/*!40000 ALTER TABLE `ai_client_tool_mcp` DISABLE KEYS */;

INSERT INTO `ai_client_tool_mcp` (`id`, `mcp_name`, `transport_type`, `transport_config`, `request_timeout`, `status`, `create_time`, `update_time`)
VALUES
	(1,'CSDN自动发帖','sse','{\n	\"baseUri\":\"http://mcp-server-csdn-app:8101\"\n}',180,1,'2025-05-02 18:43:28','2025-05-05 12:07:52'),
	(2,'微信公众号消息通知','sse','{\n	\"baseUri\":\"http://mcp-server-weixin-app:8102\"\n}',180,1,'2025-05-02 18:43:28','2025-05-05 12:07:57'),
	(3,'filesystem','stdio','{\n    \"filesystem\": {\n        \"command\": \"npx\",\n        \"args\": [\n            \"-y\",\n            \"@modelcontextprotocol/server-filesystem\",\n            \"/Users/fuzhengwei/Desktop\",\n            \"/Users/fuzhengwei/Desktop\"\n        ]\n    }\n}',180,1,'2025-05-05 13:14:42','2025-05-05 13:27:46'),
	(4,'g-search','stdio','{\n    \"g-search\": {\n        \"command\": \"npx\",\n        \"args\": [\n            \"-y\",\n            \"g-search-mcp\"\n        ]\n    }\n}',180,1,'2025-05-05 13:14:42','2025-05-10 08:23:58');

/*!40000 ALTER TABLE `ai_client_tool_mcp` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 ai_rag_order
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_rag_order`;

CREATE TABLE `ai_rag_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rag_name` varchar(50) NOT NULL COMMENT '知识库名称',
  `knowledge_tag` varchar(50) NOT NULL COMMENT '知识标签',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rag_name` (`rag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库配置表';

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
