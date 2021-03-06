package com.example.androidscript.Activities.ArkKnights;

import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.androidscript.FloatingWidget.FloatingWidgetService;
import com.example.androidscript.Activities.StartService;
import com.example.androidscript.R;
import com.example.androidscript.UITemplate.Editor;
import com.example.androidscript.util.BtnMaker;
import com.example.androidscript.util.DebugMessage;
import com.example.androidscript.util.FileOperation;
import com.example.androidscript.util.ScreenShot;

import java.util.Vector;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ArkKnightsEditor extends Editor {
    SwitchCompat TillEmpty;
    SwitchCompat EatMedicine;
    SwitchCompat EatStone;
    EditText Repeat;
    private boolean isTillEmpty = false;
    private boolean isEatMedicine = false;
    private boolean isEatStone = false;
    private String SelectedScript;
    private int nRepetition;
    public static final String FolderName = "ArkKnights/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ark_knights_editor);
        Repeat = findViewById(R.id.Repetition);
        TillEmpty = findViewById(R.id.tillEmpty);
        TillEmpty.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Repeat.setVisibility(View.GONE);
            } else {
                Repeat.setVisibility(View.VISIBLE);
            }
        });
        EatMedicine = findViewById(R.id.EatMedicine);
        EatStone = findViewById(R.id.EatStone);
        BtnMaker.registerOnClick(R.id.set_script, this, v -> {
            if(!StartService.IsAuthorized(this)){
                this.startActivity(new Intent(this, StartService.class).putExtra("Orientation", "Landscape"));
            }else{
                CheckState();
                GetRepetition();
                if (isEatStone) {
                    FloatingWidgetService.setScript(FolderName, "AutoFightEat.txt", new String[]{String.valueOf(nRepetition), "PressRestore.png"});
                } else if (isEatMedicine) {
                    FloatingWidgetService.setScript(FolderName, "AutoFightEat.txt", new String[]{String.valueOf(nRepetition), "PressRestoreMedicine.png"});
                } else {
                    FloatingWidgetService.setScript(FolderName, "AutoFight.txt", new String[]{String.valueOf(nRepetition)});
                }
                StartService.StartFloatingWidget(this);
            }
        });
    }

    private void GetRepetition() {
        if (isTillEmpty) {
            nRepetition = 100000;
        } else {
            try {
                nRepetition = Integer.parseInt(Repeat.getText().toString());
            } catch (NumberFormatException e) {
                nRepetition = 0;
            }
        }
    }

    private void CheckState() {
        isTillEmpty = TillEmpty.isChecked();
        isEatMedicine = EatMedicine.isChecked();
        isEatStone = EatStone.isChecked();
    }

    @Override
    public String getFolderName() {
        return FolderName;
    }

    double resizeRatio;

    @Override
    protected void resourceInitialize() {
        try {
            FileOperation.readDir(FolderName);
            String[] allFiles = getAssets().list(FolderName);//List all file
            for (String file : allFiles) {
                    getResource(FolderName, file);
            }
        } catch (Exception e) {
            DebugMessage.printStackTrace(e);
        }
        resizeRatio = min(ScreenShot.getHeight(), ScreenShot.getWidth()) / 1152.0;//ArkKnights seems to be height dominate.
        // resizeRatio = ScreenShot.getWidth() / 2432.0;
        writePress();
        writeTryPress();
    }

    private void writePress() {
        Vector<String> buffer = new Vector<>();
        buffer.add("Tag $Start");
        buffer.add("ClickPic $1 " + resizeRatio);
        buffer.add("Wait $2");
        buffer.add("IfGreater $R 0");
        buffer.add("JumpTo $Start");
        FileOperation.writeLines(FolderName + "Press.txt", buffer);
    }

    private void writeTryPress() {
        Vector<String> buffer = new Vector<>();
        buffer.add("ClickPic $1 " + resizeRatio);
        buffer.add("Return $R");
        FileOperation.writeLines(FolderName + "TryPress.txt", buffer);
    }
}