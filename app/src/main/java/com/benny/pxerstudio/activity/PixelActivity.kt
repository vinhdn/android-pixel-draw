package com.benny.pxerstudio.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Html
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.folderselector.FileChooserDialog
import com.benny.pxerstudio.R
import com.benny.pxerstudio.colorpicker.ColorPicker
import com.benny.pxerstudio.colorpicker.SatValView
import com.benny.pxerstudio.pxerexportable.AtlasExportable
import com.benny.pxerstudio.pxerexportable.FolderExportable
import com.benny.pxerstudio.pxerexportable.GifExportable
import com.benny.pxerstudio.pxerexportable.PngExportable
import com.benny.pxerstudio.shape.EraserShape
import com.benny.pxerstudio.shape.LineShape
import com.benny.pxerstudio.shape.RectShape
import com.benny.pxerstudio.util.Tool
import com.benny.pxerstudio.widget.FastBitmapView
import com.benny.pxerstudio.widget.PxerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback
import kotlinx.android.synthetic.main.activity_pixel.*
import kotlinx.android.synthetic.main.content_pixel.*
import java.io.File
import java.util.*

class PixelActivity : AppCompatActivity(), FileChooserDialog.FileCallback, ItemTouchCallback, PxerView.OnDropperCallBack {

    companion object {
        val UNTITLED = "Untitled"
        val rectShapeFactory = RectShape()
        val lineShapeFactory = LineShape()
        val eraserShapeFactory = EraserShape()
        var currentProjectPath: String? = null
    }

    private lateinit var cp: ColorPicker

    private var onlyShowSelected: Boolean = false

    fun setTitle(subtitle: String?, edited: Boolean) {
        title_text_view.text = Html.fromHtml("Pixel<br><small><small>" + if (subtitle.isNullOrEmpty()) UNTITLED else subtitle + (if (edited) "*" else "") + "</small></small>")
    }

    private lateinit var previousMode: PxerView.Mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pixel)

        setTitle(UNTITLED, false)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        title_text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)

        val pxerPref = getSharedPreferences("pxerPref", Context.MODE_PRIVATE)
        pxerView.selectedColor = pxerPref.getInt("lastUsedColor", Color.YELLOW)
        pxerView.setDropperCallBack(this)

        setUpLayersView()
        setupControl()

//        currentProjectPath = pxerPref.getString("lastOpenedProject", null)
//        if (!currentProjectPath.isNullOrEmpty()) {
//            val file = File(currentProjectPath!!)
//            if (file.exists()) {
//                pxerView.loadProject(file)
//                setTitle(Tool.stripExtension(file.name), false)
//            }
//        } else {
            val drawable = intent?.extras?.getInt("drawable") ?: R.drawable.im100
            pxerView.createBlankProject("", 130, 130, drawable)
