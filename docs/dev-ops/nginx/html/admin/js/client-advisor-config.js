/**
 * 客户端顾问配置管理模块
 */
const ClientAdvisorConfigManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的客户端顾问配置ID
    deleteAdvisorConfigId: null,

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadAdvisorConfigList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-advisor-config').on('click', () => {
            this.currentPage = 1;
            this.loadAdvisorConfigList();
        });

        // 新增客户端顾问配置按钮点击事件
        $('#btn-add-advisor-config').on('click', () => {
            this.showAdvisorConfigModal();
        });

        // 保存客户端顾问配置按钮点击事件
        $('#btn-save-advisor-config').on('click', () => {
            this.saveAdvisorConfig();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteAdvisorConfig();
        });

        // 回车键搜索
        $('#search-advisor-config').on('keypress', (e) => {
            if (e.which === 13) {
                this.currentPage = 1;
                this.loadAdvisorConfigList();
            }
        });
    },

    /**
     * 加载客户端顾问配置列表
     */
    loadAdvisorConfigList: function() {
        const params = {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            configName: $('#search-advisor-config').val()
        };

        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/advisor/config/queryClientAdvisorConfigList',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                this.renderAdvisorConfigList(res);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载客户端顾问配置列表失败', err);
                alert('加载客户端顾问配置列表失败');
            }
        });
    },

    /**
     * 渲染客户端顾问配置列表
     * @param {Array} data 客户端顾问配置列表数据
     */
    renderAdvisorConfigList: function(data) {
        if (!data || data.length === 0) {
            $('#advisor-config-list').html('<tr><td colspan="7" class="text-center">暂无数据</td></tr>');
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
                    <td>${item.clientId}</td>
                    <td>${item.advisorId}</td>
                    <td class="d-none d-md-table-cell">${item.configParams || '-'}</td>
                    <td>${item.status === 1 ? '<span class="badge bg-success">启用</span>' : '<span class="badge bg-danger">禁用</span>'}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="ClientAdvisorConfigManager.editAdvisorConfig(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="ClientAdvisorConfigManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#advisor-config-list').html(html);
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
                <a class="page-link" href="javascript:void(0);" onclick="ClientAdvisorConfigManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="ClientAdvisorConfigManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="ClientAdvisorConfigManager.goToPage(${this.currentPage + 1})">下一页</a>
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
        this.loadAdvisorConfigList();
    },

    /**
     * 显示客户端顾问配置模态框
     * @param {Object} advisorConfig 客户端顾问配置对象，不传则为新增
     */
    showAdvisorConfigModal: function(advisorConfig) {
        // 重置表单
        $('#advisorConfigForm')[0].reset();
        $('#advisor-config-id').val('');

        if (advisorConfig) {
            // 编辑模式
            $('#advisorConfigModalLabel').text('编辑客户端顾问配置');
            $('#advisor-config-id').val(advisorConfig.id);
            $('#advisor-config-client-id').val(advisorConfig.clientId);
            $('#advisor-config-advisor-id').val(advisorConfig.advisorId);
            $('#advisor-config-params').val(advisorConfig.configParams);
            $('#advisor-config-status').val(advisorConfig.status);
        } else {
            // 新增模式
            $('#advisorConfigModalLabel').text('新增客户端顾问配置');
            $('#advisor-config-status').val(1); // 默认启用
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('advisorConfigModal'));
        modal.show();
    },

    /**
     * 编辑客户端顾问配置
     * @param {number} id 客户端顾问配置ID
     */
    editAdvisorConfig: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/advisor/config/queryClientAdvisorConfigById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showAdvisorConfigModal(res);
            },
            error: (err) => {
                console.error('获取客户端顾问配置详情失败', err);
                alert('获取客户端顾问配置详情失败');
            }
        });
    },

    /**
     * 保存客户端顾问配置
     */
    saveAdvisorConfig: function() {
        // 表单验证
        if (!$('#advisor-config-client-id').val()) {
            alert('请输入客户端ID');
            return;
        }
        if (!$('#advisor-config-advisor-id').val()) {
            alert('请输入顾问ID');
            return;
        }

        const id = $('#advisor-config-id').val();
        const params = {
            clientId: parseInt($('#advisor-config-client-id').val()),
            advisorId: parseInt($('#advisor-config-advisor-id').val()),
            configParams: $('#advisor-config-params').val(),
            status: parseInt($('#advisor-config-status').val())
        };

        if (id) {
            // 编辑模式
            params.id = parseInt(id);
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/advisor/config/updateClientAdvisorConfig',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('更新成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('advisorConfigModal')).hide();
                        // 刷新列表
                        this.loadAdvisorConfigList();
                    } else {
                        alert('更新失败');
                    }
                },
                error: (err) => {
                    console.error('更新客户端顾问配置失败', err);
                    alert('更新客户端顾问配置失败');
                }
            });
        } else {
            // 新增模式
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/advisor/config/addClientAdvisorConfig',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('添加成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('advisorConfigModal')).hide();
                        // 刷新列表
                        this.loadAdvisorConfigList();
                    } else {
                        alert('添加失败');
                    }
                },
                error: (err) => {
                    console.error('添加客户端顾问配置失败', err);
                    alert('添加客户端顾问配置失败');
                }
            });
        }
    },

    /**
     * 显示删除确认模态框
     * @param {number} id 客户端顾问配置ID
     */
    showDeleteModal: function(id) {
        this.deleteAdvisorConfigId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除客户端顾问配置
     */
    deleteAdvisorConfig: function() {
        if (!this.deleteAdvisorConfigId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/advisor/config/deleteClientAdvisorConfig?id=${this.deleteAdvisorConfigId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    alert('删除成功');
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                    // 刷新列表
                    this.loadAdvisorConfigList();
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除客户端顾问配置失败', err);
                alert('删除客户端顾问配置失败');
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
    ClientAdvisorConfigManager.init();
});