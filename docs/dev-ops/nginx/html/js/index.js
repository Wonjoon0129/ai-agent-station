const chatArea = document.getElementById('chatArea');
const messageInput = document.getElementById('messageInput');
const submitBtn = document.getElementById('submitBtn');
const newChatBtn = document.getElementById('newChatBtn');
const chatList = document.getElementById('chatList');
const welcomeMessage = document.getElementById('welcomeMessage');
const toggleSidebarBtn = document.getElementById('toggleSidebar');
const sidebar = document.getElementById('sidebar');
const clearAllChatsBtn = document.getElementById('clearAllChatsBtn');
let currentEventSource = null;
let currentChatId = null;
// 添加文件处理相关变量
let selectedImages = [];

imageInput.addEventListener('change', function() {
    selectedImages = Array.from(this.files);
    // 可选：更新UI显示已选图片
});

// 文件选择处理
function handleFileSelect() {
    document.getElementById('fileInput').click();
}

document.getElementById('fileInput').addEventListener('change', function(e) {
    selectedFiles = Array.from(e.target.files);
    updateFileListDisplay();
});
// 更新文件列表显示


function updateFileListDisplay() {
    const fileListDiv = document.getElementById('fileList');
    fileListDiv.innerHTML = selectedFiles.map((file, index) => `
        <div class="flex items-center gap-1 bg-gray-100 px-2 py-1 rounded text-xs">
            <span class="truncate">${file.name}</span>
            <button onclick="removeFile(${index})" class="text-red-500 hover:text-red-700">&times;</button>
        </div>
    `).join('');
}

// 删除文件
function removeFile(index) {
    selectedFiles.splice(index, 1);
    updateFileListDisplay();
    document.getElementById('fileInput').value = ''; // 清空input以允许重复选择相同文件
}
// 本地存储工具函数
async function setStorageItem(key, value) {
    try {
        localStorage.setItem(key, value);
        return true;
    } catch (error) {
        console.error('Storage error:', error);
        return false;
    }
}

async function getStorageItem(key) {
    try {
        return localStorage.getItem(key);
    } catch (error) {
        console.error('Storage error:', error);
        return null;
    }
}



// 获取知识库列表
document.addEventListener('DOMContentLoaded', function() {
    // 获取知识库列表
    function loadRagOptions() {

        fetch('http://localhost:8091/ai-agent-station/api/v1/ai/admin/rag/queryAllValidRagOrder', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('网络响应不正常');
                }
                return response.json();
            })
            .then(data => {
            const ragSelect = document.getElementById('ragSelect');
            // 清空现有选项
            while (ragSelect.options.length > 1) {
                ragSelect.remove(1);
            }

            // 添加从服务器获取的选项
            if (data && data.length > 0) {
                data.forEach((item) => {
                    const option = document.createElement('option');
                    option.value = item.id;
                    option.textContent = item.ragName;
                    // 如果是第一个选项，设置为选中状态
                    ragSelect.appendChild(option);
                });
            }
        })
            .catch(error => {
                console.error('获取AI代理列表失败:', error);
            });
    };

    // 获取AI代理列表
    function fetchAiAgents() {
        // 发送请求获取AI代理列表
        fetch('http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/queryAllAgentConfigListByChannel', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'channel=agent'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('网络响应不正常');
            }
            return response.json();
        })
        .then(data => {
            const aiAgentSelect = document.getElementById('aiAgent');
            // 清空现有选项
            while (aiAgentSelect.options.length > 1) {
                aiAgentSelect.remove(1);
            }

            // 添加从服务器获取的选项
            if (data && data.length > 0) {
                data.forEach((agent, index) => {
                    const option = document.createElement('option');
                    option.value = agent.id;
                    option.textContent = agent.agentName;
                    // 如果是第一个选项，设置为选中状态
                    aiAgentSelect.appendChild(option);
                });
            }
        })
        .catch(error => {
            console.error('获取AI代理列表失败:', error);
        });
    }

    // 获取提示词列表
    function fetchPromptTemplates() {
        fetch('http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/system/prompt/queryAllSystemPromptConfig', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('网络响应不正常');
            }
            return response.json();
        })
        .then(data => {
            const promptSelect = document.getElementById('promptSelect');
            // 清空现有选项（保留第一个默认选项）
            while (promptSelect.options.length > 1) {
                promptSelect.remove(1);
            }

            // 添加从服务器获取的选项
            if (data && data.length > 0) {
                data.forEach(prompt => {
                    const option = document.createElement('option');
                    option.value = prompt.promptContent;
                    option.textContent = prompt.promptName;
                    promptSelect.appendChild(option);
                });
            }
        })
        .catch(error => {
            console.error('获取提示词列表失败:', error);
        });
    }

    // 初始化加载
    loadRagOptions();
    // 获取AI代理列表
    fetchAiAgents();
    // 获取提示词列表
    fetchPromptTemplates();

    // 添加提示词选择事件监听
    const promptSelect = document.getElementById('promptSelect');
    promptSelect.addEventListener('change', function() {
        const selectedPrompt = this.value;
        if (selectedPrompt) {
            const messageInput = document.getElementById('messageInput');
            // 如果输入框已有内容，则在内容前添加提示词
            if (messageInput.value.trim()) {
                messageInput.value = selectedPrompt + '\n\n' + messageInput.value;
            } else {
                messageInput.value = selectedPrompt;
            }
            // 重置选择框
            this.value = '';
            // 聚焦到输入框
            messageInput.focus();
        }
    });
});

