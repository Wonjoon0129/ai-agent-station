/**
 * 客户端顾问管理模块
 */
const ClientAdvisorManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的客户端顾问ID
    deleteClientAdvisorId: null,

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadClientAdvisorList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-advisor').on('click', () => {
            this.currentPage = 1;
            this.loadClientAdvisorList();
        });

        // 新增客户端顾问按钮点击事件
        $('#btn-add-client-advisor').on('click', () => {
            this.showClientAdvisorModal();
        });

        // 保存客户端顾问按钮点击事件
        $('#btn-save-client-advisor').on('click', () => {
            this.saveClientAdvisor();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteClientAdvisor();
        });

        // 回车键搜索
        $('#search-advisor-name').on('keypress', (e) => {
            if (e.which === 13) {
                this.currentPage = 1;
                this.loadClientAdvisorList();
            }
        });
    },

    /**
     * 加载客户端顾问列表
     */
    loadClientAdvisorList: function() {
        const params = {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            advisorName: $('#search-advisor-name').val()
        };

        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/advisor/queryClientAdvisorList',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                this.renderClientAdvisorList(res);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载客户端顾问列表失败', err);
                alert('加载客户端顾问列表失败');
            }
        });
    },

    /**
     * 渲染客户端顾问列表
     * @param {Array} data 客户端顾问列表数据
     */
    renderClientAdvisorList: function(data) {
        if (!data || data.length === 0) {
            $('#client-advisor-list').html('<tr><td colspan="9" class="text-center">暂无数据</td></tr>');
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
                    <td>${item.advisorName}</td>
                    <td>${item.advisorType}</td>
                    <td>${item.orderNum}</td>
                    <td class="d-none d-md-table-cell">${item.extParam || '-'}</td>
                    <td>${item.status === 1 ? '<span class="badge bg-success">启用</span>' : '<span class="badge bg-danger">禁用</span>'}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td class="d-none d-lg-table-cell">${this.formatDate(item.updateTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="ClientAdvisorManager.editClientAdvisor(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="ClientAdvisorManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#client-advisor-list').html(html);
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
                <a class="page-link" href="javascript:void(0);" onclick="ClientAdvisorManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="ClientAdvisorManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="ClientAdvisorManager.goToPage(${this.currentPage + 1})">下一页</a>
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
        this.loadClientAdvisorList();
    },

    /**
     * 显示客户端顾问模态框
     * @param {Object} clientAdvisor 客户端顾问对象，不传则为新增
     */
    showClientAdvisorModal: function(clientAdvisor) {
        // 重置表单
        $('#clientAdvisorForm')[0].reset();
        $('#client-advisor-id').val('');

        if (clientAdvisor) {
            // 编辑模式
            $('#clientAdvisorModalLabel').text('编辑客户端顾问');
            $('#client-advisor-id').val(clientAdvisor.id);
            $('#client-advisor-name').val(clientAdvisor.advisorName);
            $('#client-advisor-type').val(clientAdvisor.advisorType);
            $('#client-advisor-order').val(clientAdvisor.orderNum);
            $('#client-advisor-ext-param').val(clientAdvisor.extParam);
            $('#client-advisor-status').val(clientAdvisor.status);
        } else {
            // 新增模式
            $('#clientAdvisorModalLabel').text('新增客户端顾问');
            $('#client-advisor-status').val(1); // 默认启用
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('clientAdvisorModal'));
        modal.show();
    },

    /**
     * 编辑客户端顾问
     * @param {number} id 客户端顾问ID
     */
    editClientAdvisor: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/advisor/queryClientAdvisorById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showClientAdvisorModal(res);
            },
            error: (err) => {
                console.error('获取客户端顾问详情失败', err);
                alert('获取客户端顾问详情失败');
            }
        });
    },

    /**
     * 保存客户端顾问
     */
    saveClientAdvisor: function() {
        // 表单验证
        if (!$('#client-advisor-name').val()) {
            alert('请输入客户端顾问名称');
            return;
        }
        
        if (!$('#client-advisor-type').val()) {
            alert('请输入客户端顾问类型');
            return;
        }

        const id = $('#client-advisor-id').val();
        const params = {
            advisorName: $('#client-advisor-name').val(),
            advisorType: $('#client-advisor-type').val(),
            orderNum: parseInt($('#client-advisor-order').val() || '0'),
            extParam: $('#client-advisor-ext-param').val(),
            status: parseInt($('#client-advisor-status').val())
        };

        if (id) {
            // 编辑
            params.id = parseInt(id);
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/advisor/updateClientAdvisor',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('更新成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('clientAdvisorModal')).hide();
                        // 刷新列表
                        this.loadClientAdvisorList();
                    } else {
                        alert('更新失败');
                    }
                },
                error: (err) => {
                    console.error('更新客户端顾问失败', err);
                    alert('更新客户端顾问失败');
                }
            });
        } else {
            // 新增
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/advisor/addClientAdvisor',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('新增成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('clientAdvisorModal')).hide();
                        // 刷新列表
                        this.loadClientAdvisorList();
                    } else {
                        alert('新增失败');
                    }
                },
                error: (err) => {
                    console.error('新增客户端顾问失败', err);
                    alert('新增客户端顾问失败');
                }
            });
        }
    },

    /**
     * 显示删除确认模态框
     * @param {number} id 客户端顾问ID
     */
    showDeleteModal: function(id) {
        this.deleteClientAdvisorId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除客户端顾问
     */
    deleteClientAdvisor: function() {
        if (!this.deleteClientAdvisorId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/advisor/deleteClientAdvisor?id=${this.deleteClientAdvisorId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    alert('删除成功');
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                    // 刷新列表
                    this.loadClientAdvisorList();
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除客户端顾问失败', err);
                alert('删除客户端顾问失败');
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
    ClientAdvisorManager.init();
});