/**
 * AI智能体管理模块
 */
const AgentManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的智能体ID
    deleteAgentId: null,
    // 待预热的智能体ID
    preheatAgentId: null,

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadAgentList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-agent').on('click', () => {
            this.currentPage = 1;
            this.loadAgentList();
        });

        // 新增智能体按钮点击事件
        $('#btn-add-agent').on('click', () => {
            this.showAgentModal();
        });

        // 保存智能体按钮点击事件
        $('#btn-save-agent').on('click', () => {
            this.saveAgent();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteAgent();
        });

        // 回车键搜索
        $('#search-agent-name').on('keypress', (e) => {
            if (e.which === 13) {
                this.currentPage = 1;
                this.loadAgentList();
            }
        });
    },

    /**
     * 加载智能体列表
     */
    loadAgentList: function() {
        const params = {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            agentName: $('#search-agent-name').val()
        };

        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/queryAiAgentList',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                this.renderAgentList(res);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载智能体列表失败', err);
                alert('加载智能体列表失败');
            }
        });
    },

    /**
     * 渲染智能体列表
     * @param {Array} data 智能体列表数据
     */
    renderAgentList: function(data) {
        if (!data || data.length === 0) {
            $('#agent-list').html('<tr><td colspan="7" class="text-center">暂无数据</td></tr>');
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
                    <td>${item.agentName}</td>
                    <td class="d-none d-md-table-cell">${item.description || '-'}</td>
                    <td>${item.status === 1 ? '<span class="badge bg-success">启用</span>' : '<span class="badge bg-danger">禁用</span>'}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td class="d-none d-lg-table-cell">${this.formatDate(item.updateTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="AgentManager.editAgent(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="AgentManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                            <button class="btn btn-sm btn-outline-info btn-action" onclick="AgentManager.preheatAgent(${item.id})">
                                <i class="fas fa-fire"></i><span class="d-none d-md-inline"> 预热</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#agent-list').html(html);
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
                <a class="page-link" href="javascript:void(0);" onclick="AgentManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="AgentManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="AgentManager.goToPage(${this.currentPage + 1})">下一页</a>
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
        this.loadAgentList();
    },

    /**
     * 显示智能体模态框
     * @param {Object} agent 智能体对象，不传则为新增
     */
    showAgentModal: function(agent) {
        // 重置表单
        $('#agentForm')[0].reset();
        $('#agent-id').val('');

        if (agent) {
            // 编辑模式
            $('#agentModalLabel').text('编辑智能体');
            $('#agent-id').val(agent.id);
            $('#agent-name').val(agent.agentName);
            $('#agent-description').val(agent.description);
            $('#agent-status').val(agent.status);
        } else {
            // 新增模式
            $('#agentModalLabel').text('新增智能体');
            $('#agent-status').val(1); // 默认启用
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('agentModal'));
        modal.show();
    },

    /**
     * 编辑智能体
     * @param {number} id 智能体ID
     */
    editAgent: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/queryAiAgentById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showAgentModal(res);
            },
            error: (err) => {
                console.error('获取智能体详情失败', err);
                alert('获取智能体详情失败');
            }
        });
    },

    /**
     * 保存智能体
     */
    saveAgent: function() {
        // 表单验证
        if (!$('#agent-name').val()) {
            alert('请输入智能体名称');
            return;
        }

        const id = $('#agent-id').val();
        const params = {
            agentName: $('#agent-name').val(),
            description: $('#agent-description').val(),
            status: parseInt($('#agent-status').val())
        };

        if (id) {
            // 编辑
            params.id = parseInt(id);
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/updateAiAgent',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('更新成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('agentModal')).hide();
                        // 刷新列表
                        this.loadAgentList();
                    } else {
                        alert('更新失败');
                    }
                },
                error: (err) => {
                    console.error('更新智能体失败', err);
                    alert('更新智能体失败');
                }
            });
        } else {
            // 新增
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/addAiAgent',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('添加成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('agentModal')).hide();
                        // 刷新列表
                        this.loadAgentList();
                    } else {
                        alert('添加失败');
                    }
                },
                error: (err) => {
                    console.error('添加智能体失败', err);
                    alert('添加智能体失败');
                }
            });
        }
    },

    /**
     * 显示删除确认模态框
     * @param {number} id 智能体ID
     */
    showDeleteModal: function(id) {
        this.deleteAgentId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除智能体
     */
    deleteAgent: function() {
        if (!this.deleteAgentId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/deleteAiAgent?id=${this.deleteAgentId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    alert('删除成功');
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                    // 刷新列表
                    this.loadAgentList();
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除智能体失败', err);
                alert('删除智能体失败');
            }
        });
    },

    /**
     * 预热智能体
     * @param {number} id 智能体ID
     */
    preheatAgent: function(id) {
        this.preheatAgentId = id;
        
        // 显示加载动画
        $('#loading-overlay').show();
        
        // 调用预热接口
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/agent/preheat?aiAgentId=${id}`,
            type: 'GET',
            success: (res) => {
                // 隐藏加载动画
                $('#loading-overlay').hide();
                
                // 显示预热结果
                if (res && res.code === '0000') {
                    $('#preheat-result-message').html('<div class="alert alert-success">预热成功！</div>');
                } else {
                    $('#preheat-result-message').html(`<div class="alert alert-danger">预热失败：${res.info || '未知错误'}</div>`);
                }
                
                // 显示结果模态框
                const modal = new bootstrap.Modal(document.getElementById('preheatResultModal'));
                modal.show();
            },
            error: (err) => {
                // 隐藏加载动画
                $('#loading-overlay').hide();
                
                // 显示错误信息
                $('#preheat-result-message').html('<div class="alert alert-danger">预热失败：网络错误或服务器异常</div>');
                
                // 显示结果模态框
                const modal = new bootstrap.Modal(document.getElementById('preheatResultModal'));
                modal.show();
                
                console.error('预热智能体失败', err);
            }
        });
    },

    /**
     * 格式化日期
     * @param {string} dateStr 日期字符串
     * @returns {string} 格式化后的日期字符串
     */
    formatDate: function(dateStr) {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        return date.toLocaleString();
    }
};

// 页面加载完成后初始化
$(document).ready(function() {
    AgentManager.init();
});