async function createNewChat() {
    const chatId = Date.now().toString();
    currentChatId = chatId;
    await setStorageItem('currentChatId', chatId);
    await setStorageItem(`chat_${chatId}`, JSON.stringify({
        name: `新聊天 ${new Date().toLocaleTimeString('zh-CN', { hour12: false })}`,
        messages: []
    }));
    await updateChatList();
    clearChatArea();
    return chatId;
}

function deleteChat(chatId) {
    if (confirm('确定要删除这个聊天记录吗？')) {
        localStorage.removeItem(`chat_${chatId}`); // Remove the chat from localStorage
        if (currentChatId === chatId) { // If the current chat is being deleted
            createNewChat(); // Create a new chat
        }
        updateChatList(); // Update the chat list to reflect changes
    }
}

function updateChatList() {
    chatList.innerHTML = '';
    const chats = Object.keys(localStorage)
      .filter(key => key.startsWith('chat_'));

    const currentChatIndex = chats.findIndex(key => key.split('_')[1] === currentChatId);
    if (currentChatIndex!== -1) {
        const currentChat = chats[currentChatIndex];
        chats.splice(currentChatIndex, 1);
        chats.unshift(currentChat);
    }

    chats.forEach(chatKey => {
        let chatData = JSON.parse(localStorage.getItem(chatKey));
        const chatId = chatKey.split('_')[1];

        // 数据迁移：将旧数组格式转换为新对象格式
        if (Array.isArray(chatData)) {
            chatData = {
                name: `聊天 ${new Date(parseInt(chatId)).toLocaleDateString()}`,
                messages: chatData
            };
            localStorage.setItem(chatKey, JSON.stringify(chatData));
        }

        const li = document.createElement('li');
        li.className = `chat-item flex items-center justify-between p-2 hover:bg-gray-100 rounded-lg cursor-pointer transition-colors ${chatId === currentChatId? 'bg-blue-50' : ''}`;
        li.innerHTML = `
            <div class="flex-1">
                <div class="text-sm font-medium">${chatData.name}</div>
                <div class="text-xs text-gray-400">${new Date(parseInt(chatId)).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })}</div>
            </div>
            <div class="chat-actions flex items-center gap-1 opacity-0 transition-opacity duration-200">
                <button class="p-1 hover:bg-gray-200 rounded text-gray-500" onclick="renameChat('${chatId}')">重命名</button>
                <button class="p-1 hover:bg-red-200 rounded text-red-500" onclick="deleteChat('${chatId}')">删除</button>
            </div>
        `;
        li.addEventListener('click', (e) => {
            if (!e.target.closest('.chat-actions')) {
                loadChat(chatId);
            }
        });
        li.addEventListener('mouseenter', () => {
            li.querySelector('.chat-actions').classList.remove('opacity-0');
        });
        li.addEventListener('mouseleave', () => {
            li.querySelector('.chat-actions').classList.add('opacity-0');
        });
        chatList.appendChild(li);
    });
}

