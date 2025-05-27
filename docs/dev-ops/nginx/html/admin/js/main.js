/**
 * 主页面管理
 */
$(document).ready(function() {
    // 初始化智能体管理模块
    AgentManager.init();

    // 导航切换
    $('#nav-agent').on('click', function(e) {
        e.preventDefault();
        showContent('agent');
    });

    $('#nav-model').on('click', function(e) {
        e.preventDefault();
        showContent('model');
    });

    $('#nav-tool').on('click', function(e) {
        e.preventDefault();
        showContent('tool');
    });

    $('#nav-advisor').on('click', function(e) {
        e.preventDefault();
        showContent('advisor');
    });

    $('#nav-prompt').on('click', function(e) {
        e.preventDefault();
        showContent('prompt');
    });

    /**
     * 显示指定内容区域
     * @param {string} contentId 内容区域ID
     */
    function showContent(contentId) {
        // 隐藏所有内容区域
        $('[id^="content-"]').hide();
        // 显示指定内容区域
        $(`#content-${contentId}`).show();

        // 移除所有导航项的active类
        $('.nav-link').removeClass('active');
        // 为当前导航项添加active类
        $(`#nav-${contentId}`).addClass('active');
    }
});