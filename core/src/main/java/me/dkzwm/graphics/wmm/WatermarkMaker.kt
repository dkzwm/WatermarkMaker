/*
 * MIT License
 *
 * Copyright (c) 2020 dkzwm
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.dkzwm.graphics.wmm

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class WatermarkMaker private constructor(
    private val mContext: Context,
    private val mSize: Size,
    private val mMarkRows: List<MarkRow>,
    private val mItemDegree: Float,
    private val mRowSpacing: Float,
    private val mColumnSpacing: Float,
    private val mHorizontalSpacing: Float,
    private val mVerticalSpacing: Float,
    private val mRepeat: Repeat,
    private val mPosition: FloatArray,
    @ColorInt
    private val mItemBackgroundColor: Int,
    private val mItemBackgroundImage: Bitmap?,
    private val mItemBackgroundImageDegree: Float,
    private val mItemBackgroundImageScale: Float,
    private val mItemBackgroundImageFit: Boolean,
    private val mItemBackgroundImageAlpha: Int,
    private val mCanvasGravity: CanvasGravity,
    private val mItemGravity: ItemGravity
) {
    private val mPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val mMatrix = Matrix()
    private val mRect = Rect()
    private val mRectF = RectF()
    private var mWatermarkBitmap: Bitmap? = null

    fun make(): Bitmap {
        val bitmap = mWatermarkBitmap
        if (bitmap == null) {
            mPaint.isDither = true
            var itemHeight = 0F
            var itemWidth = 0F
            var i = 0
            val rowsWidth = FloatArray(mMarkRows.size)
            val rowsHeight = FloatArray(mMarkRows.size)
            val rowsBaselineOffset = FloatArray(mMarkRows.size)
            val rowsNeedDrawBaseOnBaseline = BooleanArray(mMarkRows.size)
            while (i < mMarkRows.size) {
                val row = mMarkRows[i]
                var width = 0F
                var height = 0F
                var baselineOffset = 0F
                var zeroBaselineHeight = 0F
                var meedDrawBaseOnBaseline = false
                for (j in row.marks.indices) {
                    val column = row.marks[j]
                    column.init(mContext, mPaint, mRect)
                    if (column.isValid) {
                        width += if (j < row.marks.size - 1) {
                            column.width + mColumnSpacing
                        } else {
                            column.width
                        }
                        height = max(column.height, height)
                        baselineOffset = max(column.baselineOffset, baselineOffset)
                        if (column.gravity == RowGravity.BOTTOM && row.isDrawBaseOnBaseline) {
                            if (column.baselineOffset == 0F) {
                                zeroBaselineHeight = max(column.height, zeroBaselineHeight)
                            } else if (!meedDrawBaseOnBaseline && zeroBaselineHeight != 0F) {
                                meedDrawBaseOnBaseline = true
                            }
                        }
                    }
                }
                if (meedDrawBaseOnBaseline) {
                    height = max(zeroBaselineHeight + baselineOffset, height)
                }
                rowsHeight[i] = height
                rowsWidth[i] = width
                rowsBaselineOffset[i] = baselineOffset
                rowsNeedDrawBaseOnBaseline[i] = meedDrawBaseOnBaseline
                itemWidth = max(width, itemWidth)
                itemHeight += if (i < mMarkRows.size) {
                    height + mRowSpacing
                } else {
                    height
                }
                i++
            }
            val height = itemHeight
            val width = itemWidth
            mMatrix.reset()
            mRectF.set(0F, 0F, itemWidth, itemHeight)
            mMatrix.setRotate(mItemDegree, width / 2F, height / 2F)
            mMatrix.mapRect(mRectF)
            val realHeight = mRectF.height().roundToInt()
            val realWidth = mRectF.width().roundToInt()
            val heightSpacing = realHeight - height
            val widthSpacing = realWidth - width
            val itemBitmap = Bitmap.createBitmap(realWidth, realHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(itemBitmap)
            canvas.save()
            canvas.rotate(mItemDegree, realWidth / 2F, realHeight / 2F)
            if (mItemBackgroundImage != null && !mItemBackgroundImage.isRecycled) {
                mPaint.alpha = mItemBackgroundImageAlpha
                val backgroundImageHeight = mItemBackgroundImage.height.toFloat()
                val backgroundImageWidth = mItemBackgroundImage.width.toFloat()
                if (mItemBackgroundImageFit ||
                    mItemBackgroundImageDegree != -1F ||
                    mItemBackgroundImageScale != 1F
                ) {
                    if (mItemBackgroundImageDegree != -1F) {
                        mMatrix.reset()
                        mRectF.set(0F, 0F, backgroundImageWidth, backgroundImageHeight)
                        mMatrix.setRotate(
                            mItemBackgroundImageDegree,
                            backgroundImageWidth / 2F,
                            backgroundImageHeight / 2F
                        )
                        mMatrix.mapRect(mRectF)
                    } else {
                        mRectF.set(0F, 0F, backgroundImageWidth, backgroundImageHeight)
                    }
                    val scale = if (mItemBackgroundImageFit) {
                        min(realHeight, realWidth).toFloat() / max(
                            mRectF.height(),
                            mRectF.width()
                        )
                    } else {
                        mItemBackgroundImageScale
                    }
                    val offsetX = backgroundImageHeight / 2F
                    val offsetY = backgroundImageWidth / 2F
                    mMatrix.reset()
                    mMatrix.postScale(scale, scale, offsetX, offsetY)
                    if (mItemBackgroundImageDegree != -1F) {
                        mMatrix.postRotate(
                            mItemBackgroundImageDegree - mItemDegree,
                            offsetX,
                            offsetY
                        )
                    }
                    val scaledBitmap = Bitmap.createBitmap(
                        mItemBackgroundImage,
                        0,
                        0,
                        mItemBackgroundImage.width,
                        mItemBackgroundImage.height,
                        mMatrix,
                        true
                    )
                    canvas.drawBitmap(
                        scaledBitmap,
                        realWidth / 2F - scaledBitmap.width / 2F,
                        realHeight / 2F - scaledBitmap.height / 2F,
                        mPaint
                    )
                    scaledBitmap.recycle()
                } else {
                    canvas.drawBitmap(
                        mItemBackgroundImage,
                        realWidth / 2F - backgroundImageWidth / 2F,
                        realHeight / 2F - backgroundImageHeight / 2F,
                        mPaint
                    )
                }
                mPaint.alpha = 255
            } else if (mItemBackgroundColor != Color.TRANSPARENT) {
                canvas.drawColor(mItemBackgroundColor)
            }
            var countY = 0F
            i = 0
            var x = widthSpacing / 2F
            var y = heightSpacing / 2F
            while (i < mMarkRows.size) {
                val row = mMarkRows[i]
                val marginLeft = when (mItemGravity) {
                    ItemGravity.CENTER -> {
                        (itemWidth - rowsWidth[i]) / 2F
                    }
                    ItemGravity.LEFT -> {
                        0F
                    }
                    else -> {
                        itemWidth - rowsWidth[i]
                    }
                }
                var columnX = x + marginLeft
                for (mark in row.marks) {
                    if (!mark.isValid) {
                        continue
                    }
                    val marginTop = when (mark.gravity) {
                        RowGravity.CENTER -> {
                            (rowsHeight[i] - mark.height) / 2F
                        }
                        RowGravity.TOP -> {
                            0F
                        }
                        else -> {
                            if (rowsNeedDrawBaseOnBaseline[i]) {
                                rowsHeight[i] - mark.height - (rowsBaselineOffset[i] - mark.baselineOffset)
                            } else {
                                rowsHeight[i] - mark.height
                            }
                        }
                    }
                    mark.draw(canvas, mPaint, columnX, y + countY + marginTop, rowsHeight[i])
                    mark.clearShadowLayer(mPaint, mPaint)
                    columnX += mark.width + mColumnSpacing
                }
                countY += rowsHeight[i] + mRowSpacing
                i++
            }
            canvas.setBitmap(null)
            val sizeWidth = mSize.width
            val sizeHeight = mSize.height
            if (sizeWidth == Size.COMPUTE || sizeHeight == Size.COMPUTE) {
                mWatermarkBitmap = itemBitmap
                return itemBitmap
            }
            val watermarkBitmap =
                Bitmap.createBitmap(sizeWidth, sizeHeight, Bitmap.Config.ARGB_8888)
            val watermarkCanvas = Canvas(watermarkBitmap)
            mPaint.reset()
            mPaint.isAntiAlias = true
            mPaint.isDither = true
            mSize.draw(watermarkCanvas, mPaint)
            canvas.save()
            if (mRepeat != Repeat.NONE) {
                val positionStart = if (mRepeat == Repeat.ODD) 0 else 1
                var position = positionStart
                val needStagger = mRepeat != Repeat.REPEAT
                when (mCanvasGravity) {
                    CanvasGravity.LEFT_TOP -> {
                        y = 0F
                        while (y < sizeHeight) {
                            x =
                                if ((needStagger) && (position++ % 2 == 1)) realWidth + mHorizontalSpacing else 0F
                            while (x < sizeWidth) {
                                watermarkCanvas.drawBitmap(itemBitmap, x, y, mPaint)
                                x += if (needStagger) (realWidth + mHorizontalSpacing) * 2 else realWidth + mHorizontalSpacing
                            }
                            y += realHeight + mVerticalSpacing
                        }
                    }
                    CanvasGravity.LEFT_BOTTOM -> {
                        y = (sizeHeight - realHeight).toFloat()
                        while (y > -realHeight - mVerticalSpacing) {
                            x =
                                if ((needStagger) && (position++ % 2 == 1)) realWidth + mHorizontalSpacing else 0F
                            while (x < sizeWidth) {
                                watermarkCanvas.drawBitmap(itemBitmap, x, y, mPaint)
                                x += if (needStagger) (realWidth + mHorizontalSpacing) * 2 else realWidth + mHorizontalSpacing
                            }
                            y -= realHeight + mVerticalSpacing
                        }
                    }
                    CanvasGravity.CENTER -> {
                        val centerStartX = sizeWidth / 2F - realWidth / 2F
                        val centerStartY = sizeHeight / 2F - realHeight / 2F
                        y = centerStartY
                        while (y > -realHeight - mVerticalSpacing) {
                            val stagger = if (needStagger) position++ % 2 == 1 else false
                            x =
                                if (stagger) (centerStartX - realWidth - mHorizontalSpacing) else centerStartX
                            while (x > -realWidth - mHorizontalSpacing) {
                                watermarkCanvas.drawBitmap(itemBitmap, x, y, mPaint)
                                x -= if (needStagger) (realWidth + mHorizontalSpacing) * 2 else realWidth + mHorizontalSpacing
                            }
                            x =
                                if (!needStagger || stagger) (centerStartX + realWidth + mHorizontalSpacing) else centerStartX + (realWidth + mHorizontalSpacing) * 2
                            while (x < sizeWidth) {
                                watermarkCanvas.drawBitmap(itemBitmap, x, y, mPaint)
                                x += if (needStagger) (realWidth + mHorizontalSpacing) * 2 else realWidth + mHorizontalSpacing
                            }
                            y -= realHeight + mVerticalSpacing
                        }
                        y = centerStartY + realHeight + mVerticalSpacing
                        position = positionStart + 1
                        while (y < sizeHeight) {
                            val stagger = if (needStagger) position++ % 2 == 1 else false
                            x =
                                if (stagger) (centerStartX - realWidth - mHorizontalSpacing) else centerStartX
                            while (x > -itemWidth) {
                                watermarkCanvas.drawBitmap(itemBitmap, x, y, mPaint)
                                x -= if (needStagger) (realWidth + mHorizontalSpacing) * 2 else realWidth + mHorizontalSpacing
                            }
                            x =
                                if (!needStagger || stagger) (centerStartX + realWidth + mHorizontalSpacing) else centerStartX + (realWidth + mHorizontalSpacing) * 2
                            while (x < sizeWidth) {
                                watermarkCanvas.drawBitmap(itemBitmap, x, y, mPaint)
                                x += if (needStagger) (realWidth + mHorizontalSpacing) * 2 else realWidth + mHorizontalSpacing
                            }
                            y += realHeight + mVerticalSpacing
                        }
                    }
                    CanvasGravity.RIGHT_TOP -> {
                        y = 0F
                        while (y < sizeHeight) {
                            x =
                                if ((needStagger) && (position++ % 2 == 1)) sizeWidth - realWidth * 2 - mHorizontalSpacing else (sizeWidth - realWidth).toFloat()
                            while (x > -realWidth - mHorizontalSpacing) {
                                watermarkCanvas.drawBitmap(itemBitmap, x, y, mPaint)
                                x -= if (needStagger) (realWidth + mHorizontalSpacing) * 2 else realWidth + mHorizontalSpacing
                            }
                            y += realHeight + mVerticalSpacing
                        }
                    }
                    else -> {
                        y = (sizeHeight - realHeight).toFloat()
                        while (y > -realHeight - mVerticalSpacing) {
                            x =
                                if ((needStagger) && (position++ % 2 == 1)) sizeWidth - realWidth * 2 - mHorizontalSpacing else (sizeWidth - realWidth).toFloat()
                            while (x > -realWidth - mHorizontalSpacing) {
                                watermarkCanvas.drawBitmap(itemBitmap, x, y, mPaint)
                                x -= if (needStagger) (realWidth + mHorizontalSpacing) * 2 else realWidth + mHorizontalSpacing
                            }
                            y -= realHeight + mVerticalSpacing
                        }
                    }
                }
            } else {
                x = mPosition[0]
                y = mPosition[1]
                when (mCanvasGravity) {
                    CanvasGravity.LEFT_BOTTOM -> {
                        y -= realHeight
                    }
                    CanvasGravity.CENTER -> {
                        x -= realWidth / 2F
                        y -= realHeight / 2F
                    }
                    CanvasGravity.RIGHT_TOP -> {
                        x -= realWidth
                    }
                    CanvasGravity.RIGHT_BOTTOM -> {
                        x -= realWidth
                        y -= realHeight
                    }
                    else -> {
                    }
                }
                watermarkCanvas.drawBitmap(itemBitmap, x, y, mPaint)
            }
            watermarkCanvas.setBitmap(null)
            itemBitmap.recycle()
            mWatermarkBitmap = watermarkBitmap
            return watermarkBitmap
        }
        return bitmap
    }

    fun recycle() {
        val bitmap = mWatermarkBitmap
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
        }
        mWatermarkBitmap = null
    }

    enum class Repeat(val def: Int) {
        NONE(-1), REPEAT(0), ODD(1), EVEN(2)
    }

    enum class CanvasGravity(val def: Int) {
        LEFT_TOP(0),
        LEFT_BOTTOM(1),
        CENTER(2),
        RIGHT_TOP(3),
        RIGHT_BOTTOM(4),
    }

    enum class RowGravity(val def: Int) {
        TOP(0),
        CENTER(1),
        BOTTOM(2)
    }

    enum class ItemGravity(val def: Int) {
        LEFT(0),
        CENTER(1),
        RIGHT(2),
    }

    class Builder private constructor(
        private val mContext: Context,
        private val mSize: Size
    ) {

        private val mMarkRows = ArrayList<MarkRow>()
        private var mItemDegree = 0F
        private var mRowSpacing = 0F
        private var mColumnSpacing = 0F
        private var mHorizontalSpacing = 12F
        private var mVerticalSpacing = 12F
        private var mRepeat = Repeat.NONE
        private var mPosition = FloatArray(2) { _ -> 0F }

        @ColorInt
        private var mItemBackgroundColor = Color.TRANSPARENT

        private var mItemBackgroundImage: Bitmap? = null
        private var mItemBackgroundImageDegree = -1F
        private var mItemBackgroundImageScale = 1F
        private var mItemBackgroundImageFit = false
        private var mItemBackgroundImageAlpha = 255
        private var mCanvasGravity = CanvasGravity.CENTER
        private var mItemGravity = ItemGravity.CENTER

        fun beginRow(): MarkRowBuilder {
            return MarkRowBuilder.create(this)
        }

        fun setItemDegree(degree: Float): Builder {
            mItemDegree = degree
            return this
        }

        fun setRowSpacing(rowSpacing: Float): Builder {
            mRowSpacing = rowSpacing
            return this
        }

        fun setColumnSpacing(columnSpacing: Float): Builder {
            mColumnSpacing = columnSpacing
            return this
        }

        fun setHorizontalSpacing(horizontalSpacing: Float): Builder {
            mHorizontalSpacing = horizontalSpacing
            return this
        }

        fun setVerticalSpacing(verticalSpacing: Float): Builder {
            mVerticalSpacing = verticalSpacing
            return this
        }

        fun setRepeat(repeat: Repeat, x: Float = 0F, y: Float = 0F): Builder {
            mRepeat = repeat
            mPosition[0] = x
            mPosition[1] = y
            return this
        }

        fun setItemBackgroundColor(@ColorInt color: Int): Builder {
            mItemBackgroundColor = color
            mItemBackgroundImage = null
            mItemBackgroundColor = Color.TRANSPARENT
            return this
        }

        fun setItemBackgroundImage(
            bitmap: Bitmap,
            alpha: Int,
            degree: Float,
            scale: Float
        ): Builder {
            mItemBackgroundImage = bitmap
            mItemBackgroundImageAlpha = alpha
            mItemBackgroundImageDegree = degree
            mItemBackgroundImageScale = scale
            mItemBackgroundImageFit = false
            mItemBackgroundColor = Color.TRANSPARENT
            return this
        }

        fun setItemBackgroundImage(
            bitmap: Bitmap,
            alpha: Int,
            degree: Float,
            fit: Boolean
        ): Builder {
            mItemBackgroundImage = bitmap
            mItemBackgroundImageAlpha = alpha
            mItemBackgroundImageDegree = degree
            mItemBackgroundImageFit = fit
            mItemBackgroundImageScale = 1F
            mItemBackgroundColor = Color.TRANSPARENT
            return this
        }

        fun setCanvasGravity(gravity: CanvasGravity): Builder {
            mCanvasGravity = gravity
            return this
        }

        fun setItemGravity(gravity: ItemGravity): Builder {
            mItemGravity = gravity
            return this
        }

        fun build(): WatermarkMaker {
            return WatermarkMaker(
                mContext,
                mSize,
                mMarkRows,
                mItemDegree,
                mRowSpacing,
                mColumnSpacing,
                mHorizontalSpacing,
                mVerticalSpacing,
                mRepeat,
                mPosition,
                mItemBackgroundColor,
                mItemBackgroundImage,
                mItemBackgroundImageDegree,
                mItemBackgroundImageScale,
                mItemBackgroundImageFit,
                mItemBackgroundImageAlpha,
                mCanvasGravity,
                mItemGravity
            )
        }

        companion object {
            @JvmStatic
            fun create(context: Context, size: Size): Builder {
                return Builder(context, size)
            }
        }

        class MarkRowBuilder private constructor(private val mBuilder: Builder) {
            private val mMarks: MutableList<Mark> = ArrayList()
            private var mIsDrawBaseOnBaseline = false
            fun addMark(mark: Mark): MarkRowBuilder {
                mMarks.add(mark)
                return this
            }

            fun drawBaseOnBaseline(onBaseline: Boolean): MarkRowBuilder {
                mIsDrawBaseOnBaseline = onBaseline
                return this
            }

            fun newTextMark(text: String): ITextMarkBuilder {
                return TextMarkBuilder(this, text, 0)
            }

            fun newTextMark(@StringRes textResId: Int): ITextMarkBuilder {
                return TextMarkBuilder(this, null, textResId)
            }

            fun newImageMark(bitmap: Bitmap): IImageMarkBuilder {
                return ImageMarkBuilder(this, bitmap, 0)
            }

            fun newImageMark(@DrawableRes bitmapResId: Int): IImageMarkBuilder {
                return ImageMarkBuilder(this, null, bitmapResId)
            }

            fun endRow(): Builder {
                mBuilder.mMarkRows.add(MarkRow(mMarks, mIsDrawBaseOnBaseline))
                return mBuilder
            }

            companion object {
                @JvmStatic
                internal fun create(builder: Builder): MarkRowBuilder {
                    return MarkRowBuilder(builder)
                }
            }
        }

        interface ITextMarkBuilder : TextMark.IBuilder<ITextMarkBuilder> {
            fun done(): MarkRowBuilder
        }

        class TextMarkBuilder constructor(
            markMarkRow: MarkRowBuilder,
            text: String?,
            @StringRes textResId: Int
        ) :
            TextMark.Builder<ITextMarkBuilder>(text, textResId), ITextMarkBuilder {
            private val mWatermarkRow = markMarkRow
            override fun done(): MarkRowBuilder {
                mWatermarkRow.addMark(build())
                return mWatermarkRow
            }
        }

        interface IImageMarkBuilder : ImageMark.IBuilder<IImageMarkBuilder> {
            fun done(): MarkRowBuilder
        }

        class ImageMarkBuilder constructor(
            markMarkRow: MarkRowBuilder,
            bitmap: Bitmap?,
            @DrawableRes bitmapResId: Int
        ) :
            ImageMark.Builder<IImageMarkBuilder>(bitmap, bitmapResId), IImageMarkBuilder {
            private val mWatermarkRow = markMarkRow
            override fun done(): MarkRowBuilder {
                mWatermarkRow.addMark(build())
                return mWatermarkRow
            }
        }
    }
}
