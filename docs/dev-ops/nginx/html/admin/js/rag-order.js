/**
 * RAG订单管理模块
 */
const RagOrderManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的RAG订单ID
    deleteRagOrderId: null,

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadRagOrderList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-order').on('click', () => {
            this.currentPage = 1;
            this.loadRagOrderList();
        });

        // 新增RAG订单按钮点击事件
        $('#btn-add-rag-order').on('click', () => {
            this.showRagOrderModal();
        });

        // 保存RAG订单按钮点击事件
        $('#btn-save-rag-order').on('click', () => {
            this.saveRagOrder();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteRagOrder();
        });

        // 回车键搜索
        $('#search-order-id').on('keypress', (e) => {
            if (e.which === 13) {
                this.currentPage = 1;
                this.loadRagOrderList();
            }
        });
    },

    /**
     * 加载RAG订单列表
     */
    loadRagOrderList: function() {
        const params = {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            id: $('#search-order-id').val() ? parseInt($('#search-order-id').val()) : null
        };

        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/rag/queryRagOrderList',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                this.renderRagOrderList(res);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载RAG订单列表失败', err);
                alert('加载RAG订单列表失败');
            }
        });
    },

    /**
     * 渲染RAG订单列表
     * @param {Array} data RAG订单列表数据
     */
    renderRagOrderList: function(data) {
        if (!data || data.length === 0) {
            $('#rag-order-list').html('<tr><td colspan="7" class="text-center">暂无数据</td></tr>');
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
                    <td>${item.ragName}</td>
                    <td>${item.knowledgeTag}</td>
                    <td>${this.formatStatus(item.status)}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td class="d-none d-lg-table-cell">${this.formatDate(item.updateTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="RagOrderManager.editRagOrder(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="RagOrderManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#rag-order-list').html(html);
    },

    /**
     * 格式化状态
     * @param {number} status 状态码
     * @returns {string} 格式化后的状态
     */
    formatStatus: function(status) {
        switch (status) {
            case 1:
                return '<span class="badge bg-success">启用</span>';
            case 0:
                return '<span class="badge bg-danger">禁用</span>';
            default:
                return '<span class="badge bg-secondary">未知</span>';
        }
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
                <a class="page-link" href="javascript:void(0);" onclick="RagOrderManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="RagOrderManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="RagOrderManager.goToPage(${this.currentPage + 1})">下一页</a>
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
        this.loadRagOrderList();
    },

    /**
     * 显示RAG订单模态框
     * @param {Object} ragOrder RAG订单对象，不传则为新增
     */
    showRagOrderModal: function(ragOrder) {
        // 重置表单
        $('#ragOrderForm')[0].reset();
        $('#rag-order-id').val('');

        if (ragOrder) {
            // 编辑模式
            $('#ragOrderModalLabel').text('编辑RAG订单');
            $('#rag-order-id').val(ragOrder.id);
            $('#rag-name').val(ragOrder.ragName);
            $('#knowledge-tag').val(ragOrder.knowledgeTag);
            $('#rag-status').val(ragOrder.status);
        } else {
            // 新增模式
            $('#ragOrderModalLabel').text('新增RAG订单');
            $('#rag-status').val(1); // 默认启用
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('ragOrderModal'));
        modal.show();
    },

    /**
     * 编辑RAG订单
     * @param {number} id RAG订单ID
     */
    editRagOrder: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/rag/queryRagOrderById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showRagOrderModal(res);
            },
            error: (err) => {
                console.error('获取RAG订单详情失败', err);
                alert('获取RAG订单详情失败');
            }
        });
    },

    /**
     * 保存RAG订单
     */
    saveRagOrder: function() {
        // 表单验证
        if (!$('#rag-name').val()) {
            alert('请输入知识库名称');
            return;
        }
        if (!$('#knowledge-tag').val()) {
            alert('请输入知识标签');
            return;
        }

        const id = $('#rag-order-id').val();
        const params = {
            id: id ? parseInt(id) : null,
            ragName: $('#rag-name').val(),
            knowledgeTag: $('#knowledge-tag').val(),
            status: parseInt($('#rag-status').val())
        };

        // 根据是否有ID决定是新增还是更新
        const url = id ? 
            'http://192.168.1.104:8091/ai-agent-station/api/v1/ai/admin/rag/updateRagOrder' : 
            'http://192.168.1.104:8091/ai-agent-station/api/v1/ai/admin/rag/addRagOrder';

        $.ajax({
            url: url,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                if (res) {
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('ragOrderModal')).hide();
                    // 重新加载列表
                    this.loadRagOrderList();
                    alert(id ? '更新成功' : '新增成功');
                } else {
                    alert(id ? '更新失败' : '新增失败');
                }
            },
            error: (err) => {
                console.error(id ? '更新RAG订单失败' : '新增RAG订单失败', err);
                alert(id ? '更新RAG订单失败' : '新增RAG订单失败');
            }
        });
    },

    /**
     * 显示删除确认模态框
     * @param {number} id RAG订单ID
     */
    showDeleteModal: function(id) {
        this.deleteRagOrderId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除RAG订单
     */
    deleteRagOrder: function() {
        if (!this.deleteRagOrderId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/rag/deleteRagOrder?id=${this.deleteRagOrderId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                    // 重新加载列表
                    this.loadRagOrderList();
                    alert('删除成功');
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除RAG订单失败', err);
                alert('删除RAG订单失败');
            }
        });
    }
};

// 页面加载完成后初始化
$(document).ready(function() {
    RagOrderManager.init();
});