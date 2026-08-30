package key.boo.ard.ali

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.TextView

class PersianKeyboardService : InputMethodService(), OnKeyboardActionListener {

    private lateinit var keyboardView: GKeyboardView
    private lateinit var highlightOverlay: HighlightOverlayView
    private lateinit var counterText: TextView
    private lateinit var activeKeyboard: Keyboard

    private val handler = Handler(Looper.getMainLooper())
    private var autoTypingRunnable: Runnable? = null

    companion object {
        const val KEYCODE_MACRO_NEXT = -10
        const val KEYCODE_MACRO_RESET = -11
        const val KEYCODE_MACRO_SETTINGS = -12
    }

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_view, null)
        keyboardView = root.findViewById(R.id.keyboard_view)
        highlightOverlay = root.findViewById(R.id.highlight_overlay)
        counterText = root.findViewById(R.id.counter_text)

        loadKeyboardForCurrentSize()
        keyboardView.isPreviewEnabled = false
        keyboardView.setOnKeyboardActionListener(this)

        updateCounterDisplay()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (::keyboardView.isInitialized) {
            loadKeyboardForCurrentSize()
            updateCounterDisplay()
        }
    }

    private fun loadKeyboardForCurrentSize() {
        val size = prefs().getString("keyboard_size", "MEDIUM")
        val resId = when (size) {
            "SMALL" -> R.xml.keyboard_persian_letters_small
            "LARGE" -> R.xml.keyboard_persian_letters_large
            else -> R.xml.keyboard_persian_letters_medium
        }
        activeKeyboard = Keyboard(this, resId)
        keyboardView.keyboard = activeKeyboard
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic: InputConnection = currentInputConnection ?: return
        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> ic.deleteSurroundingText(1, 0)
            Keyboard.KEYCODE_DONE -> sendEnter(ic)
            KEYCODE_MACRO_NEXT -> startAutoTyping(ic)
            KEYCODE_MACRO_RESET -> resetMacro()
            KEYCODE_MACRO_SETTINGS -> openMacroSettings()
            else -> ic.commitText(primaryCode.toChar().toString(), 1)
        }
    }

    private fun prefs() = getSharedPreferences("macro_prefs", MODE_PRIVATE)

    private fun getItems(): List<String> {
        val raw = prefs().getString("macro_items", "") ?: ""
        return raw.split("\n").filter { it.isNotBlank() }
    }

    private fun isCharMode(): Boolean = prefs().getString("auto_mode", "FULL") == "CHAR"
    private fun typingSpeedMs(): Long = prefs().getInt("auto_speed_ms", 45).toLong().coerceAtLeast(10)

    private fun startAutoTyping(ic: InputConnection) {
        val items = getItems()
        if (items.isEmpty() || autoTypingRunnable != null) return

        val p = prefs()
        val lineIndex = p.getInt("macro_line_index", 0) % items.size
        val currentItem = items[lineIndex]

        if (!isCharMode()) {
            ic.commitText(currentItem, 1)
            sendEnter(ic)
            p.edit().putInt("macro_line_index", lineIndex + 1).apply()
            updateCounterDisplay()
            return
        }

        var charIndex = 0
        val runnable = object : Runnable {
            override fun run() {
                if (charIndex < currentItem.length) {
                    val ch = currentItem[charIndex]
                    ic.commitText(ch.toString(), 1)
                    highlightKeyFor(ch)
                    charIndex++
                    handler.postDelayed(this, typingSpeedMs())
                } else {
                    sendEnter(ic)
                    p.edit().putInt("macro_line_index", lineIndex + 1).apply()
                    updateCounterDisplay()
                    autoTypingRunnable = null
                }
            }
        }
        autoTypingRunnable = runnable
        handler.post(runnable)
    }

    private fun highlightKeyFor(ch: Char) {
        val code = ch.code
        val key = activeKeyboard.keys.firstOrNull { it.codes.isNotEmpty() && it.codes[0] == code }
        if (key != null) {
            highlightOverlay.showAt(key.x, key.y, key.width, key.height)
            handler.postDelayed({ highlightOverlay.hide() }, typingSpeedMs().coerceAtMost(200))
        }
    }

    private fun resetMacro() {
        autoTypingRunnable?.let { handler.removeCallbacks(it) }
        autoTypingRunnable = null
        prefs().edit().putInt("macro_line_index", 0).apply()
        updateCounterDisplay()
    }

    private fun openMacroSettings() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun sendEnter(ic: InputConnection) {
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    private fun updateCounterDisplay() {
        val items = getItems()
        if (items.isEmpty()) {
            counterText.text = ""
            return
        }
        val lineIndex = prefs().getInt("macro_line_index", 0) % items.size
        counterText.text = "پیشرفت: ${lineIndex + 1}/${items.size}"
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
