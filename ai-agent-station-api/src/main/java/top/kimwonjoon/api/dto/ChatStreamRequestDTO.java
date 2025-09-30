
package top.kimwonjoon.api.dto;

public class ChatStreamRequestDTO {
    private String chatId;
    private Long modelId;
    private Long ragId;
    private String message;
    
    // Getters and Setters
    public String getChatId() {
        return chatId;
    }
    
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
    
    public Long getModelId() {
        return modelId;
    }
    
    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }
    
    public Long getRagId() {
        return ragId;
    }
    
    public void setRagId(Long ragId) {
        this.ragId = ragId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
