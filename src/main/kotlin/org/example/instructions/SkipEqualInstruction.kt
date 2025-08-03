package org.example.instructions

import org.example.core.CPU

class SkipEqualInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        val rY = (instruction shr 4) and 0xF
        
        val valueX = cpu.getRegister(rX)
        val valueY = cpu.getRegister(rY)
        
        if (valueX == valueY) {
            cpu.setProgramCounter(cpu.getProgramCounter() + 4)
            return false
        }
        
        return true
    }
} 