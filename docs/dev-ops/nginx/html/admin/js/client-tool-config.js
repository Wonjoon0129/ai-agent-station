/**
 * 客户端工具配置管理模块
 */
const ClientToolConfigManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的客户端工具配置ID
    deleteToolConfigId: null,
    // 搜索类型
    searchType: 'all',
    // 搜索值
    searchValue: '',

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadToolConfigList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-tool-config').on('click', () => {
            this.currentPage = 1;
            this.searchType = $('#search-type').val();
            this.searchValue = $('#search-value').val();
            this.loadToolConfigList();
        });

        // 新增客户端工具配置按钮点击事件
        $('#btn-add-tool-config').on('click', () => {
            this.showToolConfigModal();
        });

        // 保存客户端工具配置按钮点击事件
        $('#btn-save-tool-config').on('click', () => {
            this.saveToolConfig();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteToolConfig();
        });

        // 回车键搜索
        $('#search-value').on('keypress', (e) => {
            if (e.which === 13) {
                this.currentPage = 1;
                this.searchType = $('#search-type').val();
                this.searchValue = $('#search-value').val();
                this.loadToolConfigList();
            }
        });
    },

    /**
     * 加载客户端工具配置列表
     */
    loadToolConfigList: function() {
        const params = {
            pageNum: this.currentPage,
            pageSize: this.pageSize
        };

        // 根据搜索类型添加搜索条件
        if (this.searchType === 'clientId' && this.searchValue) {
            params.clientId = parseInt(this.searchValue);
        } else if (this.searchType === 'toolName' && this.searchValue) {
            params.toolName = this.searchValue;
        }

        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/tool/config/queryClientToolConfigList',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                this.renderToolConfigList(res);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载客户端工具配置列表失败', err);
                alert('加载客户端工具配置列表失败');
            }
        });
    },

    /**
     * 渲染客户端工具配置列表
     * @param {Array} data 客户端工具配置列表数据
     */
    renderToolConfigList: function(data) {
        if (!data || data.length === 0) {
            $('#tool-config-list').html('<tr><td colspan="8" class="text-center">暂无数据</td></tr>');
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
            // 截断工具参数，只显示前30个字符
            const toolParams = item.toolParams ? 
                (item.toolParams.length > 30 ? item.toolParams.substring(0, 30) + '...' : item.toolParams) : 
                '-';
            
            html += `
                <tr>
                    <td>${item.id}</td>
                    <td>${item.clientId}</td>
                    <td>${item.toolName || '-'}</td>
                    <td class="d-none d-md-table-cell">${item.description || '-'}</td>
                    <td class="d-none d-md-table-cell">${toolParams}</td>
                    <td>${item.status === 1 ? '<span class="badge bg-success">启用</span>' : '<span class="badge bg-danger">禁用</span>'}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="ClientToolConfigManager.editToolConfig(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="ClientToolConfigManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#tool-config-list').html(html);
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
                <a class="page-link" href="javascript:void(0);" onclick="ClientToolConfigManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="ClientToolConfigManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="ClientToolConfigManager.goToPage(${this.currentPage + 1})">下一页</a>
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
        this.loadToolConfigList();
    },

    /**
     * 显示客户端工具配置模态框
     * @param {Object} toolConfig 客户端工具配置对象，不传则为新增
     */
    showToolConfigModal: function(toolConfig) {
        // 重置表单
        $('#toolConfigForm')[0].reset();
        $('#tool-config-id').val('');

        if (toolConfig) {
            // 编辑模式
            $('#toolConfigModalLabel').text('编辑客户端工具配置');
            $('#tool-config-id').val(toolConfig.id);
            $('#tool-config-client-id').val(toolConfig.clientId);
            $('#tool-config-name').val(toolConfig.toolName);
            $('#tool-config-description').val(toolConfig.description);
            $('#tool-config-params').val(toolConfig.toolParams);
            $('#tool-config-status').val(toolConfig.status);
        } else {
            // 新增模式
            $('#toolConfigModalLabel').text('新增客户端工具配置');
            $('#tool-config-status').val(1); // 默认启用
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('toolConfigModal'));
        modal.show();
    },

    /**
     * 编辑客户端工具配置
     * @param {number} id 客户端工具配置ID
     */
    editToolConfig: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/tool/config/queryClientToolConfigById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showToolConfigModal(res);
            },
            error: (err) => {
                console.error('获取客户端工具配置详情失败', err);
                alert('获取客户端工具配置详情失败');
            }
        });
    },

    /**
     * 保存客户端工具配置
     */
    saveToolConfig: function() {
        // 表单验证
        if (!$('#tool-config-client-id').val()) {
            alert('请输入客户端ID');
            return;
        }
        if (!$('#tool-config-name').val()) {
            alert('请输入工具名称');
            return;
        }

        // 验证工具参数是否为有效的JSON格式
        const toolParams = $('#tool-config-params').val();
        if (toolParams) {
            try {
                JSON.parse(toolParams);
            } catch (e) {
                alert('工具参数必须是有效的JSON格式');
                return;
            }
        }

        const id = $('#tool-config-id').val();
        const params = {
            clientId: parseInt($('#tool-config-client-id').val()),
            toolName: $('#tool-config-name').val(),
            description: $('#tool-config-description').val(),
            toolParams: $('#tool-config-params').val(),
            status: parseInt($('#tool-config-status').val())
        };

        if (id) {
            // 编辑模式
            params.id = parseInt(id);
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/tool/config/updateClientToolConfig',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('更新成功');
                        const modal = bootstrap.Modal.getInstance(document.getElementById('toolConfigModal'));
                        modal.hide();
                        this.loadToolConfigList();
                    } else {
                        alert('更新失败');
                    }
                },
                error: (err) => {
                    console.error('更新客户端工具配置失败', err);
                    alert('更新客户端工具配置失败');
                }
            });
        } else {
            // 新增模式
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/tool/config/addClientToolConfig',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('添加成功');
                        const modal = bootstrap.Modal.getInstance(document.getElementById('toolConfigModal'));
                        modal.hide();
                        this.loadToolConfigList();
                    } else {
                        alert('添加失败');
                    }
                },
                error: (err) => {
                    console.error('添加客户端工具配置失败', err);
                    alert('添加客户端工具配置失败');
                }
            });
        }
    },

    /**
     * 显示删除确认模态框
     * @param {number} id 客户端工具配置ID
     */
    showDeleteModal: function(id) {
        this.deleteToolConfigId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除客户端工具配置
     */
    deleteToolConfig: function() {
        if (!this.deleteToolConfigId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/tool/config/deleteClientToolConfig?id=${this.deleteToolConfigId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    alert('删除成功');
                    const modal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
                    modal.hide();
                    this.loadToolConfigList();
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除客户端工具配置失败', err);
                alert('删除客户端工具配置失败');
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
    ClientToolConfigManager.init();
});