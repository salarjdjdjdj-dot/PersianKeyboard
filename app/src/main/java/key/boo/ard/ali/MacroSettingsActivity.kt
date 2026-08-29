package key.boo.ard.ali

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MacroSettingsActivity : Activity() {

    private lateinit var editText: EditText
    private lateinit var modeLabel: TextView
    private var selectedMode = "FULL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("macro_prefs", MODE_PRIVATE)
        val savedText = prefs.getString("macro_items", "1\n2\n3\n4\n5\n6\n7\n8\n9\n0")
        selectedMode = prefs.getString("auto_mode", "FULL") ?: "FULL"

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(32, 32, 32, 32)

        editText = EditText(this)
        editText.setText(savedText)
        editText.setLines(10)
        editText.gravity = Gravity.TOP

        modeLabel = TextView(this)
        updateModeLabel()

        val modeRow = LinearLayout(this)
        modeRow.orientation = LinearLayout.HORIZONTAL

        val fullButton = Button(this)
        fullButton.text = "حالت: کامل"
        fullButton.setOnClickListener {
            selectedMode = "FULL"
            updateModeLabel()
        }

        val charButton = Button(this)
        charButton.text = "حالت: حرف به حرف"
        charButton.setOnClickListener {
            selectedMode = "CHAR"
            updateModeLabel()
        }

        modeRow.addView(fullButton)
        modeRow.addView(charButton)

        val saveButton = Button(this)
        saveButton.text = "ذخیره"
        saveButton.setOnClickListener {
            val text = editText.text.toString()
            prefs.edit()
                .putString("macro_items", text)
                .putString("auto_mode", selectedMode)
                .putInt("macro_line_index", 0)
                .putInt("macro_char_index", 0)
                .apply()
            Toast.makeText(this, "ذخیره شد", Toast.LENGTH_SHORT).show()
            finish()
        }

        root.addView(editText)
        root.addView(modeLabel)
        root.addView(modeRow)
        root.addView(saveButton)
        setContentView(root)
    }

    private fun updateModeLabel() {
        modeLabel.text = "حالت فعلی: " + if (selectedMode == "CHAR") "حرف به حرف" else "کامل"
    }
}
