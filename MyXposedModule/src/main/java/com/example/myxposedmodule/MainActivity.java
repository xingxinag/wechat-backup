package com.example.myxposedmodule;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.myxposedmodule.config.Announcement;
import com.example.myxposedmodule.dialog.AgreementDialog;
import com.example.myxposedmodule.dialog.RestoreOptionsDialog;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PICK_BACKUP_FILE = 1002;
    private TextView backupPathText;
    private TextView statusText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 先显示使用协议
        AgreementDialog.showIfNeeded(this, new AgreementDialog.AgreementCallback() {
            @Override
            public void onAgreed() {
                initializeApp();
            }
            
            @Override
            public void onDisagreed() {
                finish();
            }
        });
    }
    
    private void initializeApp() {
        setContentView(R.layout.activity_main);

        backupPathText = findViewById(R.id.backup_path_text);
        statusText = findViewById(R.id.status_text);
        progressBar = findViewById(R.id.progress_bar);

        // 自动备份按钮
        findViewById(R.id.backup_button).setOnClickListener(v -> 
            checkPermissionsAndExecute(OperationType.BACKUP));

        // 自动恢复按钮
        findViewById(R.id.restore_auto_button).setOnClickListener(v -> 
            checkPermissionsAndExecute(OperationType.RESTORE_AUTO));

        // 手动恢复按钮
        findViewById(R.id.restore_manual_button).setOnClickListener(v -> 
            checkPermissionsAndExecute(OperationType.RESTORE_MANUAL));

        // 显示默认备份路径
        updateBackupPathText();

        // 检查公告
        checkAnnouncement();
    }
    
    private void checkAnnouncement() {
        Announcement.checkAnnouncement(this, announcement -> {
            runOnUiThread(() -> {
                showAnnouncementDialog(announcement);
            });
        });
    }
    
    private void showAnnouncementDialog(Announcement.AnnouncementItem announcement) {
        new AlertDialog.Builder(this)
            .setTitle(announcement.title)
            .setMessage(announcement.content)
            .setPositiveButton("我知道了", (dialog, which) -> {
                Announcement.markAsRead(this, announcement.id);
            })
            .show();
    }

    private void updateBackupPathText() {
        String path = Config.getBackupPath(this);
        backupPathText.setText("备份位置: " + path);
    }

    private void showManualRestoreDialog() {
        new AlertDialog.Builder(this)
            .setTitle("选择恢复方式")
            .setItems(new String[]{"从文件选择", "从文件夹选择"}, (dialog, which) -> {
                if (which == 0) {
                    selectBackupFile();
                } else {
                    selectBackupFolder();
                }
            })
            .show();
    }

    private void selectBackupFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_BACKUP_FILE);
    }

    private void selectBackupFolder() {
        // 使用系统文件选择器或自定义文件浏览器
        Intent intent = new Intent(this, FilePickerActivity.class);
        startActivityForResult(intent, PICK_BACKUP_FOLDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_BACKUP_FILE) {
                Uri uri = data.getData();
                handleSelectedBackup(uri);
            } else if (requestCode == PICK_BACKUP_FOLDER) {
                String path = data.getStringExtra("selected_path");
                handleSelectedBackupFolder(path);
            }
        }
    }

    private void handleSelectedBackup(Uri uri) {
        RestoreOptionsDialog.show(this, new RestoreOptionsDialog.Callback() {
            @Override
            public void onOptionsSelected(RestoreOptionsDialog.RestoreOptions options) {
                showProgress("正在恢复...");
                BackupManager.restore(MainActivity.this, uri, options, new BackupManager.BackupCallback() {
                    @Override
                    public void onProgress(int progress, String message) {
                        runOnUiThread(() -> {
                            progressDialog.setProgress(progress);
                            progressDialog.setMessage(message);
                        });
                    }

                    @Override
                    public void onSuccess(String path) {
                        runOnUiThread(() -> {
                            hideProgress();
                            showSuccess("恢复成功");
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            hideProgress();
                            showError("恢复失败: " + error);
                        });
                    }
                });
            }

            @Override
            public void onCancel() {
                // 用户取消恢复
            }
        });
    }

    private void showRestoreConfirmDialog(String path) {
        new AlertDialog.Builder(this)
            .setTitle("确认恢复")
            .setMessage("确定要从以下位置恢复备份吗？\n" + path)
            .setPositiveButton("确定", (dialog, which) -> {
                executeManualRestore(path);
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void executeManualRestore(String path) {
        showProgress("正在恢复...");
        BackupManager.restoreFromPath(this, path, new BackupManager.Callback() {
            @Override
            public void onSuccess() {
                hideProgress();
                showSuccess("恢复成功");
            }

            @Override
            public void onError(String message) {
                hideProgress();
                showError("恢复失败: " + message);
            }
        });
    }

    private void showProgress(String message) {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText(message);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        statusText.setText(message);
        statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }

    private void showError(String message) {
        statusText.setText(message);
        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    private void checkPermissionsAndExecute(OperationType type) {
        if (!checkPermissions()) {
            new AlertDialog.Builder(this)
                .setTitle("需要权限")
                .setMessage("此功能需要存储权限才能正常工作，是否授予权限？")
                .setPositiveButton("授予权限", (dialog, which) -> requestPermissions())
                .setNegativeButton("取消", null)
                .show();
            return;
        }
        // ... 执行操作
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要存储权限才能执行备份/恢复操作", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void executeBackup() {
        try {
            ShellUtils.executeScript(this, "backup.sh");
            Toast.makeText(this, "备份开始执行", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "备份执行失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void executeRestore() {
        try {
            ShellUtils.executeScript(this, "recovery.sh");
            Toast.makeText(this, "恢复开始执行", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "恢复执行失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isModuleActive() {
        return false; // 将被Hook修改为true
    }

    private void checkModuleStatus() {
        if (!isModuleActive()) {
            new AlertDialog.Builder(this)
                .setTitle("模块未激活")
                .setMessage("请在LSPosed管理器中启用本模块，并确保已勾选微信应用")
                .setPositiveButton("确定", null)
                .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkModuleStatus();
    }
} 