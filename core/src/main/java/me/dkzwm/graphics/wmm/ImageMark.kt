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
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange

class ImageMark private constructor(
    gravity: WatermarkMaker.RowGravity,
    private var mBitmap: Bitmap?,
    @DrawableRes
    private val mBitmapResId: Int,
    @FloatRange(from = 0.0, to = 1.0)
    private val mScale: Float,
    private val mDegree: Float,
    alpha: Int,
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
    private val mMatrix = Matrix()

    interface IBuilder<T : IBuilder<T>> : Mark.IBuilder<T> {
        fun setScale(scale: Float): T
        fun setDegree(degree: Float): T
    }

    interface IDefBuildBuilder<T : IBuilder<T>> : IBuilder<T> {
        fun build(): ImageMark
    }

    interface ICanBuildBuilder : IDefBuildBuilder<ICanBuildBuilder>

    @Suppress("UNCHECKED_CAST")
    open class Builder<T : IBuilder<T>> protected constructor(
        bitmap: Bitmap?,
        @DrawableRes bitmapResId: Int
    ) : Mark.Builder<T>(), IDefBuildBuilder<T> {

        private var mBitmap: Bitmap? = bitmap

        @DrawableRes
        private var mBitmapResId: Int = bitmapResId

        @FloatRange(from = 0.0, to = 1.0)
        private var mScale: Float = 1F
        private var mDegree: Float = 0F

        override fun setScale(scale: Float): T {
            mScale = scale
            return this as T
        }

        override fun setDegree(degree: Float): T {
            mDegree = degree
            return this as T
        }

        override fun setShadowLayer(radius: Float, dx: Float, dy: Float, shadowColor: Int): T {
            Log.w(javaClass.simpleName, "If the shadow is configured, then you must turn off hardware acceleration to display the shadow normally")
            return super.setShadowLayer(radius, dx, dy, shadowColor)
        }

        override fun build(): ImageMark {
            return ImageMark(
                mGravity, mBitmap, mBitmapResId, mScale, mDegree, mAlpha, mBackgroundColor,
                mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom, mShadowLayerRadius,
                mShadowLayerDx, mShadowLayerDy, mShadowLayerColor, mPaint
            )
        }

        companion object {
            @JvmStatic
            fun create(bitmap: Bitmap): Builder<ICanBuildBuilder> {
                return Builder(bitmap, 0)
            }

            @JvmStatic
            fun create(@DrawableRes bitmapResId: Int): Builder<ICanBuildBuilder> {
                return Builder(null, bitmapResId)
            }
        }
    }

    override fun init(context: Context, paint: Paint, rect: Rect) {
        if (mBitmapResId != 0) {
            mBitmap = BitmapFactory.decodeResource(context.resources, mBitmapResId)
        }
        val bitmap = mBitmap
        if (bitmap != null && !bitmap.isRecycled) {
            mWidth = bitmap.width.toFloat()
            mHeight = bitmap.height.toFloat()
            if (mScale < 1F || mDegree % 360 != 0F) {
                mMatrix.reset()
                mMatrix.postTranslate(bitmap.width / 2F, bitmap.height / 2F)
                mMatrix.postScale(mScale, mScale)
                mMatrix.postRotate(mDegree)
                mMatrix.postTranslate(-bitmap.width / 2F, bitmap.height / 2F)
                mRectF.set(0F, 0F, mWidth, mHeight)
                mMatrix.mapRect(mRectF)
                mWidth = mRectF.width()
                mHeight = mRectF.height()
            }
            mWidth += mPaddingLeft + mPaddingRight
            mHeight += mPaddingTop + mPaddingBottom
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
        val bitmap = mBitmap
        val alpha = paint.alpha
        paint.alpha = mAlpha
        if (bitmap != null) {
            val x = startX - mPaddingLeft
            val y = startTop - mPaddingTop
            if (mScale < 1F || mDegree % 360 != 0F) {
                mMatrix.reset()
                mMatrix.postTranslate(bitmap.width / 2F, bitmap.height / 2F)
                mMatrix.postScale(mScale, mScale)
                mMatrix.postRotate(mDegree)
                mMatrix.postTranslate(-bitmap.width / 2F, bitmap.height / 2F)
                val scaledBitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.width,
                    bitmap.height,
                    mMatrix,
                    true
                )
                canvas.drawBitmap(scaledBitmap, x, y, paint)
                if (!scaledBitmap.isRecycled) {
                    scaledBitmap.recycle()
                }
            } else {
                canvas.drawBitmap(bitmap, x, y, paint)
            }
        }
        paint.alpha = alpha
    }
}
