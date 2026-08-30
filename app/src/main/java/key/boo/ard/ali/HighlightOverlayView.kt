package key.boo.ard.ali

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class HighlightOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var rect: RectF? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x664285F4.toInt()
        style = Paint.Style.FILL
    }

    fun showAt(x: Int, y: Int, width: Int, height: Int) {
        rect = RectF(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat())
        invalidate()
    }

    fun hide() {
        rect = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rect?.let { canvas.drawRoundRect(it, 12f, 12f, paint) }
    }
}
