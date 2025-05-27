/**
 * MCP工具管理模块
 */
const McpManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的MCP ID
    deleteMcpId: null,

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadMcpList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-mcp').on('click', () => {
            this.currentPage = 1;
            this.loadMcpList();
        });

        // 新增MCP按钮点击事件
        $('#btn-add-mcp').on('click', () => {
            this.showMcpModal();
        });

        // 保存MCP按钮点击事件
        $('#btn-save-mcp').on('click', () => {
            this.saveMcp();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteMcp();
        });

        // 回车键搜索
        $('#search-mcp-name').on('keypress', (e) => {
            if (e.which === 13) {
                this.currentPage = 1;
                this.loadMcpList();
            }
        });
    },

    /**
     * 加载MCP列表
     */
    loadMcpList: function() {
        const params = {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            mcpName: $('#search-mcp-name').val()
        };

        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/tool/mcp/queryMcpList',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                this.renderMcpList(res);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载MCP列表失败', err);
                alert('加载MCP列表失败');
            }
        });
    },

    /**
     * 渲染MCP列表
     * @param {Array} data MCP列表数据
     */
    renderMcpList: function(data) {
        if (!data || data.length === 0) {
            $('#mcp-list').html('<tr><td colspan="8" class="text-center">暂无数据</td></tr>');
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
                    <td>${item.mcpName}</td>
                    <td class="d-none d-md-table-cell">${item.transportType}</td>
                    <td class="d-none d-lg-table-cell">${this.formatConfig(item.transportConfig)}</td>
                    <td class="d-none d-md-table-cell">${item.requestTimeout}</td>
                    <td>${item.status === 1 ? '<span class="badge bg-success">启用</span>' : '<span class="badge bg-danger">禁用</span>'}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="McpManager.editMcp(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="McpManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#mcp-list').html(html);
    },

    /**
     * 格式化配置信息，避免过长
     * @param {string} config 配置信息
     * @returns {string} 格式化后的配置信息
     */
    formatConfig: function(config) {
        if (!config) return '-';
        if (config.length > 30) {
            return config.substring(0, 30) + '...';
        }
        return config;
    },

    /**
     * 格式化日期
     * @param {string} dateStr 日期字符串
     * @returns {string} 格式化后的日期
     */
    formatDate: function(dateStr) {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        return date.toLocaleString();
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
                <a class="page-link" href="javascript:void(0);" onclick="McpManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="McpManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="McpManager.goToPage(${this.currentPage + 1})">下一页</a>
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
        this.loadMcpList();
    },

    /**
     * 显示MCP模态框
     * @param {Object} mcp MCP对象，不传则为新增
     */
    showMcpModal: function(mcp) {
        // 重置表单
        $('#mcpForm')[0].reset();
        $('#mcp-id').val('');

        if (mcp) {
            // 编辑模式
            $('#mcpModalLabel').text('编辑MCP');
            $('#mcp-id').val(mcp.id);
            $('#mcp-name').val(mcp.mcpName);
            $('#transport-type').val(mcp.transportType);
            $('#transport-config').val(mcp.transportConfig);
            $('#request-timeout').val(mcp.requestTimeout);
            $('#mcp-status').val(mcp.status);
        } else {
            // 新增模式
            $('#mcpModalLabel').text('新增MCP');
            $('#mcp-status').val(1); // 默认启用
            $('#transport-type').val('sse'); // 默认SSE
            $('#request-timeout').val(5); // 默认5分钟
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('mcpModal'));
        modal.show();
    },

    /**
     * 编辑MCP
     * @param {number} id MCP ID
     */
    editMcp: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/tool/mcp/queryMcpById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showMcpModal(res);
            },
            error: (err) => {
                console.error('获取MCP详情失败', err);
                alert('获取MCP详情失败');
            }
        });
    },

    /**
     * 保存MCP
     */
    saveMcp: function() {
        // 表单验证
        if (!$('#mcp-name').val()) {
            alert('请输入MCP名称');
            return;
        }

        if (!$('#transport-type').val()) {
            alert('请选择传输类型');
            return;
        }

        if (!$('#request-timeout').val() || parseInt($('#request-timeout').val()) <= 0) {
            alert('请输入有效的请求超时时间');
            return;
        }

        const id = $('#mcp-id').val();
        const params = {
            mcpName: $('#mcp-name').val(),
            transportType: $('#transport-type').val(),
            transportConfig: $('#transport-config').val(),
            requestTimeout: parseInt($('#request-timeout').val()),
            status: parseInt($('#mcp-status').val())
        };

        if (id) {
            // 编辑
            params.id = parseInt(id);
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/tool/mcp/updateMcp',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('更新成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('mcpModal')).hide();
                        // 刷新列表
                        this.loadMcpList();
                    } else {
                        alert('更新失败');
                    }
                },
                error: (err) => {
                    console.error('更新MCP失败', err);
                    alert('更新MCP失败');
                }
            });
        } else {
            // 新增
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/tool/mcp/addMcp',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('添加成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('mcpModal')).hide();
                        // 刷新列表
                        this.loadMcpList();
                    } else {
                        alert('添加失败');
                    }
                },
                error: (err) => {
                    console.error('添加MCP失败', err);
                    alert('添加MCP失败');
                }
            });
        }
    },

    /**
     * 显示删除确认模态框
     * @param {number} id MCP ID
     */
    showDeleteModal: function(id) {
        this.deleteMcpId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除MCP
     */
    deleteMcp: function() {
        if (!this.deleteMcpId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/tool/mcp/deleteMcp?id=${this.deleteMcpId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    alert('删除成功');
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                    // 刷新列表
                    this.loadMcpList();
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除MCP失败', err);
                alert('删除MCP失败');
            }
        });
    }
};

// 页面加载完成后初始化
$(document).ready(function() {
    McpManager.init();
});