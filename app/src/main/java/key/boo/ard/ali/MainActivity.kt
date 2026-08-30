package key.boo.ard.ali

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var editText: EditText
    private lateinit var modeGroup: RadioGroup
    private lateinit var sizeGroup: RadioGroup
    private lateinit var speedSeekBar: SeekBar
    private lateinit var speedLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("macro_prefs", MODE_PRIVATE)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(40, 40, 40, 40)

        val title = TextView(this)
        title.text = "تنظیمات کیبورد فارسی"
        title.textSize = 18f
        title.setPadding(0, 0, 0, 24)

        val itemsLabel = TextView(this)
        itemsLabel.text = "متن‌های آماده (هر خط یک آیتم):"

        editText = EditText(this)
        editText.setText(prefs.getString("macro_items", "1\n2\n3\n4\n5\n6\n7\n8\n9\n0"))
        editText.setLines(8)
        editText.gravity = Gravity.TOP

        val modeLabel = TextView(this)
        modeLabel.text = "حالت تایپ خودکار:"
        modeLabel.setPadding(0, 24, 0, 0)

        modeGroup = RadioGroup(this)
        modeGroup.orientation = RadioGroup.HORIZONTAL
        val fullRadio = RadioButton(this).apply { id = 1001; text = "کامل" }
        val charRadio = RadioButton(this).apply { id = 1002; text = "حرف به حرف" }
        modeGroup.addView(fullRadio)
        modeGroup.addView(charRadio)
        modeGroup.check(if (prefs.getString("auto_mode", "FULL") == "CHAR") 1002 else 1001)

        speedLabel = TextView(this)
        speedLabel.setPadding(0, 24, 0, 0)

        speedSeekBar = SeekBar(this)
        speedSeekBar.max = 300
        val currentSpeed = prefs.getInt("auto_speed_ms", 45)
        speedSeekBar.progress = currentSpeed
        updateSpeedLabel(currentSpeed)
        speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateSpeedLabel(progress.coerceAtLeast(10))
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val sizeLabel = TextView(this)
        sizeLabel.text = "اندازه کیبورد:"
        sizeLabel.setPadding(0, 24, 0, 0)

        sizeGroup = RadioGroup(this)
        sizeGroup.orientation = RadioGroup.HORIZONTAL
        val smallRadio = RadioButton(this).apply { id = 2001; text = "کوچک" }
        val mediumRadio = RadioButton(this).apply { id = 2002; text = "متوسط" }
        val largeRadio = RadioButton(this).apply { id = 2003; text = "بزرگ" }
        sizeGroup.addView(smallRadio)
        sizeGroup.addView(mediumRadio)
        sizeGroup.addView(largeRadio)
        sizeGroup.check(
            when (prefs.getString("keyboard_size", "MEDIUM")) {
                "SMALL" -> 2001
                "LARGE" -> 2003
                else -> 2002
            }
        )

        val saveButton = Button(this)
        saveButton.text = "ذخیره تنظیمات"
        saveButton.setPadding(0, 24, 0, 0)
        saveButton.setOnClickListener {
            val mode = if (modeGroup.checkedRadioButtonId == 1002) "CHAR" else "FULL"
            val size = when (sizeGroup.checkedRadioButtonId) {
                2001 -> "SMALL"
                2003 -> "LARGE"
                else -> "MEDIUM"
            }
            prefs.edit()
                .putString("macro_items", editText.text.toString())
                .putString("auto_mode", mode)
                .putInt("auto_speed_ms", speedSeekBar.progress.coerceAtLeast(10))
                .putString("keyboard_size", size)
                .putInt("macro_line_index", 0)
                .apply()
        }

        val openSettingsButton = Button(this)
        openSettingsButton.text = "فعال‌سازی کیبورد در تنظیمات گوشی"
        openSettingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        root.addView(title)
        root.addView(itemsLabel)
        root.addView(editText)
        root.addView(modeLabel)
        root.addView(modeGroup)
        root.addView(speedLabel)
        root.addView(speedSeekBar)
        root.addView(sizeLabel)
        root.addView(sizeGroup)
        root.addView(saveButton)
        root.addView(openSettingsButton)

        setContentView(root)
    }

    private fun updateSpeedLabel(ms: Int) {
        speedLabel.text = "سرعت تایپ: ${ms.coerceAtLeast(10)} میلی‌ثانیه بین هر حرف"
    }
}
