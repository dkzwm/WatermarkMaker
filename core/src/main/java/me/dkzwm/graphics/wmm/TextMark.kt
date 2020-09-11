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
import androidx.annotation.ColorInt
import androidx.annotation.StringRes

class TextMark private constructor(
    gravity: WatermarkMaker.RowGravity,
    private var mText: String?,
    @StringRes
    private val mTextResId: Int,
    @ColorInt private val mTextColor: Int,
    private val mTextSize: Float,
    alpha: Int,
    private val mTypeface: Typeface?,
    @ColorInt backgroundColor: Int,
    paddingLeft: Float,
    paddingTop: Float,
    paddingRight: Float,
    paddingBottom: Float,
    shadowLayerRadius: Float,
    shadowLayerDx: Float,
    shadowLayerDy: Float,
    @ColorInt shadowLayerColor: Int,
    paint: Paint?
) : Mark(
    gravity,
    alpha,
    backgroundColor,
    paddingLeft,
    paddingTop,
    paddingRight,
    paddingBottom,
    shadowLayerRadius,
    shadowLayerDx,
    shadowLayerDy,
    shadowLayerColor,
    paint
) {
    private val mFontMetrics = Paint.FontMetrics()
    private var mRect = Rect()

    interface IBuilder<T : IBuilder<T>> : Mark.IBuilder<T> {
        fun setTextColor(@ColorInt color: Int): T
        fun setTextSize(size: Float): T
        fun setTypeface(typeface: Typeface?): T
    }

    interface IDefBuildBuilder<T : IBuilder<T>> : IBuilder<T> {
        fun build(): TextMark
    }

    interface ICanBuildBuilder : IDefBuildBuilder<ICanBuildBuilder>

    @Suppress("UNCHECKED_CAST")
    open class Builder<T : IBuilder<T>> protected constructor(
        private var mText: String?,
        @StringRes private var mTextResId: Int
    ) :
        Mark.Builder<T>(),
        IDefBuildBuilder<T> {
        @ColorInt
        private var mTextColor = Color.WHITE
        private var mTextSize = 22F
        private var mTypeface: Typeface? = null

        override fun setTextColor(color: Int): T {
            mTextColor = color
            return this as T
        }

        override fun setTextSize(size: Float): T {
            mTextSize = size
            return this as T
        }

        override fun setTypeface(typeface: Typeface?): T {
            mTypeface = typeface
            return this as T
        }

        override fun build(): TextMark {
            return TextMark(
                mGravity, mText, mTextResId, mTextColor, mTextSize, mAlpha, mTypeface,
                mBackgroundColor, mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom,
                mShadowLayerRadius, mShadowLayerDx, mShadowLayerDy, mShadowLayerColor, mPaint
            )
        }

        companion object {
            @JvmStatic
            fun create(text: String): Builder<ICanBuildBuilder> {
                return Builder(text, 0)
            }

            @JvmStatic
            fun create(@StringRes textResId: Int): Builder<ICanBuildBuilder> {
                return Builder(null, textResId)
            }
        }
    }

    override fun init(context: Context, paint: Paint, rect: Rect) {
        if (mTextResId != 0) {
            mText = context.getString(mTextResId)
        }
        val text = mText
        if (text != null && text.isNotEmpty()) {
            val width: Float
            val height: Float
            val outPaint = mPaint
            if (outPaint == null) {
                val size = paint.textSize
                val typeface = paint.typeface
                paint.textSize = mTextSize
                paint.typeface = mTypeface
                width = paint.measureText(mText)
                paint.getFontMetrics(mFontMetrics)
                height = mFontMetrics.bottom - mFontMetrics.top
                paint.textSize = size
                paint.typeface = typeface
            } else {
                width = outPaint.measureText(mText)
                outPaint.getFontMetrics(mFontMetrics)
                height = mFontMetrics.bottom - mFontMetrics.top
            }
            mBaselineOffset = mFontMetrics.bottom
            mWidth = width + mPaddingLeft + mPaddingRight
            mHeight = height + mPaddingTop + mPaddingBottom
        }
    }

    override fun draw(
        canvas: Canvas,
        paint: Paint,
        startX: Float,
        startTop: Float,
        rowHeight: Float
    ) {
        super.draw(canvas, paint, startX, startTop, rowHeight)
        val text = mText
        if (text != null && text.isNotEmpty()) {
            val outPaint = mPaint
            if (outPaint == null) {
                val size = paint.textSize
                val color = paint.color
                val typeface = paint.typeface
                val alpha = paint.alpha
                paint.textSize = mTextSize
                paint.color = mTextColor
                paint.alpha = mAlpha
                paint.typeface = mTypeface
                canvas.drawText(
                    text,
                    0,
                    text.length,
                    startX - mPaddingLeft,
                    getTextTopY(text, paint, startTop, rowHeight),
                    paint
                )
                paint.textSize = size
                paint.color = color
                paint.alpha = alpha
                paint.typeface = typeface
            } else {
                canvas.drawText(
                    text,
                    0,
                    text.length,
                    startX - mPaddingLeft,
                    getTextTopY(text, outPaint, startTop, rowHeight),
                    outPaint
                )
            }
        }
    }

    private fun getTextTopY(text: String, paint: Paint, topY: Float, rowHeight: Float): Float {
        return if (mGravity == WatermarkMaker.RowGravity.CENTER) {
            paint.getTextBounds(text, 0, text.length, mRect)
            if (mRect.height() > (mFontMetrics.descent - mFontMetrics.ascent)) {
                topY + rowHeight / 2F - (rowHeight - height) / 2F + (mFontMetrics.bottom - mFontMetrics.top) / 2F - mFontMetrics.bottom
            } else {
                topY + rowHeight / 2F - (rowHeight - height) / 2F + (mFontMetrics.descent - mFontMetrics.ascent) / 2F - mFontMetrics.descent
            }
        } else {
            topY - mFontMetrics.top
        } + mPaddingTop
    }
}
