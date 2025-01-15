package com.example.myxposedmodule.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressDialog extends Dialog {
    private ProgressBar progressBar;
    private TextView messageText;
    private TextView percentText;
    
    public ProgressDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);
        
        progressBar = findViewById(R.id.progress_bar);
        messageText = findViewById(R.id.message_text);
        percentText = findViewById(R.id.percent_text);
    }
    
    public void setProgress(int progress) {
        progressBar.setProgress(progress);
        percentText.setText(progress + "%");
    }
    
    public void setMessage(String message) {
        messageText.setText(message);
    }
} 