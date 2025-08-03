package org.example.instructions

import org.example.core.CPU

class ReadKeyboardInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        
        val value = cpu.waitForKeyboardInput()
        cpu.setRegister(rX, value)
        
        return true
    }
} 