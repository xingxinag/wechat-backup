package com.example.myxposedmodule.backup;

public enum EncryptionType {
    NONE("不加密"),
    PASSWORD("密码加密"),
    AES("AES加密");
    
    private final String displayName;
    
    EncryptionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 