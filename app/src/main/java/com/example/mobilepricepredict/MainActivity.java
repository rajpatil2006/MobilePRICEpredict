package com.example.mobilepricepredict;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.content.res.AssetFileDescriptor;


import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private Interpreter tflite;

    private EditText etBatteryPower, etClockSpeed, etFC, etIntMemory, etMDep,
            etMobileWt, etNCores, etPC, etPxHeight, etPxWidth, etRAM,
            etSCH, etSCW, etTalkTime;

    private Switch switchBluetooth, switchDualSim, switch4G, switch3G, switchTouch, switchWiFi;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        etBatteryPower = findViewById(R.id.etBatteryPower);
        etClockSpeed = findViewById(R.id.etClockSpeed);
        etFC = findViewById(R.id.etFC);
        etIntMemory = findViewById(R.id.etIntMemory);
        etMDep = findViewById(R.id.etMDep);
        etMobileWt = findViewById(R.id.etMobileWt);
        etNCores = findViewById(R.id.etNCores);
        etPC = findViewById(R.id.etPC);
        etPxHeight = findViewById(R.id.etPxHeight);
        etPxWidth = findViewById(R.id.etPxWidth);
        etRAM = findViewById(R.id.etRAM);
        etSCH = findViewById(R.id.etSCH);
        etSCW = findViewById(R.id.etSCW);
        etTalkTime = findViewById(R.id.etTalkTime);

        switchBluetooth = findViewById(R.id.switchBluetooth);
        switchDualSim = findViewById(R.id.switchDualSim);
        switch4G = findViewById(R.id.switch4G);
        switch3G = findViewById(R.id.switch3G);
        switchTouch = findViewById(R.id.switchTouch);
        switchWiFi = findViewById(R.id.switchWiFi);

        tvResult = findViewById(R.id.tvResult);

        Button btnPredict = findViewById(R.id.btnPredict);
        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!areInputsValid()) {
                    Toast.makeText(MainActivity.this, "Please fill all the input fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                float[] input = getInputFeatures();
                float[][] output = new float[1][4];
                tflite.run(input, output);
                int predictedClass = argMax(output[0]);

                String[] priceLabels = {"Low Cost", "Medium Cost", "High Cost", "Very High Cost"};
                String resultText = "Predicted Price Range: " + predictedClass + " (" + priceLabels[predictedClass] + ")";
                tvResult.setText(resultText);
            }
        });


    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    private float[] getInputFeatures() {
        float[] input = new float[20];

        input[0] = getFloat(etBatteryPower);
        input[1] = switchBluetooth.isChecked() ? 1 : 0;
        input[2] = getFloat(etClockSpeed);
        input[3] = switchDualSim.isChecked() ? 1 : 0;
        input[4] = getFloat(etFC);
        input[5] = switch4G.isChecked() ? 1 : 0;
        input[6] = getFloat(etIntMemory);
        input[7] = getFloat(etMDep);
        input[8] = getFloat(etMobileWt);
        input[9] = getFloat(etNCores);
        input[10] = getFloat(etPC);
        input[11] = getFloat(etPxHeight);
        input[12] = getFloat(etPxWidth);
        input[13] = getFloat(etRAM);
        input[14] = getFloat(etSCH);
        input[15] = getFloat(etSCW);
        input[16] = getFloat(etTalkTime);
        input[17] = switch3G.isChecked() ? 1 : 0;
        input[18] = switchTouch.isChecked() ? 1 : 0;
        input[19] = switchWiFi.isChecked() ? 1 : 0;

        return input;
    }

    private float getFloat(EditText editText) {
        String text = editText.getText().toString();
        if (text.isEmpty()) return 0f;
        return Float.parseFloat(text);
    }
    private boolean areInputsValid() {
        EditText[] fields = {
                etBatteryPower, etClockSpeed, etFC, etIntMemory, etMDep,
                etMobileWt, etNCores, etPC, etPxHeight, etPxWidth,
                etRAM, etSCH, etSCW, etTalkTime
        };

        for (EditText field : fields) {
            if (field.getText().toString().trim().isEmpty()) {
                field.setError("Required");
                return false;
            }
        }
        return true;
    }


    private int argMax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) maxIndex = i;
        }
        return maxIndex;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
    }
}
