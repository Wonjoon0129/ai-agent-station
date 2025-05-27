/**
 * 智能体客户端关联管理模块
 */
const AgentClientManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的智能体客户端关联ID
    deleteAgentClientId: null,

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadAgentClientList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-agent-client').on('click', () => {
            this.currentPage = 1;
            this.loadAgentClientList();
        });

        // 新增智能体客户端关联按钮点击事件
        $('#btn-add-agent-client').on('click', () => {
            this.showAgentClientModal();
        });

        // 保存智能体客户端关联按钮点击事件
        $('#btn-save-agent-client').on('click', () => {
            this.saveAgentClient();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteAgentClient();
        });

        // 回车键搜索
        $('#search-value').on('keypress', (e) => {
            if (e.which === 13) {
                this.currentPage = 1;
                this.loadAgentClientList();
            }
        });
    },

    /**
     * 加载智能体客户端关联列表
     */
    loadAgentClientList: function() {
        const searchType = $('#search-type').val();
        const searchValue = $('#search-value').val();
        
        // 根据搜索类型决定调用哪个接口
        if (searchType === 'agent' && searchValue) {
            this.loadAgentClientByAgentId(searchValue);
        } else if (searchType === 'client' && searchValue) {
            this.loadAgentClientByClientId(searchValue);
        } else {
            // 默认查询所有
            this.loadAllAgentClient();
        }
    },

    /**
     * 加载所有智能体客户端关联
     */
    loadAllAgentClient: function() {
        const params = {
            pageNum: this.currentPage,
            pageSize: this.pageSize
        };

        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/client/queryAgentClientList',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                this.renderAgentClientList(res);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载智能体客户端关联列表失败', err);
                alert('加载智能体客户端关联列表失败');
            }
        });
    },

    /**
     * 根据智能体ID加载客户端关联
     */
    loadAgentClientByAgentId: function(agentId) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/client/queryAgentClientByAgentId?agentId=${agentId}`,
            type: 'GET',
            success: (res) => {
                this.renderAgentClientList(res);
                // 特定查询不需要分页
                $('#pagination').html('');
            },
            error: (err) => {
                console.error('根据智能体ID查询客户端关联列表失败', err);
                alert('根据智能体ID查询客户端关联列表失败');
            }
        });
    },

    /**
     * 根据客户端ID加载智能体关联
     */
    loadAgentClientByClientId: function(clientId) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/client/queryAgentClientByClientId?clientId=${clientId}`,
            type: 'GET',
            success: (res) => {
                this.renderAgentClientList(res);
                // 特定查询不需要分页
                $('#pagination').html('');
            },
            error: (err) => {
                console.error('根据客户端ID查询智能体关联列表失败', err);
                alert('根据客户端ID查询智能体关联列表失败');
            }
        });
    },

    /**
     * 渲染智能体客户端关联列表
     * @param {Array} data 智能体客户端关联列表数据
     */
    renderAgentClientList: function(data) {
        if (!data || data.length === 0) {
            $('#agent-client-list').html('<tr><td colspan="5" class="text-center">暂无数据</td></tr>');
            this.total = 0;
            this.pages = 0;
            return;
        }
    
        // 假设第一条数据中包含了分页信息
        if (data[0]) {
            this.total = data[0].total || 0;
            this.pages = data[0].pages || 0;
        }
    
        let html = '';
        data.forEach(item => {
            html += `
                <tr>
                    <td>${item.id}</td>
                    <td>${item.agentId}</td>
                    <td>${item.clientId}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="AgentClientManager.editAgentClient(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="AgentClientManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#agent-client-list').html(html);
    },

    /**
     * 渲染分页
     */
    renderPagination: function() {
        if (this.pages <= 1) {
            $('#pagination').html('');
            return;
        }

        let html = '';
        // 上一页
        html += `
            <li class="page-item ${this.currentPage === 1 ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="AgentClientManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="AgentClientManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="AgentClientManager.goToPage(${this.currentPage + 1})">下一页</a>
            </li>
        `;

        $('#pagination').html(html);
    },

    /**
     * 跳转到指定页
     * @param {number} page 页码
     */
    goToPage: function(page) {
        if (page < 1 || page > this.pages) {
            return;
        }
        this.currentPage = page;
        this.loadAgentClientList();
    },

    /**
     * 显示智能体客户端关联模态框
     * @param {Object} agentClient 智能体客户端关联对象，不传则为新增
     */
    showAgentClientModal: function(agentClient) {
        // 重置表单
        $('#agentClientForm')[0].reset();
        $('#agent-client-id').val('');

        if (agentClient) {
            // 编辑模式
            $('#agentClientModalLabel').text('编辑智能体客户端关联');
            $('#agent-client-id').val(agentClient.id);
            $('#agent-id').val(agentClient.agentId);
            $('#client-id').val(agentClient.clientId);
        } else {
            // 新增模式
            $('#agentClientModalLabel').text('新增智能体客户端关联');
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('agentClientModal'));
        modal.show();
    },

    /**
     * 编辑智能体客户端关联
     * @param {number} id 智能体客户端关联ID
     */
    editAgentClient: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/client/queryAgentClientById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showAgentClientModal(res);
            },
            error: (err) => {
                console.error('获取智能体客户端关联详情失败', err);
                alert('获取智能体客户端关联详情失败');
            }
        });
    },

    /**
     * 保存智能体客户端关联
     */
    saveAgentClient: function() {
        // 表单验证
        if (!$('#agent-id').val()) {
            alert('请输入智能体ID');
            return;
        }
        if (!$('#client-id').val()) {
            alert('请输入客户端ID');
            return;
        }

        const id = $('#agent-client-id').val();
        const agentClient = {
            agentId: parseInt($('#agent-id').val()),
            clientId: parseInt($('#client-id').val())
        };

        if (id) {
            // 编辑模式
            agentClient.id = parseInt(id);
            this.updateAgentClient(agentClient);
        } else {
            // 新增模式
            this.addAgentClient(agentClient);
        }
    },

    /**
     * 新增智能体客户端关联
     * @param {Object} agentClient 智能体客户端关联对象
     */
    addAgentClient: function(agentClient) {
        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/client/addAgentClient',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(agentClient),
            success: (res) => {
                if (res) {
                    alert('新增成功');
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('agentClientModal')).hide();
                    // 刷新列表
                    this.loadAgentClientList();
                } else {
                    alert('新增失败');
                }
            },
            error: (err) => {
                console.error('新增智能体客户端关联失败', err);
                alert('新增智能体客户端关联失败');
            }
        });
    },

    /**
     * 更新智能体客户端关联
     * @param {Object} agentClient 智能体客户端关联对象
     */
    updateAgentClient: function(agentClient) {
        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/client/updateAgentClient',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(agentClient),
            success: (res) => {
                if (res) {
                    alert('更新成功');
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('agentClientModal')).hide();
                    // 刷新列表
                    this.loadAgentClientList();
                } else {
                    alert('更新失败');
                }
            },
            error: (err) => {
                console.error('更新智能体客户端关联失败', err);
                alert('更新智能体客户端关联失败');
            }
        });
    },

    /**
     * 显示删除确认模态框
     * @param {number} id 智能体客户端关联ID
     */
    showDeleteModal: function(id) {
        this.deleteAgentClientId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除智能体客户端关联
     */
    deleteAgentClient: function() {
        if (!this.deleteAgentClientId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/client/deleteAgentClient?id=${this.deleteAgentClientId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    alert('删除成功');
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                    // 刷新列表
                    this.loadAgentClientList();
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除智能体客户端关联失败', err);
                alert('删除智能体客户端关联失败');
            }
        });
    },

    /**
     * 格式化日期
     * @param {string} dateStr 日期字符串
     * @returns {string} 格式化后的日期字符串
     */
    formatDate: function(dateStr) {
        if (!dateStr) {
            return '-';
        }
        const date = new Date(dateStr);
        return date.toLocaleString();
    }
};

// 页面加载完成后初始化
$(document).ready(function() {
    AgentClientManager.init();
});