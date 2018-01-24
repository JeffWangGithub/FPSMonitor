package com.glan.blockdetect;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.glan.detector.FPSMonitor;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, FPSMonitor.FPSChanged {

    private TextView mTitle;
    private Button mBtn;
    private TextView mStackInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FPSMonitor.FPSCalcuValve(600)
                .frameValve(120)
                .FPSListener(this)
                .start();

        mTitle = (TextView) findViewById(R.id.title);
        mStackInfo = (TextView) findViewById(R.id.stack_info);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Random random = new Random();
        int rand = random.nextInt(100);
        try {
            Thread.sleep(rand + 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFPSChanged(int fps) {
        mTitle.setText(fps + "");
    }

    @Override
    public void onStackChanged(String stackInfo) {
        mStackInfo.setText(stackInfo);
    }
}
