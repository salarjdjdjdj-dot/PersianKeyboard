package key.boo.ard.ali

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet

class GKeyboardView(context: Context, attrs: AttributeSet?) : KeyboardView(context, attrs) {

    private val enterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4285F4.toInt()
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val kb = keyboard ?: return
        for (key in kb.keys) {
            if (key.codes.isNotEmpty() && key.codes[0] == Keyboard.KEYCODE_DONE) {
                val rect = RectF(
                    key.x.toFloat(),
                    key.y.toFloat(),
                    (key.x + key.width).toFloat(),
                    (key.y + key.height).toFloat()
                )
                canvas.drawRoundRect(rect, 14f, 14f, enterPaint)
                key.icon?.let { icon ->
                    val iw = icon.intrinsicWidth
                    val ih = icon.intrinsicHeight
                    val left = key.x + (key.width - iw) / 2
                    val top = key.y + (key.height - ih) / 2
                    icon.setBounds(left, top, left + iw, top + ih)
                    icon.draw(canvas)
                }
            }
        }
    }
}
