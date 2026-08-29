package key.boo.ard.ali

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class MacroSettingsActivity : Activity() {

    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("macro_prefs", MODE_PRIVATE)
        val savedText = prefs.getString("macro_items", "1\n2\n3\n4\n5\n6\n7\n8\n9\n0")

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(32, 32, 32, 32)

        editText = EditText(this)
        editText.setText(savedText)
        editText.setLines(12)
        editText.gravity = android.view.Gravity.TOP

        val saveButton = Button(this)
        saveButton.text = "ذخیره"
        saveButton.setOnClickListener {
            val text = editText.text.toString()
            prefs.edit()
                .putString("macro_items", text)
                .putInt("macro_index", 0)
                .apply()
            Toast.makeText(this, "ذخیره شد", Toast.LENGTH_SHORT).show()
            finish()
        }

        root.addView(editText)
        root.addView(saveButton)
        setContentView(root)
    }
}
