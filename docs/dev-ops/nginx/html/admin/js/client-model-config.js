/**
 * 客户端模型配置管理模块
 */
const ClientModelConfigManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的客户端模型配置ID
    deleteClientModelConfigId: null,
    // 搜索类型
    searchType: 'all',
    // 搜索值
    searchValue: '',

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadClientModelConfigList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-config').on('click', () => {
            this.searchType = $('#search-type').val();
            this.searchValue = $('#search-value').val();
            this.currentPage = 1;
            this.loadClientModelConfigList();
        });

        // 新增客户端模型配置按钮点击事件
        $('#btn-add-client-model-config').on('click', () => {
            this.showClientModelConfigModal();
        });

        // 保存客户端模型配置按钮点击事件
        $('#btn-save-client-model-config').on('click', () => {
            this.saveClientModelConfig();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteClientModelConfig();
        });

        // 回车键搜索
        $('#search-value').on('keypress', (e) => {
            if (e.which === 13) {
                this.searchType = $('#search-type').val();
                this.searchValue = $('#search-value').val();
                this.currentPage = 1;
                this.loadClientModelConfigList();
            }
        });
    },

    /**
     * 加载客户端模型配置列表
     */
    loadClientModelConfigList: function() {
        let url = 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/config/queryClientModelConfigList';
        let params = {};
        
        // 根据搜索类型构建查询参数
        if (this.searchType === 'clientId' && this.searchValue) {
            url = `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/config/queryClientModelConfigByClientId?clientId=${this.searchValue}`;
        } else if (this.searchType === 'modelId' && this.searchValue) {
            url = `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/config/queryClientModelConfigByModelId?modelId=${this.searchValue}`;
        } else {
            // 默认查询所有
            params = {
                pageNum: this.currentPage,
                pageSize: this.pageSize
            };
        }

        $.ajax({
            url: url,
            type: this.searchType === 'all' ? 'POST' : 'GET',
            contentType: 'application/json',
            data: this.searchType === 'all' ? JSON.stringify(params) : null,
            success: (res) => {
                // 如果是数组，直接使用；如果是单个对象，转为数组
                const dataList = Array.isArray(res) ? res : (res ? [res] : []);
                this.renderClientModelConfigList(dataList);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载客户端模型配置列表失败', err);
                alert('加载客户端模型配置列表失败');
            }
        });
    },

    /**
     * 渲染客户端模型配置列表
     * @param {Array} data 客户端模型配置列表数据
     */
    renderClientModelConfigList: function(data) {
        if (!data || data.length === 0) {
            $('#client-model-config-list').html('<tr><td colspan="7" class="text-center">暂无数据</td></tr>');
            this.total = 0;
            this.pages = 0;
            return;
        }
    
        // 假设第一条数据中包含了分页信息
        if (data[0]) {
            this.total = data[0].total || data.length;
            this.pages = data[0].pages || 1;
        }
    
        let html = '';
        data.forEach(item => {
            html += `
                <tr>
                    <td>${item.id}</td>
                    <td>${item.clientId}</td>
                    <td>${item.modelId}</td>
                    <td class="d-none d-md-table-cell">${this.formatModelParams(item.modelParams)}</td>
                    <td>${item.status === 1 ? '<span class="badge bg-success">启用</span>' : '<span class="badge bg-danger">禁用</span>'}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="ClientModelConfigManager.editClientModelConfig(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="ClientModelConfigManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#client-model-config-list').html(html);
    },

    /**
     * 格式化模型参数
     * @param {string} params 模型参数JSON字符串
     * @returns {string} 格式化后的模型参数
     */
    formatModelParams: function(params) {
        if (!params) return '-';
        try {
            // 尝试解析JSON并显示前30个字符
            const obj = JSON.parse(params);
            const str = JSON.stringify(obj);
            return str.length > 30 ? str.substring(0, 30) + '...' : str;
        } catch (e) {
            return params.length > 30 ? params.substring(0, 30) + '...' : params;
        }
    },

    /**
     * 格式化日期
     * @param {string|Date} date 日期
     * @returns {string} 格式化后的日期字符串
     */
    formatDate: function(date) {
        if (!date) return '-';
        try {
            const d = new Date(date);
            return d.toLocaleString();
        } catch (e) {
            return date;
        }
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
                <a class="page-link" href="javascript:void(0);" onclick="ClientModelConfigManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="ClientModelConfigManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="ClientModelConfigManager.goToPage(${this.currentPage + 1})">下一页</a>
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
        this.loadClientModelConfigList();
    },

    /**
     * 显示客户端模型配置模态框
     * @param {Object} clientModelConfig 客户端模型配置对象，不传则为新增
     */
    showClientModelConfigModal: function(clientModelConfig) {
        // 重置表单
        $('#clientModelConfigForm')[0].reset();
        $('#client-model-config-id').val('');

        if (clientModelConfig) {
            // 编辑模式
            $('#clientModelConfigModalLabel').text('编辑客户端模型配置');
            $('#client-model-config-id').val(clientModelConfig.id);
            $('#client-id').val(clientModelConfig.clientId);
            $('#model-id').val(clientModelConfig.modelId);
            $('#model-params').val(clientModelConfig.modelParams);
            $('#client-model-config-status').val(clientModelConfig.status);
        } else {
            // 新增模式
            $('#clientModelConfigModalLabel').text('新增客户端模型配置');
            $('#client-model-config-status').val(1); // 默认启用
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('clientModelConfigModal'));
        modal.show();
    },

    /**
     * 编辑客户端模型配置
     * @param {number} id 客户端模型配置ID
     */
    editClientModelConfig: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/config/queryClientModelConfigById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showClientModelConfigModal(res);
            },
            error: (err) => {
                console.error('获取客户端模型配置详情失败', err);
                alert('获取客户端模型配置详情失败');
            }
        });
    },

    /**
     * 保存客户端模型配置
     */
    saveClientModelConfig: function() {
        // 表单验证
        if (!$('#client-id').val()) {
            alert('请输入客户端ID');
            return;
        }
        if (!$('#model-id').val()) {
            alert('请输入模型ID');
            return;
        }

        // 构建请求参数
        const params = {
            id: $('#client-model-config-id').val() || null,
            clientId: parseInt($('#client-id').val()),
            modelId: parseInt($('#model-id').val()),
            modelParams: $('#model-params').val(),
            status: parseInt($('#client-model-config-status').val())
        };

        // 判断是新增还是更新
        const url = params.id ? 
            'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/config/updateClientModelConfig' :
            'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/config/addClientModelConfig';

        $.ajax({
            url: url,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                if (res) {
                    // 关闭模态框
                    const modal = bootstrap.Modal.getInstance(document.getElementById('clientModelConfigModal'));
                    modal.hide();
                    // 刷新列表
                    this.loadClientModelConfigList();
                    alert(params.id ? '更新成功' : '新增成功');
                } else {
                    alert(params.id ? '更新失败' : '新增失败');
                }
            },
            error: (err) => {
                console.error(params.id ? '更新客户端模型配置失败' : '新增客户端模型配置失败', err);
                alert(params.id ? '更新客户端模型配置失败' : '新增客户端模型配置失败');
            }
        });
    },

    /**
     * 显示删除确认模态框
     * @param {number} id 客户端模型配置ID
     */
    showDeleteModal: function(id) {
        this.deleteClientModelConfigId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除客户端模型配置
     */
    deleteClientModelConfig: function() {
        if (!this.deleteClientModelConfigId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/config/deleteClientModelConfig?id=${this.deleteClientModelConfigId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    // 关闭模态框
                    const modal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
                    modal.hide();
                    // 刷新列表
                    this.loadClientModelConfigList();
                    alert('删除成功');
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除客户端模型配置失败', err);
                alert('删除客户端模型配置失败');
            }
        });
    }
};

// 页面加载完成后初始化
$(document).ready(function() {
    ClientModelConfigManager.init();
});