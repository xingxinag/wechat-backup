package com.example.myxposedmodule.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilePickerActivity extends Activity {
    private ListView listView;
    private TextView currentPath;
    private File currentDir;
    private List<String> items;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);
        
        listView = findViewById(R.id.file_list);
        currentPath = findViewById(R.id.current_path);
        
        // 从外部存储根目录开始
        currentDir = Environment.getExternalStorageDirectory();
        browseTo(currentDir);
        
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = items.get(position);
            if (selected.equals("..")) {
                upOneLevel();
            } else {
                File file = new File(currentDir, selected);
                if (file.isDirectory()) {
                    browseTo(file);
                } else if (file.getName().endsWith(".zip")) {
                    returnResult(file);
                }
            }
        });
    }
    
    private void browseTo(File dir) {
        currentDir = dir;
        currentPath.setText(dir.getAbsolutePath());
        
        items = new ArrayList<>();
        if (dir.getParentFile() != null) {
            items.add("..");
        }
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() || file.getName().endsWith(".zip")) {
                    items.add(file.getName());
                }
            }
        }
        Collections.sort(items);
        
        listView.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1, items));
    }
    
    private void upOneLevel() {
        if (currentDir.getParentFile() != null) {
            browseTo(currentDir.getParentFile());
        }
    }
    
    private void returnResult(File file) {
        Intent intent = new Intent();
        intent.putExtra("selected_path", file.getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }
} 