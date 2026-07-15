package com.novasec.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private var currentInput = ""
    private var operator: String? = null
    private var firstOperand: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.display)

        val digitIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )
        digitIds.forEachIndexed { index, id ->
            findViewById<Button>(id).setOnClickListener { onDigit(index.toString()) }
        }

        findViewById<Button>(R.id.btnDot).setOnClickListener { onDot() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { onClear() }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { onEquals() }
        findViewById<Button>(R.id.btnPlus).setOnClickListener { onOperator("+") }
        findViewById<Button>(R.id.btnMinus).setOnClickListener { onOperator("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { onOperator("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { onOperator("÷") }
    }

    private fun onDigit(digit: String) {
        currentInput += digit
        display.text = currentInput
    }

    private fun onDot() {
        if (!currentInput.contains(".")) {
            currentInput += if (currentInput.isEmpty()) "0." else "."
            display.text = currentInput
        }
    }

    private fun onClear() {
        currentInput = ""
        operator = null
        firstOperand = null
        display.text = "0"
    }

    private fun onOperator(op: String) {
        if (currentInput.isEmpty()) return
        firstOperand = currentInput.toDouble()
        operator = op
        currentInput = ""
    }

    private fun onEquals() {
        if (firstOperand == null || operator == null || currentInput.isEmpty()) return
        val second = currentInput.toDouble()
        val result = when (operator) {
            "+" -> firstOperand!! + second
            "-" -> firstOperand!! - second
            "×" -> firstOperand!! * second
            "÷" -> if (second != 0.0) firstOperand!! / second else Double.NaN
            else -> second
        }
        val resultStr = if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            result.toString()
        }
        display.text = resultStr
        currentInput = resultStr
        operator = null
        firstOperand = null
    }
}
