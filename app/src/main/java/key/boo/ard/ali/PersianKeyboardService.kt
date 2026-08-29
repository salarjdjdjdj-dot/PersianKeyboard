package key.boo.ard.ali

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.TextView

class PersianKeyboardService : InputMethodService(), OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var counterText: TextView
    private lateinit var lettersKeyboard: Keyboard

    companion object {
        const val KEYCODE_MACRO_NEXT = -10
        const val KEYCODE_MACRO_RESET = -11
        const val KEYCODE_MACRO_SETTINGS = -12
    }

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_view, null)
        keyboardView = root.findViewById(R.id.keyboard_view)
        counterText = root.findViewById(R.id.counter_text)

        lettersKeyboard = Keyboard(this, R.xml.keyboard_persian_letters)

        keyboardView.isPreviewEnabled = false
        keyboardView.keyboard = lettersKeyboard
        keyboardView.setOnKeyboardActionListener(this)

        updateCounterDisplay()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (::counterText.isInitialized) updateCounterDisplay()
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic: InputConnection = currentInputConnection ?: return

        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> ic.deleteSurroundingText(1, 0)
            Keyboard.KEYCODE_DONE -> sendEnter(ic)
            KEYCODE_MACRO_NEXT -> runAutoStep(ic)
            KEYCODE_MACRO_RESET -> resetMacro()
            KEYCODE_MACRO_SETTINGS -> openMacroSettings()
            else -> ic.commitText(primaryCode.toChar().toString(), 1)
        }

        updateCounterDisplay()
    }

    private fun prefs() = getSharedPreferences("macro_prefs", MODE_PRIVATE)

    private fun getItems(): List<String> {
        val raw = prefs().getString("macro_items", "") ?: ""
        return raw.split("\n").filter { it.isNotBlank() }
    }

    private fun isCharMode(): Boolean = prefs().getString("auto_mode", "FULL") == "CHAR"

    private fun runAutoStep(ic: InputConnection) {
        val items = getItems()
        if (items.isEmpty()) return

        val p = prefs()
        val lineIndex = p.getInt("macro_line_index", 0) % items.size
        val currentItem = items[lineIndex]

        if (isCharMode()) {
            val charIndex = p.getInt("macro_char_index", 0)
            if (charIndex < currentItem.length) {
                ic.commitText(currentItem[charIndex].toString(), 1)
                p.edit().putInt("macro_char_index", charIndex + 1).apply()
            }
            if (charIndex + 1 >= currentItem.length) {
                sendEnter(ic)
                p.edit()
                    .putInt("macro_char_index", 0)
                    .putInt("macro_line_index", lineIndex + 1)
                    .apply()
            }
        } else {
            ic.commitText(currentItem, 1)
            sendEnter(ic)
            p.edit().putInt("macro_line_index", lineIndex + 1).apply()
        }
    }

    private fun resetMacro() {
        prefs().edit()
            .putInt("macro_line_index", 0)
            .putInt("macro_char_index", 0)
            .apply()
    }

    private fun openMacroSettings() {
        val intent = Intent(this, MacroSettingsActivity::class.java)
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
        val p = prefs()
        val lineIndex = p.getInt("macro_line_index", 0) % items.size
        val total = items.size
        counterText.text = if (isCharMode()) {
            val charIndex = p.getInt("macro_char_index", 0)
            "پیشرفت: ${lineIndex + 1}/$total · حرف $charIndex/${items[lineIndex].length}"
        } else {
            "پیشرفت: ${lineIndex + 1}/$total"
        }
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
