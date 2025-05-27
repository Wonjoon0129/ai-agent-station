/**
 * AI代理任务调度管理模块
 */
const TaskScheduleManager = {
    // 当前页码
    currentPage: 1,
    // 每页条数
    pageSize: 10,
    // 总条数
    total: 0,
    // 总页数
    pages: 0,
    // 待删除的任务调度ID
    deleteTaskScheduleId: null,

    /**
     * 初始化
     */
    init: function() {
        this.bindEvents();
        this.loadTaskScheduleList();
    },

    /**
     * 绑定事件
     */
    bindEvents: function() {
        // 搜索按钮点击事件
        $('#btn-search-task').on('click', () => {
            this.currentPage = 1;
            this.loadTaskScheduleList();
        });

        // 新增任务调度按钮点击事件
        $('#btn-add-task-schedule').on('click', () => {
            this.showTaskScheduleModal();
        });

        // 保存任务调度按钮点击事件
        $('#btn-save-task-schedule').on('click', () => {
            this.saveTaskSchedule();
        });

        // 确认删除按钮点击事件
        $('#btn-confirm-delete').on('click', () => {
            this.deleteTaskSchedule();
        });

        // 回车键搜索
        $('#search-task-name').on('keypress', (e) => {
            if (e.which === 13) {
                this.currentPage = 1;
                this.loadTaskScheduleList();
            }
        });
    },

    /**
     * 加载任务调度列表
     */
    loadTaskScheduleList: function() {
        const params = {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            taskName: $('#search-task-name').val()
        };

        $.ajax({
            url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/task/queryTaskScheduleList',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: (res) => {
                this.renderTaskScheduleList(res);
                this.renderPagination();
            },
            error: (err) => {
                console.error('加载任务调度列表失败', err);
                alert('加载任务调度列表失败');
            }
        });
    },

    /**
     * 渲染任务调度列表
     * @param {Array} data 任务调度列表数据
     */
    renderTaskScheduleList: function(data) {
        if (!data || data.length === 0) {
            $('#task-schedule-list').html('<tr><td colspan="8" class="text-center">暂无数据</td></tr>');
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
                    <td>${item.taskName}</td>
                    <td>${item.agentId}</td>
                    <td class="d-none d-md-table-cell">${item.description || '-'}</td>
                    <td class="d-none d-md-table-cell">${item.taskParam || '-'}</td>
                    <td>${item.status === 1 ? '<span class="badge bg-success">启用</span>' : '<span class="badge bg-danger">禁用</span>'}</td>
                    <td class="d-none d-md-table-cell">${this.formatDate(item.createTime)}</td>
                    <td class="d-none d-lg-table-cell">${this.formatDate(item.updateTime)}</td>
                    <td>
                        <div class="d-flex flex-wrap">
                            <button class="btn btn-sm btn-outline-primary btn-action" onclick="TaskScheduleManager.editTaskSchedule(${item.id})">
                                <i class="fas fa-edit"></i><span class="d-none d-md-inline"> 编辑</span>
                            </button>
                            <button class="btn btn-sm btn-outline-danger btn-action" onclick="TaskScheduleManager.showDeleteModal(${item.id})">
                                <i class="fas fa-trash"></i><span class="d-none d-md-inline"> 删除</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
    
        $('#task-schedule-list').html(html);
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
                <a class="page-link" href="javascript:void(0);" onclick="TaskScheduleManager.goToPage(${this.currentPage - 1})">上一页</a>
            </li>
        `;

        // 页码
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.pages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0);" onclick="TaskScheduleManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        // 下一页
        html += `
            <li class="page-item ${this.currentPage === this.pages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0);" onclick="TaskScheduleManager.goToPage(${this.currentPage + 1})">下一页</a>
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
        this.loadTaskScheduleList();
    },

    /**
     * 显示任务调度模态框
     * @param {Object} taskSchedule 任务调度对象，不传则为新增
     */
    showTaskScheduleModal: function(taskSchedule) {
        // 重置表单
        $('#taskScheduleForm')[0].reset();
        $('#task-schedule-id').val('');

        if (taskSchedule) {
            // 编辑模式
            $('#taskScheduleModalLabel').text('编辑任务调度');
            $('#task-schedule-id').val(taskSchedule.id);
            $('#task-name').val(taskSchedule.taskName);
            $('#task-agent-id').val(taskSchedule.agentId);
            $('#task-description').val(taskSchedule.description);
            $('#task-cron').val(taskSchedule.cronExpression);
            $('#task-param').val(taskSchedule.taskParam);
            $('#task-status').val(taskSchedule.status);
        } else {
            // 新增模式
            $('#taskScheduleModalLabel').text('新增任务调度');
            $('#task-status').val(1); // 默认启用
        }

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('taskScheduleModal'));
        modal.show();
    },

    /**
     * 编辑任务调度
     * @param {number} id 任务调度ID
     */
    editTaskSchedule: function(id) {
        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/task/queryTaskScheduleById?id=${id}`,
            type: 'GET',
            success: (res) => {
                this.showTaskScheduleModal(res);
            },
            error: (err) => {
                console.error('获取任务调度详情失败', err);
                alert('获取任务调度详情失败');
            }
        });
    },

    /**
     * 保存任务调度
     */
    saveTaskSchedule: function() {
        // 表单验证
        if (!$('#task-name').val()) {
            alert('请输入任务名称');
            return;
        }
        
        if (!$('#task-agent-id').val()) {
            alert('请输入智能体ID');
            return;
        }

        const id = $('#task-schedule-id').val();
        const params = {
            id: id,
            taskName: $('#task-name').val(),
            agentId: parseInt($('#task-agent-id').val()),
            description: $('#task-description').val(),
            cronExpression: $('#task-cron').val(),
            taskParam: $('#task-param').val(),
            status: parseInt($('#task-status').val())
        };

        if (id) {
            // 编辑
            params.id = parseInt(id);
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/task/updateTaskSchedule',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('更新成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('taskScheduleModal')).hide();
                        // 刷新列表
                        this.loadTaskScheduleList();
                    } else {
                        alert('更新失败');
                    }
                },
                error: (err) => {
                    console.error('更新任务调度失败', err);
                    alert('更新任务调度失败');
                }
            });
        } else {
            // 新增
            $.ajax({
                url: 'http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/task/addTaskSchedule',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: (res) => {
                    if (res) {
                        alert('添加成功');
                        // 关闭模态框
                        bootstrap.Modal.getInstance(document.getElementById('taskScheduleModal')).hide();
                        // 刷新列表
                        this.loadTaskScheduleList();
                    } else {
                        alert('添加失败');
                    }
                },
                error: (err) => {
                    console.error('添加任务调度失败', err);
                    alert('添加任务调度失败');
                }
            });
        }
    },

    /**
     * 显示删除确认模态框
     * @param {number} id 任务调度ID
     */
    showDeleteModal: function(id) {
        this.deleteTaskScheduleId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    },

    /**
     * 删除任务调度
     */
    deleteTaskSchedule: function() {
        if (!this.deleteTaskScheduleId) {
            return;
        }

        $.ajax({
            url: `http://localhost:8091/ai-agent-station/api/v1/ai/admin/agent/task/deleteTaskSchedule?id=${this.deleteTaskScheduleId}`,
            type: 'GET',
            success: (res) => {
                if (res) {
                    alert('删除成功');
                    // 关闭模态框
                    bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                    // 刷新列表
                    this.loadTaskScheduleList();
                } else {
                    alert('删除失败');
                }
            },
            error: (err) => {
                console.error('删除任务调度失败', err);
                alert('删除任务调度失败');
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
    TaskScheduleManager.init();
});