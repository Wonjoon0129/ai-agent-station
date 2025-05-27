/**
 * 客户端模型管理模块
 */
const ClientModelManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的客户端模型ID
    deleteClientModelId: null,

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadClientModelList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-model').on('click', () => {
            this.currentPage = 1;
            this.loadClientModelList();
        });

        // 新增客户端模型按钮点击事件
        $('#btn-add-client-model').on('click', () => {
            this.showClientModelModal();
        });

        // 保存客户端模型按钮点击事件
        $('#btn-save-client-model').on('click', () => {
            this.saveClientModel();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteClientModel();
        });

        // 回车键搜索
        $('#search-model-name').on('keypress', (e) => {
            if (e.which === 13) {
                this.currentPage = 1;
                this.loadClientModelList();
            }
        });
    },

    /**
     * 加载客户端模型列表
     */
    loadClientModelList: function() {
        const params = {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            modelName: $('#search-model-name').val()
        };

        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/queryClientModelList',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                this.renderClientModelList(res);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载客户端模型列表失败', err);
                alert('加载客户端模型列表失败');
            }
        });
    },

    /**
     * 渲染客户端模型列表
     * @param {Array} data 客户端模型列表数据
     */
    renderClientModelList: function(data) {
        if (!data || data.length === 0) {
            $('#client-model-list').html('<tr><td colspan="7" class="text-center">暂无数据</td></tr>');
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
                    <td>${item.modelName}</td>
                    <td class="d-none d-md-table-cell">${item.modelVersion || '-'}</td>
                    <td>${item.status === 1 ? '<span class="badge bg-success">启用</span>' : '<span class="badge bg-danger">禁用</span>'}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td class="d-none d-lg-table-cell">${this.formatDate(item.updateTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="ClientModelManager.editClientModel(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="ClientModelManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#client-model-list').html(html);
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
                <a class="page-link" href="javascript:void(0);" onclick="ClientModelManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="ClientModelManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="ClientModelManager.goToPage(${this.currentPage + 1})">下一页</a>
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
        this.loadClientModelList();
    },

    /**
     * 显示客户端模型模态框
     * @param {Object} clientModel 客户端模型对象，不传则为新增
     */
    showClientModelModal: function(clientModel) {
        // 重置表单
        $('#clientModelForm')[0].reset();
        $('#client-model-id').val('');

        if (clientModel) {
            // 编辑模式
            $('#clientModelModalLabel').text('编辑客户端模型');
            $('#client-model-id').val(clientModel.id);
            $('#client-model-name').val(clientModel.modelName);
            $('#client-model-description').val(clientModel.description);
            $('#client-model-status').val(clientModel.status);
        } else {
            // 新增模式
            $('#clientModelModalLabel').text('新增客户端模型');
            $('#client-model-status').val(1); // 默认启用
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('clientModelModal'));
        modal.show();
    },

    /**
     * 编辑客户端模型
     * @param {number} id 客户端模型ID
     */
    editClientModel: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/queryClientModelById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showClientModelModal(res);
            },
            error: (err) => {
                console.error('获取客户端模型详情失败', err);
                alert('获取客户端模型详情失败');
            }
        });
    },

    /**
     * 保存客户端模型
     */
    saveClientModel: function() {
        // 表单验证
        if (!$('#client-model-name').val()) {
            alert('请输入客户端模型名称');
            return;
        }

        const id = $('#client-model-id').val();
        const params = {
            modelName: $('#client-model-name').val(),
            description: $('#client-model-description').val(),
            status: parseInt($('#client-model-status').val())
        };

        if (id) {
            // 编辑
            params.id = parseInt(id);
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/updateClientModel',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('更新成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('clientModelModal')).hide();
                        // 刷新列表
                        this.loadClientModelList();
                    } else {
                        alert('更新失败');
                    }
                },
                error: (err) => {
                    console.error('更新客户端模型失败', err);
                    alert('更新客户端模型失败');
                }
            });
        } else {
            // 新增
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/addClientModel',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('新增成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('clientModelModal')).hide();
                        // 刷新列表
                        this.loadClientModelList();
                    } else {
                        alert('新增失败');
                    }
                },
                error: (err) => {
                    console.error('新增客户端模型失败', err);
                    alert('新增客户端模型失败');
                }
            });
        }
    },

    /**
     * 显示删除模态框
     * @param {number} id 客户端模型ID
     */
    showDeleteModal: function(id) {
        this.deleteClientModelId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除客户端模型
     */
    deleteClientModel: function() {
        if (!this.deleteClientModelId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/client/model/deleteClientModel?id=${this.deleteClientModelId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    alert('删除成功');
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                    // 刷新列表
                    this.loadClientModelList();
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除客户端模型失败', err);
                alert('删除客户端模型失败');
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
    ClientModelManager.init();
});