package key.boo.ard.ali

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection

class PersianKeyboardService : InputMethodService(), OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var lettersKeyboard: Keyboard

    companion object {
        const val KEYCODE_MACRO_NEXT = -10
        const val KEYCODE_MACRO_RESET = -11
        const val KEYCODE_MACRO_SETTINGS = -12
    }

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView

        lettersKeyboard = Keyboard(this, R.xml.keyboard_persian_letters)

        keyboardView.isPreviewEnabled = false
        keyboardView.keyboard = lettersKeyboard
        keyboardView.setOnKeyboardActionListener(this)

        return keyboardView
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic: InputConnection = currentInputConnection ?: return

        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> {
                ic.deleteSurroundingText(1, 0)
            }
            Keyboard.KEYCODE_DONE -> {
                sendEnter(ic)
            }
            KEYCODE_MACRO_NEXT -> {
                typeNextMacroItem(ic)
            }
            KEYCODE_MACRO_RESET -> {
                resetMacroIndex()
            }
            KEYCODE_MACRO_SETTINGS -> {
                openMacroSettings()
            }
            else -> {
                val code = primaryCode.toChar()
                ic.commitText(code.toString(), 1)
            }
        }
    }

    private fun typeNextMacroItem(ic: InputConnection) {
        val prefs = getSharedPreferences("macro_prefs", MODE_PRIVATE)
        val rawText = prefs.getString("macro_items", "") ?: ""
        val items = rawText.split("\n").filter { it.isNotBlank() }

        if (items.isEmpty()) return

        val index = prefs.getInt("macro_index", 0)
        val safeIndex = index % items.size
        val currentItem = items[safeIndex]

        ic.commitText(currentItem, 1)
        sendEnter(ic)

        prefs.edit().putInt("macro_index", safeIndex + 1).apply()
    }

    private fun resetMacroIndex() {
        val prefs = getSharedPreferences("macro_prefs", MODE_PRIVATE)
        prefs.edit().putInt("macro_index", 0).apply()
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

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
