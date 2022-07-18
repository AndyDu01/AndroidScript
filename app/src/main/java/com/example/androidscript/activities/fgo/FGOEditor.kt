package com.example.androidscript.activities.fgo

import com.example.androidscript.util.DebugMessage
import com.example.androidscript.util.FileOperation
import com.example.androidscript.uitemplate.UIEditor
import android.os.Bundle
import com.example.androidscript.util.BtnMaker
import com.example.androidscript.R
import com.example.androidscript.floatingwidget.FloatingWidgetService
import android.widget.Toast
import com.example.androidscript.util.ScriptCompiler
import android.view.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import java.lang.Exception
import java.util.*

class FGOEditor : UIEditor() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BtnMaker.registerOnClick(
            R.id.start_service,
            this
        ) { startServiceHandler("Landscape") }
        BtnMaker.registerOnClick(R.id.save_file, this) {
            var syntaxFlag = true
            for (Line in blockData) {
                if (Line!!.contains("")) {
                    syntaxFlag = false
                    break
                }
            }
            if (syntaxFlag) {
                FileOperation.writeWords(folderName + fileName, blockData)
                compiler.compile(blockData)
                FloatingWidgetService.setScript(folderName, "Run.txt", null)
                Toast.makeText(this.applicationContext, "Successful!!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    this.applicationContext,
                    "Arguments can't be empty",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        //Set blocks' data
        if (FileOperation.findFile(folderName, fileName)) {
            blockData = FileOperation.readFromFileWords(folderName + fileName)
        } else {
            blockData.add(Vector(listOf(*PreStageBlock)))
            blockData.add(Vector(listOf(*SkillBlock)))
            blockData.add(Vector(listOf(*NoblePhantasmsBlock)))
            blockData.add(Vector(listOf(*CraftSkillBlock)))
            blockData.add(Vector(listOf(*EndBlock)))
        }
        blockView.layoutManager = LinearLayoutManager(this)
        blockView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        blockView.adapter = FGOBlockAdapter(blockData)
        //SetButtonData
        buttonData.add("SelectCard")
        buttonData.add("CraftSkill")
        buttonData.add("ServantSkill")
        buttonView.layoutManager = GridLayoutManager(this, 2)
        buttonView.adapter = FGOButtonAdapter(
            blockData, buttonData, (Objects.requireNonNull(
                blockView.adapter
            ) as FGOBlockAdapter).onOrderChange
        )
    }

    override fun resourceInitialize() {
        try {
            FileOperation.readDir(folderName) //Since we access depth 2 folder later, make sure depth 1 is built;
            FileOperation.readDir(folderName + "Friend/")
            FileOperation.readDir(folderName + "Craft/")
            for (folder in arrayOf(
                folderName,
                folderName + "Friend/",
                folderName + "Craft/"
            )) {
                for (file in assets.list(folder)!!) {
                    getResource(folder, file!!)
                }
            }
        } catch (e: Exception) {
            DebugMessage.printStackTrace(e)
        }
    }

    companion object {
        const val folderName = "FGO/"
        val PreStageBlock = arrayOf("PreStage", "0", "0", "0", "1", "0", "0", "0", "0", "0")
        val SkillBlock = arrayOf("Skill", "0", "0", "0", "0", "0", "0", "0", "0", "0")
        val CraftSkillBlock = arrayOf("CraftSkill", "0", "0", "0", "0")
        val NoblePhantasmsBlock = arrayOf("NoblePhantasms", "0", "0", "0", "0")
        val EndBlock = arrayOf("End")
        val compiler: ScriptCompiler = FGOScriptCompiler()
    }
}