let currentContextMenu = null;
// 优化后的上下文菜单
function showChatContextMenu(event, chatId) {
    event.stopPropagation();
    closeContextMenu();

    const buttonRect = event.target.closest('button').getBoundingClientRect();
    const menu = document.createElement('div');
    menu.className = 'context-menu';
    menu.style.position = 'fixed';
    menu.style.left = `${buttonRect.left}px`;
    menu.style.top = `${buttonRect.bottom + 4}px`;

    menu.innerHTML = `
        <div class="context-menu-item" onclick="renameChat('${chatId}')">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
            </svg>
            重命名
        </div>
        <div class="context-menu-item text-red-500" onclick="deleteChat('${chatId}')">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
            </svg>
            删除
        </div>
    `;

    document.body.appendChild(menu);
    currentContextMenu = menu;

    // 点击外部关闭菜单
    setTimeout(() => {
        document.addEventListener('click', closeContextMenu, { once: true });
    });
}

function closeContextMenu() {
    if (currentContextMenu) {
        currentContextMenu.remove();
        currentContextMenu = null;
    }
}

function renameChat(chatId) {
    const chatKey = `chat_${chatId}`;
    const chatData = JSON.parse(localStorage.getItem(chatKey));
    const currentName = chatData.name || `聊天 ${new Date(parseInt(chatId)).toLocaleString()}`;
    let newName = prompt('请输入新的聊天名称', currentName);
    if (newName && newName.length > 10) {
        newName = newName.substring(0, 10);
    }

    if (newName) {
        chatData.name = newName;
        localStorage.setItem(chatKey, JSON.stringify(chatData));
        updateChatList();
    }
}

function loadChat(chatId) {
    currentChatId = chatId;
    localStorage.setItem('currentChatId', chatId);
    clearChatArea();
    const chatData = JSON.parse(localStorage.getItem(`chat_${chatId}`) || { messages: [] });
    chatData.messages.forEach(msg => {
        appendMessage(msg.content, msg.isAssistant, false);
    });
    updateChatList()
}

function clearChatArea() {
    chatArea.innerHTML = '';
    welcomeMessage.style.display = 'flex';
}

function appendMessage(content, isAssistant = false, saveToStorage = true) {
    loadingSpinner.classList.add('hidden');

    welcomeMessage.style.display = 'none';
    const messageDiv = document.createElement('div');
    messageDiv.className = `max-w-4xl mx-auto mb-4 p-4 rounded-lg ${isAssistant ? 'bg-gray-100' : 'bg-white border'} markdown-body relative`;

    const renderedContent = DOMPurify.sanitize(marked.parse(content));
    messageDiv.innerHTML = renderedContent;

    // 添加复制按钮
    const copyBtn = document.createElement('button');
    copyBtn.className = 'absolute top-2 right-2 p-1 bg-gray-200 rounded-md text-xs';
    copyBtn.textContent = '复制';
    copyBtn.onclick = () => {
        navigator.clipboard.writeText(content).then(() => {
            copyBtn.textContent = '已复制';
            setTimeout(() => copyBtn.textContent = '复制', 2000);
        });
    };
    messageDiv.appendChild(copyBtn);

    chatArea.appendChild(messageDiv);
    chatArea.scrollTop = chatArea.scrollHeight;

    // 仅在需要时保存到本地存储
    if (saveToStorage && currentChatId) {
        // 确保读取和保存完整的数据结构
        const chatData = JSON.parse(localStorage.getItem(`chat_${currentChatId}`) || '{"name": "新聊天", "messages": []}');
        chatData.messages.push({ content, isAssistant });

        // 如果是用户的第一条消息，将其作为聊天名称
        if (!isAssistant && chatData.messages.length === 1) {
            const nameContent = content.length > 20 ? content.substring(0, 20) + '...' : content;
            chatData.name = nameContent;
        }

        localStorage.setItem(`chat_${currentChatId}`, JSON.stringify(chatData));
        updateChatList(); // 更新聊天列表以显示新名称
    }
}


