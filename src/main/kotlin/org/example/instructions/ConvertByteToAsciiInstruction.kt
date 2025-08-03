package org.example.instructions

import org.example.core.CPU

class ConvertByteToAsciiInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        val rY = (instruction shr 4) and 0xF
        
        val digit = cpu.getRegister(rX).toInt() and 0xFF
        
        if (digit > 0xF) {
            throw IllegalArgumentException("Digit value must be 0-F, got: ${digit.toString(16)}")
        }
        
        val asciiValue = if (digit <= 9) {
            0x30 + digit
        } else {
            0x41 + (digit - 10)
        }
        
        cpu.setRegister(rY, asciiValue.toByte())
        
        return true
    }
} 