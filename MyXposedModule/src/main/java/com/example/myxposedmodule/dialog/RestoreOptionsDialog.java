package com.example.myxposedmodule.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import com.example.myxposedmodule.R;
import com.example.myxposedmodule.backup.EncryptionType;

public class RestoreOptionsDialog {
    public interface Callback {
        void onOptionsSelected(RestoreOptions options);
        void onCancel();
    }
    
    public static class RestoreOptions {
        public EncryptionType encryptionType;
        public String password;
    }
    
    public static void show(Context context, Callback callback) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_restore_options, null);
        
        RadioGroup encryptionGroup = view.findViewById(R.id.encryption_group);
        EditText passwordInput = view.findViewById(R.id.password_input);
        
        // 根据加密选项显示/隐藏密码输入框
        encryptionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean showPassword = checkedId == R.id.radio_password;
            passwordInput.setVisibility(showPassword ? View.VISIBLE : View.GONE);
        });
        
        new AlertDialog.Builder(context)
            .setTitle("恢复选项")
            .setView(view)
            .setPositiveButton("确定", (dialog, which) -> {
                RestoreOptions options = new RestoreOptions();
                
                // 获取加密类型
                int checkedId = encryptionGroup.getCheckedRadioButtonId();
                if (checkedId == R.id.radio_none) {
                    options.encryptionType = EncryptionType.NONE;
                } else if (checkedId == R.id.radio_password) {
                    options.encryptionType = EncryptionType.PASSWORD;
                    options.password = passwordInput.getText().toString();
                } else if (checkedId == R.id.radio_aes) {
                    options.encryptionType = EncryptionType.AES;
                }
                
                callback.onOptionsSelected(options);
            })
            .setNegativeButton("取消", (dialog, which) -> callback.onCancel())
            .show();
    }
} 