//        }
        System.gc()
    }

    override fun onColorDropped(newColor: Int) {
        fab_color.setColor(newColor)
        cp.setColor(newColor)

        fab_dropper.callOnClick()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onPostCreate(savedInstanceState)
    }

    fun onProjectTitleClicked (view: View){
        openProjectManager()
    }

    fun onToggleToolsPanel(view: View) {
    }

    private fun setupControl() {

        fab_color.setColor(pxerView.selectedColor)
        fab_color.colorNormal = pxerView.selectedColor
        fab_color.colorPressed = pxerView.selectedColor
        cp = ColorPicker(this, pxerView.selectedColor, SatValView.OnColorChangeListener { newColor ->
            pxerView.selectedColor = newColor
            fab_color.setColor(newColor)
        })
        fab_color.setOnClickListener { view -> cp.show(view) }
        fab_dropper.setOnClickListener {
            if (pxerView.mode == PxerView.Mode.Dropper){
                pxerView.mode = previousMode
                fab_dropper.setImageResource(R.drawable.ic_colorize_24dp)
            }else{

                previousMode = pxerView.mode
                pxerView.mode = PxerView.Mode.Dropper
                fab_dropper.setImageResource(R.drawable.ic_close_24dp)
            }
        }
    }

    private fun setUpLayersView() {

    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {

        pxerView.moveLayer(oldPosition, newPosition)

        if (oldPosition < newPosition) {
            for (i in oldPosition + 1..newPosition) {
            }
        } else {
            for (i in oldPosition - 1 downTo newPosition) {

            }
        }

        return true
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        pxerView.currentLayer = newPosition
    }

    fun onLayerUpdate() {
    }

    private fun openProjectManager() {
        pxerView.save(false)
        startActivityForResult(Intent(this, ProjectManagerActivity::class.java), 659)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 659 && data != null) {
            val path = data.getStringExtra("selectedProjectPath")
            if (path != null && !path.isEmpty()) {
                currentProjectPath = path
                val file = File(path)
                if (file.exists()) {
                    pxerView.loadProject(file)
                    setTitle(Tool.stripExtension(file.name), false)
                }
            } else if (data.getBooleanExtra("fileNameChanged", false)) {
                currentProjectPath = ""
                pxerView.projectName = ""
                recreate()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createNewProject() {
        val l = layoutInflater.inflate(R.layout.dialog_activity_drawing_newproject, null) as ConstraintLayout
        val editText = l.findViewById<EditText>(R.id.et1) as EditText
        val seekBar = l.findViewById<SeekBar>(R.id.sb) as SeekBar
        val textView = l.findViewById<TextView>(R.id.tv2) as TextView
        val seekBar2 = l.findViewById<SeekBar>(R.id.sb2) as SeekBar
        val textView2 = l.findViewById<TextView>(R.id.tv3) as TextView
        seekBar.max = 127
        seekBar.progress = 39
        textView.text = "Width : " + 40
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textView.text = "Width : " + (i + 1).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        seekBar2.max = 127
        seekBar2.progress = 39
        textView2.text = "Height : " + 40
        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textView2.text = "Height : " + (i + 1).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        MaterialDialog.Builder(this)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .customView(l, false)
                .title(R.string.newproject)
                .positiveText(R.string.create)
                .negativeText(R.string.cancel)
                .onPositive(MaterialDialog.SingleButtonCallback { _, _ ->
                    if (editText.text.toString().isEmpty()) return@SingleButtonCallback
                    setTitle(editText.text.toString(), true)
                    pxerView.createBlankProject(editText.text.toString(), seekBar.progress + 1, seekBar2.progress + 1)
                })
                .show()
        pxerView.save(false)
    }

    override fun onFileSelection(dialog: FileChooserDialog, file: File) {
        pxerView.loadProject(file)
        setTitle(Tool.stripExtension(file.name), false)
        currentProjectPath = file.path
    }

    override fun onFileChooserDismissed(dialog: FileChooserDialog) {

    }

    override fun onStop() {
        saveState()
        super.onStop()
    }

    private fun saveState() {
        val pxerPref = getSharedPreferences("pxerPref", Context.MODE_PRIVATE)
        pxerPref.edit()
                .putString("lastOpenedProject", currentProjectPath)
                .putInt("lastUsedColor", pxerView.selectedColor)
                .apply()
        if (!pxerView.projectName.isNullOrEmpty() || pxerView.projectName != UNTITLED)
            pxerView.save(false)
        else
            pxerView.save(true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        cp.onConfigChanges()
    }

    private inner class LayerThumbItem : AbstractItem<LayerThumbItem, LayerThumbItem.ViewHolder>() {
        var pressedTime = 0

        val isPressSecondTime: Boolean
            get() = pressedTime == 2

        fun pressed() {
            pressedTime++
            pressedTime = Math.min(2, pressedTime)
        }

        override fun getType(): Int {
            return R.id.item_layer_thumb
        }

        override fun getLayoutRes(): Int {
            return R.layout.item_layer_thumb
        }

        override fun bindView(viewHolder: ViewHolder, payloads: List<*>?) {
            super.bindView(viewHolder, payloads)
            viewHolder.iv.isSelected = isSelected

            val layer = pxerView.pxerLayers[viewHolder.layoutPosition]
            viewHolder.iv.setVisible(layer.visible)
            viewHolder.iv.bitmap = layer.bitmap
        }

        override fun isSelectable(): Boolean {
            return true
        }

        override fun withSetSelected(selected: Boolean): LayerThumbItem {
            if (!selected)
                pressedTime = 0
            return super.withSetSelected(selected)
        }

        override fun getViewHolder(v: View): ViewHolder {
            return ViewHolder(v)
        }

        internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var iv: FastBitmapView = view as FastBitmapView
        }
    }

    private inner class ToolItem(var icon: Int) : AbstractItem<ToolItem, ToolItem.ViewHolder>() {

        override fun getType(): Int {
            return R.id.item_tool
        }

        override fun getLayoutRes(): Int {
            return R.layout.item_tool
        }

        override fun bindView(viewHolder: ViewHolder, payloads: List<*>?) {
            super.bindView(viewHolder, payloads)

            if (isSelected)
                viewHolder.iv.alpha = 1f
            else
                viewHolder.iv.alpha = 0.3f

            viewHolder.iv.setImageResource(icon)
        }

        override fun isSelectable(): Boolean {
            return true
        }

        override fun getViewHolder(v: View): ViewHolder {
            return ViewHolder(v)
        }

        internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var iv: ImageView = view as ImageView
        }
    }
}