function startEventStream(message) {
    const loadingSpinner = document.getElementById('loadingSpinner');
    const aiAgentSelect = document.getElementById('aiAgent');
    const aiAgentId = aiAgentSelect.value;
    const modelSelect = document.getElementById('modelSelect').value;
    const ragId = document.getElementById('ragSelect').value || '0';

    // 区分普通对话和智能体对话
    const isAgentChat = aiAgentId && aiAgentId !== '0';

    loadingSpinner.classList.remove('hidden');
    submitBtn.disabled = true;

    if (currentEventSource) {
        currentEventSource.close();
    }

    if (selectedImages.length > 0) {
        uploadImagesWithMessage(message, selectedImages, modelSelect,ragId);

    }
    else if (isAgentChat) {
        // 处理智能体对话（普通HTTP请求）
        handleAgentChat(message, aiAgentId,currentChatId,loadingSpinner);
    } else {
        // 原有流式对话逻辑
        handleStreamChat(message, modelSelect,ragId);
    }
}
// 新增处理普通请求的方法
async function handleAgentChat(message, aiAgentId,currentChatId,loadingSpinner) {
    try {
        const response = await fetch(`http://localhost:8091/ai-agent-station/api/v1/ai/agent/chat_agent?chatId=${currentChatId}&aiAgentId=${aiAgentId}&message=${encodeURIComponent(message)}`);

        if (response.ok!=true) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        if (data.code === "0000") {
            appendMessage(data.data, true);
        } else {
            appendMessage(`请求失败：${data.info || '未知错误'}`, true);
        }
    } catch (error) {
        console.error('请求失败:', error);
        appendMessage(`请求失败：${error.message}`, true);
    } finally {
        loadingSpinner.classList.add('hidden');
        submitBtn.disabled = false;

    }
}
document.getElementById('imageUploadBtn').addEventListener('click', function() {
    document.getElementById('imageInput').click();
});

document.getElementById('imageInput').addEventListener('change', function(e) {
    const files = e.target.files;
    const uploadBtn = document.getElementById('imageUploadBtn');

    // 当有图片上传时添加绿色背景，否则恢复默认
    if (files.length > 0) {
        uploadBtn.classList.add('bg-green-500');
    } else {
        uploadBtn.classList.remove('bg-green-500');
    }

    // 原有文件列表更新逻辑
    const fileListDiv = document.getElementById('fileList');
    fileListDiv.innerHTML = files.length > 0
        ? `已选择文件：${Array.from(files).map(f => f.name).join(', ')}`
        : '未选择文件';
});

