package com.example.myxposedmodule.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.example.myxposedmodule.R;
import com.example.myxposedmodule.backup.EncryptionType;

public class BackupOptionsDialog {
    public interface Callback {
        void onOptionsSelected(BackupOptions options);
        void onCancel();
    }
    
    public static class BackupOptions {
        public EncryptionType encryptionType;
        public String password;
        public boolean compressBackup;
        public String customPath;
    }
    
    public static void show(Context context, Callback callback) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_backup_options, null);
        
        RadioGroup encryptionGroup = view.findViewById(R.id.encryption_group);
        EditText passwordInput = view.findViewById(R.id.password_input);
        CheckBox compressCheckbox = view.findViewById(R.id.compress_checkbox);
        EditText pathInput = view.findViewById(R.id.path_input);
        TextView pathHint = view.findViewById(R.id.path_hint);
        
        // 根据加密选项显示/隐藏密码输入框
        encryptionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean showPassword = checkedId == R.id.radio_password;
            passwordInput.setVisibility(showPassword ? View.VISIBLE : View.GONE);
        });
        
        new AlertDialog.Builder(context)
            .setTitle("备份选项")
            .setView(view)
            .setPositiveButton("确定", (dialog, which) -> {
                BackupOptions options = new BackupOptions();
                
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
                
                options.compressBackup = compressCheckbox.isChecked();
                options.customPath = pathInput.getText().toString();
                
                callback.onOptionsSelected(options);
            })
            .setNegativeButton("取消", (dialog, which) -> callback.onCancel())
            .show();
    }
} 