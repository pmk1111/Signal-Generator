package org.mklabs.signal_generator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 초기에 선택되어야 하는 라디오 버튼 설정
        val radioButton1 = findViewById<AppCompatRadioButton>(R.id.radioButton1)
        radioButton1.backgroundTintList = ContextCompat.getColorStateList(this, R.color.selectedColor)

        val checkedRadioButton = findViewById<RadioGroup>(R.id.radioGroup)
        val checkedRadioButtonId = checkedRadioButton.checkedRadioButtonId

        if (checkedRadioButtonId != View.NO_ID) {
            // 선택된 라디오 버튼이 있는 경우
            val selectedRadioButton = findViewById<AppCompatRadioButton>(checkedRadioButtonId)
            updateButtonTextColor(selectedRadioButton, true)
            println("기본값: ${selectedRadioButton.text}")
        } else {
            println("기본값: No radio button selected.")
        }

    }

    fun signalSelect(view: View) {
        // 라디오 버튼 클릭 시 배경색 변경
        val clickedButton = view as AppCompatRadioButton
        clickedButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.selectedColor)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        for (i in 0 until radioGroup.childCount) {
            val child = radioGroup.getChildAt(i)
            if (child is AppCompatRadioButton && child != view) {
                // 선택 안 한 라디오 버튼의 배경색과 글자색 초기화
                child.backgroundTintList = ContextCompat.getColorStateList(this, R.color.unselectedColor)
                updateButtonTextColor(child, false)
            }
        }
        // 선택한 버튼의 글자 색 변경
        updateButtonTextColor(clickedButton, true)

        println("선택한 버튼: ${clickedButton.text}")
    }

    private fun updateButtonTextColor(button: AppCompatRadioButton, isSelected: Boolean) {
        val textColorResId = if (isSelected) R.color.white else R.color.black
        button.setTextColor(ContextCompat.getColorStateList(this, textColorResId))
    }
}
