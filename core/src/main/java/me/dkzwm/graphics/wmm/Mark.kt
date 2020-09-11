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
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt

abstract class Mark protected constructor(
    @JvmField
    protected val mGravity: WatermarkMaker.RowGravity,
    protected val mAlpha: Int,
    @ColorInt private val mBackgroundColor: Int,
    protected var mPaddingLeft: Float,
    protected var mPaddingTop: Float,
    protected var mPaddingRight: Float,
    protected var mPaddingBottom: Float,
    protected val mShadowLayerRadius: Float,
    protected val mShadowLayerDx: Float,
    protected val mShadowLayerDy: Float,
    @ColorInt
    protected val mShadowLayerColor: Int,
    protected val mPaint: Paint?,
) {
    @JvmField
    protected var mHeight = 0F

    @JvmField
    protected var mWidth = 0F
    val height: Float
        get() = mHeight
    val width: Float
        get() = mWidth

    val gravity: WatermarkMaker.RowGravity
        get() = mGravity

    val isValid: Boolean
        get() = mHeight > 0F && mWidth > 0F

    @JvmField
    protected var mBaselineOffset = 0F

    val baselineOffset: Float
        get() = mBaselineOffset

    protected val mRectF = RectF()

    abstract fun init(context: Context, paint: Paint, rect: Rect)

    @CallSuper
    open fun draw(
        canvas: Canvas,
        paint: Paint,
        startX: Float,
        startTop: Float,
        rowHeight: Float
    ) {
        val outPaint = mPaint
        if (outPaint == null) {
            paint.setShadowLayer(
                mShadowLayerRadius,
                mShadowLayerDx,
                mShadowLayerDy,
                mShadowLayerColor
            )
            if (mBackgroundColor != Color.TRANSPARENT) {
                mRectF.set(startX, startTop, startX + mWidth, startTop + mHeight)
                val paintColor = paint.color
                paint.color = mBackgroundColor
                canvas.drawRect(mRectF, paint)
                paint.color = paintColor
            }
        } else {
            mRectF.set(startX, startTop, startX + mWidth, startTop + mHeight)
            canvas.drawRect(mRectF, outPaint)
        }
    }

    fun clearShadowLayer(textPaint: Paint, imgPaint: Paint) {
        textPaint.clearShadowLayer()
        imgPaint.clearShadowLayer()
    }

    interface IBuilder<T : IBuilder<T>> {
        fun setAlpha(alpha: Int): T
        fun setPadding(left: Float, top: Float, right: Float, bottom: Float): T
        fun setBackgroundColor(@ColorInt color: Int): T
        fun setRowGravity(gravity: WatermarkMaker.RowGravity): T
        fun setPaint(paint: Paint): T
        fun setShadowLayer(
            radius: Float,
            dx: Float,
            dy: Float,
            @ColorInt shadowColor: Int
        ): T
    }

    @Suppress("UNCHECKED_CAST")
    open class Builder<T : IBuilder<T>> : IBuilder<T> {
        @ColorInt
        protected var mBackgroundColor = Color.TRANSPARENT
        protected var mGravity = WatermarkMaker.RowGravity.CENTER

        protected var mShadowLayerRadius = 0F
        protected var mShadowLayerDx = 0F
        protected var mShadowLayerDy = 0F

        @ColorInt
        protected var mShadowLayerColor = Color.TRANSPARENT
        protected var mAlpha = 255
        protected var mPaddingLeft = 0F
        protected var mPaddingTop = 0F
        protected var mPaddingRight = 0F
        protected var mPaddingBottom = 0F
        protected var mPaint: Paint? = null

        override fun setAlpha(alpha: Int): T {
            mAlpha = alpha
            return this as T
        }

        override fun setPadding(left: Float, top: Float, right: Float, bottom: Float): T {
            mPaddingLeft = left
            mPaddingTop = top
            mPaddingRight = right
            mPaddingBottom = bottom
            return this as T
        }

        override fun setBackgroundColor(@ColorInt color: Int): T {
            mBackgroundColor = color
            return this as T
        }

        override fun setRowGravity(gravity: WatermarkMaker.RowGravity): T {
            mGravity = gravity
            return this as T
        }

        override fun setPaint(paint: Paint): T {
            mPaint = paint
            return this as T
        }

        override fun setShadowLayer(
            radius: Float,
            dx: Float,
            dy: Float,
            @ColorInt shadowColor: Int
        ): T {
            mShadowLayerRadius = radius
            mShadowLayerDx = dx
            mShadowLayerDy = dy
            mShadowLayerColor = shadowColor
            return this as T
        }
    }
}
