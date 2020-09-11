package me.dkzwm.graphics.wmm.demo

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.dkzwm.graphics.wmm.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val mSimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val mTextColor = Color.parseColor("#33FFFFFF")
    private lateinit var mSize: Size
    private lateinit var mDisplayMetrics: DisplayMetrics
    private lateinit var mImageMark: ImageMark
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mDisplayMetrics = resources.displayMetrics
        mSize = ImageSize(BitmapFactory.decodeResource(resources, R.drawable.galaxy))
        mImageMark =
            ImageMark.Builder.create(BitmapFactory.decodeResource(resources, R.drawable.avatar))
                .build()
        make()
        seekBar_item_rotation_degree.setOnSeekBarChangeListener(
            object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    textView_item_rotation_degree.text = progress.toString(10)
                    make()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )
        seekBar_item_background_image_alpha.setOnSeekBarChangeListener(
            object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    textView_item_background_image_alpha.text = progress.toString(10)
                    make()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )
        radioGroup_item_gravity.setOnCheckedChangeListener { _, _ ->
            make()
        }
        radioGroup_canvas_gravity.setOnCheckedChangeListener { _, _ ->
            make()
        }
        radioGroup_repeat.setOnCheckedChangeListener { _, _ ->
            make()
        }
    }

    private fun make() {
        if (imageView_pic.tag is WatermarkMaker) {
            (imageView_pic.tag as WatermarkMaker).recycle()
        }
        val maker = WatermarkMaker.Builder.create(this, mSize)
            .beginRow()
            .addMark(mImageMark)
            .newTextMark(getString(R.string.nickname)).setTextColor(mTextColor)
            .setTextSize(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    14F,
                    mDisplayMetrics
                )
            )
            .done()
            .drawBaseOnBaseline(false)
            .endRow()
            .beginRow()
            .newTextMark(
                String.format(
                    getString(R.string.operation_time),
                    mSimpleDateFormat.format(Date())
                )
            ).setTextColor(mTextColor).done()
            .endRow()
            .beginRow()
            .newTextMark(getString(R.string.only_be_used_for_this_business))
            .setTextColor(mTextColor)
            .done()
            .endRow()
            .setItemDegree(seekBar_item_rotation_degree.progress.toFloat())
            .setItemGravity(getItemGravity())
            .setItemBackgroundImage(
                BitmapFactory.decodeResource(resources, R.drawable.unicorn),
                seekBar_item_background_image_alpha.progress,
                seekBar_item_rotation_degree.progress.toFloat(),
                true
            )
            .setCanvasGravity(getCanvasGravity())
            .setRepeat(getRepeat())
            .build()
        imageView_pic.setImageBitmap(maker.make())
        imageView_pic.tag = maker
    }

    private fun getItemGravity(): WatermarkMaker.ItemGravity {
        return when (radioGroup_item_gravity.checkedRadioButtonId) {
            R.id.radioButton_item_gravity_LEFT ->
                WatermarkMaker.ItemGravity.LEFT
            R.id.radioButton_item_gravity_CENTER ->
                WatermarkMaker.ItemGravity.CENTER
            else ->
                WatermarkMaker.ItemGravity.RIGHT
        }
    }

    private fun getCanvasGravity(): WatermarkMaker.CanvasGravity {
        return when (radioGroup_canvas_gravity.checkedRadioButtonId) {
            R.id.radioButton_canvas_gravity_LEFT_TOP ->
                WatermarkMaker.CanvasGravity.LEFT_TOP
            R.id.radioButton_canvas_gravity_LEFT_BOTTOM ->
                WatermarkMaker.CanvasGravity.LEFT_BOTTOM
            R.id.radioButton_canvas_gravity_CENTER ->
                WatermarkMaker.CanvasGravity.CENTER
            R.id.radioButton_canvas_gravity_RIGHT_TOP ->
                WatermarkMaker.CanvasGravity.RIGHT_TOP
            else ->
                WatermarkMaker.CanvasGravity.RIGHT_BOTTOM
        }
    }

    private fun getRepeat(): WatermarkMaker.Repeat {
        return when (radioGroup_repeat.checkedRadioButtonId) {
            R.id.radioButton_repeat_NONE ->
                WatermarkMaker.Repeat.NONE
            R.id.radioButton_repeat_REPEAT ->
                WatermarkMaker.Repeat.REPEAT
            R.id.radioButton_repeat_ODD ->
                WatermarkMaker.Repeat.ODD
            else ->
                WatermarkMaker.Repeat.EVEN
        }
    }
}
