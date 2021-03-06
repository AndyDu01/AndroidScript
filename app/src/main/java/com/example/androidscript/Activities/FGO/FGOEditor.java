package com.example.androidscript.Activities.FGO;

import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.androidscript.FloatingWidget.FloatingWidgetService;
import com.example.androidscript.R;
import com.example.androidscript.UITemplate.UIEditor;
import com.example.androidscript.util.BtnMaker;
import com.example.androidscript.util.DebugMessage;
import com.example.androidscript.util.FileOperation;
import com.example.androidscript.util.ScriptCompiler;

import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;

public class FGOEditor extends UIEditor {

    public static final String FolderName = "FGO/";
    public static final String[] PreStageBlock = {"PreStage", "0", "0", "0", "1", "0", "0", "0", "0", "0"};
    public static final String[] SkillBlock = {"Skill", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
    public static final String[] CraftSkillBlock = {"CraftSkill", "0", "0", "0", "0"};
    public static final String[] NoblePhantasmsBlock = {"NoblePhantasms", "0", "0", "0", "0"};
    public static final String[] EndBlock = {"End"};
    public static final ScriptCompiler compiler = new FGOScriptCompiler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BtnMaker.registerOnClick(R.id.start_service, this, v -> startServiceHandler("Landscape"));
        BtnMaker.registerOnClick(R.id.save_file, this, (v -> {
            boolean syntaxFlag = true;
            for (Vector<String> Line : this.BlockData) {
                if (Line.contains("")) {
                    syntaxFlag = false;
                    break;
                }
            }
            if (syntaxFlag) {
                FileOperation.writeWords(FGOEditor.FolderName + this.filename, this.BlockData);
                compiler.compile(this.BlockData);
                FloatingWidgetService.setScript(FGOEditor.FolderName, "Run.txt", null);
                Toast.makeText(this.getApplicationContext(), "Successful!!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this.getApplicationContext(), "Arguments can't be empty", Toast.LENGTH_LONG).show();
            }
        }
        ));
        //SetBlockData

        if (FileOperation.findFile(FGOEditor.FolderName, this.filename)) {
            this.BlockData = FileOperation.readFromFileWords(FGOEditor.FolderName + this.filename);

        } else {
            this.BlockData.add(new Vector<>(Arrays.asList(PreStageBlock)));
            this.BlockData.add(new Vector<>(Arrays.asList(SkillBlock)));
            this.BlockData.add(new Vector<>(Arrays.asList(NoblePhantasmsBlock)));
            this.BlockData.add(new Vector<>(Arrays.asList(CraftSkillBlock)));
            this.BlockData.add(new Vector<>(Arrays.asList(EndBlock)));
        }

        this.BlockView.setLayoutManager(new LinearLayoutManager(this));
        this.BlockView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.BlockView.setAdapter(new FGOBlockAdapter(BlockData));
        //SetButtonData
        this.ButtonData.add("SelectCard");
        this.ButtonData.add("CraftSkill");
        this.ButtonData.add("ServantSkill");

        this.ButtonView.setLayoutManager(new GridLayoutManager(this, 2));
        this.ButtonView.setAdapter(new FGOButtonAdapter(BlockData, ButtonData, ((FGOBlockAdapter) Objects.requireNonNull(BlockView.getAdapter())).onOrderChange));
    }

    @Override
    public String getFolderName() {
        return FolderName;
    }

    @Override
    protected void resourceInitialize() {
        try {
            FileOperation.readDir(FolderName);//Since we access depth 2 folder later, make sure depth 1 is built;
            FileOperation.readDir(FolderName + "Friend/");
            FileOperation.readDir(FolderName + "Craft/");
            for (String folder : new String[]{FolderName, FolderName + "Friend/", FolderName + "Craft/"}) {
                for (String file : getAssets().list(folder)) {
                    getResource(folder, file);
                }
            }
        } catch (Exception e) {
            DebugMessage.printStackTrace(e);
        }
    }
}
