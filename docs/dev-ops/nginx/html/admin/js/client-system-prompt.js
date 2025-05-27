/**
 * 系统提示词管理模块
 */
const SystemPromptManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的系统提示词ID
    deleteSystemPromptId: null,

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadSystemPromptList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-prompt').on('click', () => {
            this.currentPage = 1;
            this.loadSystemPromptList();
        });

        // 新增系统提示词按钮点击事件
        $('#btn-add-system-prompt').on('click', () => {
            this.showSystemPromptModal();
        });

        // 保存系统提示词按钮点击事件
        $('#btn-save-system-prompt').on('click', () => {
            this.saveSystemPrompt();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteSystemPrompt();
        });

        // 回车键搜索
        $('#search-prompt-name').on('keypress', (e) => {
            if (e.which === 13) {
                this.currentPage = 1;
                this.loadSystemPromptList();
            }
        });
    },

    /**
     * 加载系统提示词列表
     */
    loadSystemPromptList: function() {
        const params = {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            promptName: $('#search-prompt-name').val()
        };

        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/system/prompt/querySystemPromptList',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                this.renderSystemPromptList(res);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载系统提示词列表失败', err);
                alert('加载系统提示词列表失败');
            }
        });
    },

    /**
     * 渲染系统提示词列表
     * @param {Array} data 系统提示词列表数据
     */
    renderSystemPromptList: function(data) {
        if (!data || data.length === 0) {
            $('#system-prompt-list').html('<tr><td colspan="7" class="text-center">暂无数据</td></tr>');
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
            // 截断提示词内容，只显示前50个字符
            const promptContent = item.promptContent ? 
                (item.promptContent.length > 50 ? item.promptContent.substring(0, 50) + '...' : item.promptContent) : 
                '-';
            
            html += `
                <tr>
                    <td>${item.id}</td>
                    <td>${item.promptName}</td>
                    <td class="d-none d-md-table-cell">${promptContent}</td>
                    <td>${item.status === 1 ? '<span class="badge bg-success">启用</span>' : '<span class="badge bg-danger">禁用</span>'}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td class="d-none d-lg-table-cell">${this.formatDate(item.updateTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="SystemPromptManager.editSystemPrompt(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="SystemPromptManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#system-prompt-list').html(html);
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
                <a class="page-link" href="javascript:void(0);" onclick="SystemPromptManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="SystemPromptManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="SystemPromptManager.goToPage(${this.currentPage + 1})">下一页</a>
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
        this.loadSystemPromptList();
    },

    /**
     * 显示系统提示词模态框
     * @param {Object} systemPrompt 系统提示词对象，不传则为新增
     */
    showSystemPromptModal: function(systemPrompt) {
        // 重置表单
        $('#systemPromptForm')[0].reset();
        $('#system-prompt-id').val('');

        if (systemPrompt) {
            // 编辑模式
            $('#systemPromptModalLabel').text('编辑系统提示词');
            $('#system-prompt-id').val(systemPrompt.id);
            $('#system-prompt-name').val(systemPrompt.promptName);
            $('#system-prompt-content').val(systemPrompt.promptContent);
            $('#system-prompt-status').val(systemPrompt.status);
        } else {
            // 新增模式
            $('#systemPromptModalLabel').text('新增系统提示词');
            $('#system-prompt-status').val(1); // 默认启用
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('systemPromptModal'));
        modal.show();
    },

    /**
     * 编辑系统提示词
     * @param {number} id 系统提示词ID
     */
    editSystemPrompt: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/system/prompt/querySystemPromptById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showSystemPromptModal(res);
            },
            error: (err) => {
                console.error('获取系统提示词详情失败', err);
                alert('获取系统提示词详情失败');
            }
        });
    },

    /**
     * 保存系统提示词
     */
    saveSystemPrompt: function() {
        // 表单验证
        if (!$('#system-prompt-name').val()) {
            alert('请输入系统提示词名称');
            return;
        }

        if (!$('#system-prompt-content').val()) {
            alert('请输入系统提示词内容');
            return;
        }

        const id = $('#system-prompt-id').val();
        const params = {
            promptName: $('#system-prompt-name').val(),
            promptContent: $('#system-prompt-content').val(),
            status: parseInt($('#system-prompt-status').val())
        };

        if (id) {
            // 编辑
            params.id = parseInt(id);
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/system/prompt/updateSystemPrompt',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        // 关闭模态框
                        const modal = bootstrap.Modal.getInstance(document.getElementById('systemPromptModal'));
                        modal.hide();
                        // 刷新列表
                        this.loadSystemPromptList();
                        alert('更新成功');
                    } else {
                        alert('更新失败');
                    }
                },
                error: (err) => {
                    console.error('更新系统提示词失败', err);
                    alert('更新系统提示词失败');
                }
            });
        } else {
            // 新增
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/system/prompt/addSystemPrompt',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        // 关闭模态框
                        const modal = bootstrap.Modal.getInstance(document.getElementById('systemPromptModal'));
                        modal.hide();
                        // 刷新列表
                        this.loadSystemPromptList();
                        alert('新增成功');
                    } else {
                        alert('新增失败');
                    }
                },
                error: (err) => {
                    console.error('新增系统提示词失败', err);
                    alert('新增系统提示词失败');
                }
            });
        }
    },

    /**
     * 显示删除确认模态框
     * @param {number} id 系统提示词ID
     */
    showDeleteModal: function(id) {
        this.deleteSystemPromptId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除系统提示词
     */
    deleteSystemPrompt: function() {
        if (!this.deleteSystemPromptId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/system/prompt/deleteSystemPrompt?id=${this.deleteSystemPromptId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    // 关闭模态框
                    const modal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
                    modal.hide();
                    // 刷新列表
                    this.loadSystemPromptList();
                    alert('删除成功');
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除系统提示词失败', err);
                alert('删除系统提示词失败');
            }
        });
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
    }
};

// 页面加载完成后初始化
$(document).ready(function() {
    SystemPromptManager.init();
});