function uploadImagesWithMessage(message, images, modelId, ragId) {
    const formData = new FormData();
    formData.append('modelId', modelId);
    formData.append('message', message);
    images.forEach(file => formData.append('image', file));
    formData.append('ragId',ragId)
    submitBtn.disabled = true;
    fetch('http://localhost:8091/ai-agent-station/api/v1/ai/agent/multi_chat', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === '0000') {
            uploadBtn.classList.remove('bg-green-500');
            appendMessage(data.data, true);

            // 隐藏加载指示器
            loadingSpinner.classList.add('hidden');
            submitBtn.disabled = false;
        } else {
            appendMessage(`请求失败：${data.info || '未知错误'}`, true);
        }
    })
    .catch(error => {
        appendMessage(`请求失败：${error.message}`, true);
    })
    .finally(() => {
        submitBtn.disabled = false;
        selectedImages = [];
        uploadBtn.classList.remove('bg-green-500');
        loadingSpinner.classList.add('hidden');
    });
}

function handleStreamChat(message, modelId,ragId) {
    const modelId2 =modelId || '0';
    const url = `http://localhost:8091/ai-agent-station/api/v1/ai/agent/chat_stream?modelId=${modelId2}&ragId=${ragId}&message=${encodeURIComponent(message)}`;
    currentEventSource = new EventSource(url);
    let accumulatedContent = '';
    let tempMessageDiv = null;

    currentEventSource.onmessage = function(event) {
        try {
            const data = JSON.parse(event.data);

            if (data.result) {
                const output = data.result.output;
                if (output.text) {
                    const newContent = output.text;
                    accumulatedContent += newContent;

                    if (!tempMessageDiv) {
                        tempMessageDiv = document.createElement('div');
                        tempMessageDiv.className = 'max-w-4xl mx-auto mb-4 p-4 rounded-lg bg-gray-100 markdown-body relative';
                        chatArea.appendChild(tempMessageDiv);
                        welcomeMessage.style.display = 'none';
                    }

                    tempMessageDiv.textContent = accumulatedContent;
                    chatArea.scrollTop = chatArea.scrollHeight;
                }

                if (output.metadata.finishReason === 'STOP') {
                    currentEventSource.close();
                    const finalContent = accumulatedContent;
                    tempMessageDiv.innerHTML = DOMPurify.sanitize(marked.parse(finalContent));

                    const copyBtn = document.createElement('button');
                    copyBtn.className = 'absolute top-2 right-2 p-1 bg-gray-200 rounded-md text-xs';
                    copyBtn.textContent = '复制';
                    copyBtn.onclick = () => {
                        navigator.clipboard.writeText(finalContent).then(() => {
                            copyBtn.textContent = '已复制';
                            setTimeout(() => copyBtn.textContent = '复制', 2000);
                        });
                    };
                    tempMessageDiv.appendChild(copyBtn);

                    if (currentChatId) {
                        const chatData = JSON.parse(localStorage.getItem(`chat_${currentChatId}`) || '{"name": "新聊天", "messages": []}');
                        chatData.messages.push({ content: finalContent, isAssistant: true });
                        localStorage.setItem(`chat_${currentChatId}`, JSON.stringify(chatData));
                    }

                    // 隐藏加载指示器
                    loadingSpinner.classList.add('hidden');
                    submitBtn.disabled = false;
                }
            } else {
                currentEventSource.close();
                // 隐藏加载指示器
                loadingSpinner.classList.add('hidden');
                submitBtn.disabled = false;
            }
        } catch (e) {
            console.error('Error parsing event data:', e);
            // 发生错误时也隐藏加载指示器
            loadingSpinner.classList.add('hidden');
            submitBtn.disabled = false;
        }
    };

    currentEventSource.onerror = function(error) {
        console.error('EventSource error:', error);
        currentEventSource.close();
        // 发生错误时隐藏加载指示器
        loadingSpinner.classList.add('hidden');
        submitBtn.disabled = false;
    };
}


submitBtn.addEventListener('click', () => {
    const message = messageInput.value.trim();
    if (!message) return;

    if (!currentChatId) {
        createNewChat();
    }

    appendMessage(message, false);
    messageInput.value = '';
    startEventStream(message);
});

messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        submitBtn.click();
    }
});

newChatBtn.addEventListener('click', createNewChat);

