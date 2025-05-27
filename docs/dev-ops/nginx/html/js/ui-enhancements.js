/**
 * UI增强脚本 - 为AI知识库应用添加交互效果和动画
 */

document.addEventListener('DOMContentLoaded', function() {
    // 侧边栏切换功能增强
    const toggleSidebarBtn = document.getElementById('toggleSidebar');
    const sidebar = document.getElementById('sidebar');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    const sidebarIconPath = document.getElementById('sidebarIconPath');
    
    if (toggleSidebarBtn && sidebar) {
        toggleSidebarBtn.addEventListener('click', function() {
            sidebar.classList.toggle('open');
            
            // 在移动设备上显示/隐藏遮罩层
            if (window.innerWidth < 768) {
                if (sidebar.classList.contains('open')) {
                    sidebarOverlay.classList.remove('hidden');
                    setTimeout(() => {
                        sidebarOverlay.classList.add('active');
                    }, 10);
                    
                    // 更改图标为关闭
                    sidebarIconPath.setAttribute('d', 'M6 18L18 6M6 6l12 12');
                } else {
                    sidebarOverlay.classList.remove('active');
                    setTimeout(() => {
                        sidebarOverlay.classList.add('hidden');
                    }, 300);
                    
                    // 恢复图标为菜单
                    sidebarIconPath.setAttribute('d', 'M4 6h16M4 12h16M4 18h16');
                }
            }
        });
        
        // 点击遮罩层关闭侧边栏
        if (sidebarOverlay) {
            sidebarOverlay.addEventListener('click', function() {
                sidebar.classList.remove('open');
                sidebarOverlay.classList.remove('active');
                setTimeout(() => {
                    sidebarOverlay.classList.add('hidden');
                }, 300);
                
                // 恢复图标为菜单
                sidebarIconPath.setAttribute('d', 'M4 6h16M4 12h16M4 18h16');
            });
        }
    }
    
    // 上传菜单动画效果
    const uploadMenuButton = document.getElementById('uploadMenuButton');
    const uploadMenu = document.getElementById('uploadMenu');
    
    if (uploadMenuButton && uploadMenu) {
        uploadMenuButton.addEventListener('click', function(e) {
            e.stopPropagation();
            uploadMenu.classList.toggle('hidden');
            
            if (!uploadMenu.classList.contains('hidden')) {
                // 添加点击外部关闭菜单
                setTimeout(() => {
                    document.addEventListener('click', closeUploadMenu);
                }, 10);
            }
        });
        
        // 防止菜单内部点击关闭菜单
        uploadMenu.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }
    
    function closeUploadMenu() {
        if (uploadMenu) {
            uploadMenu.classList.add('hidden');
            document.removeEventListener('click', closeUploadMenu);
        }
    }
    
    // 美化消息显示
    function enhanceMessages() {
        const messages = document.querySelectorAll('.message');
        messages.forEach((message, index) => {
            // 添加延迟出现动画
            setTimeout(() => {
                message.style.opacity = '1';
                message.style.transform = 'translateY(0)';
            }, index * 100);
        });
    }
    
    // 监听DOM变化，为新添加的消息添加动画
    const chatAreaObserver = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            if (mutation.addedNodes.length) {
                enhanceMessages();
            }
        });
    });
    
    const chatArea = document.getElementById('chatArea');
    if (chatArea) {
        chatAreaObserver.observe(chatArea, { childList: true, subtree: true });
    }
    
    // 输入框自动调整高度
    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
        messageInput.addEventListener('input', function() {
            this.style.height = 'auto';
            this.style.height = (this.scrollHeight) + 'px';
        });
    }
    
    // 添加键盘快捷键
    document.addEventListener('keydown', function(e) {
        // Ctrl+Enter 或 Command+Enter 发送消息
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            const submitBtn = document.getElementById('submitBtn');
            if (submitBtn) {
                submitBtn.click();
            }
        }
    });
    
    // 初始化时执行一次
    enhanceMessages();
});