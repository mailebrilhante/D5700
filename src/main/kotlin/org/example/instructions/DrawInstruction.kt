package org.example.instructions

import org.example.core.CPU

class DrawInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        val row = (instruction shr 4) and 0xF
        val col = instruction and 0xF
        
        val asciiValue = cpu.getRegister(rX).toInt() and 0xFF
        
        if (asciiValue > 0x7F) {
            throw IllegalArgumentException("ASCII value must be 0-7F, got: ${asciiValue.toString(16)}")
        }
        
        if (row > 7 || col > 7) {
            throw IllegalArgumentException("Row and column must be 0-7, got: row=$row, col=$col")
        }
        
        val character = asciiValue.toChar()
        cpu.getScreen().update(col, row, character)
        cpu.getScreen().draw()
        
        return true
    }
} 