toggleSidebarBtn.addEventListener('click', () => {
    sidebar.classList.toggle('-translate-x-full');
    updateSidebarIcon();
});

function updateSidebarIcon() {
    const iconPath = document.getElementById('sidebarIconPath');
    if (sidebar.classList.contains('-translate-x-full')) {
        iconPath.setAttribute('d', 'M4 6h16M4 12h16M4 18h16');
    } else {
        iconPath.setAttribute('d', 'M4 6h16M4 12h16M4 18h16');
    }
}

// Initialize
updateChatList();
const savedChatId = localStorage.getItem('currentChatId');
if (savedChatId) {
    loadChat(savedChatId);
}

// Handle window resize for responsive design
window.addEventListener('resize', () => {
    if (window.innerWidth > 768) {
        sidebar.classList.remove('-translate-x-full');
    } else {
        sidebar.classList.add('-translate-x-full');
    }
});

// Initial check for mobile devices
if (window.innerWidth <= 768) {
    sidebar.classList.add('-translate-x-full');
}
// 加载模型列表
async function loadModelOptions() {

    try {
        const response = await fetch('http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/queryAllModelConfig', {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error('获取模型列表失败');
        }

        const models = await response.json();
        const modelSelect = document.getElementById('modelSelect');

        // 清空现有选项
        modelSelect.innerHTML = '<option value="">选择模型</option>';

        // 添加新选项
        models.forEach(model => {
            const option = document.createElement('option');
            option.value = model.id; // 假设接口返回modelId字段
            option.textContent = model.modelName; // 假设接口返回modelName字段
            // 在loadModelOptions()中添加：
            option.textContent = `${model.modelVersion} (${model.status === 1 ? '可用' : '维护中'})`;
            option.disabled = model.status !== 1;
            modelSelect.appendChild(option);
        });
    } catch (error) {
        console.error('加载模型失败:', error);
        // 可以添加UI提示
    }
}

updateSidebarIcon();


// 初始化时加载模型
document.addEventListener('DOMContentLoaded', () => {
    // 在现有的DOMContentLoaded事件监听器中添加
    const modelSelect = document.getElementById('modelSelect');
    modelSelect.addEventListener('change', function() {
        console.log('Selected model ID:', this.value); // 调试用
    });
    loadModelOptions();
});

// 清空所有聊天记录
function clearAllChats() {
    if (confirm('确定要清空所有聊天记录吗？此操作不可恢复！')) {
        // 获取所有聊天记录的key
        const keys = Object.keys(localStorage).filter(key => key.startsWith('chat_'));

        // 删除所有聊天记录
        keys.forEach(key => localStorage.removeItem(key));

        // 清除当前聊天ID
        localStorage.removeItem('currentChatId');
        currentChatId = null;

        // 清空UI
        clearChatArea();
        updateChatList();

        // 创建新的空聊天
        createNewChat();
    }
}

// 绑定清空按钮事件
clearAllChatsBtn.addEventListener('click', clearAllChats);

// 上传知识下拉菜单控制
// 获取上传知识按钮和菜单元素
const uploadMenuButton = document.getElementById('uploadMenuButton');
const uploadMenu = document.getElementById('uploadMenu');

// 切换菜单显示
uploadMenuButton.addEventListener('click', (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (uploadMenu.style.display === 'none' || uploadMenu.style.display === '') {
        uploadMenu.style.display = 'block';
    } else {
        uploadMenu.style.display = 'none';
    }
});

// 点击外部区域关闭菜单
document.addEventListener('click', (e) => {
    if (!uploadMenu.contains(e.target) && e.target !== uploadMenuButton && !uploadMenuButton.contains(e.target)) {
        uploadMenu.style.display = 'none';
    }
});

// 菜单项点击后关闭菜单
document.querySelectorAll('#uploadMenu a').forEach(item => {
    item.addEventListener('click', () => {
        uploadMenu.style.display = 'none';
    });
});
