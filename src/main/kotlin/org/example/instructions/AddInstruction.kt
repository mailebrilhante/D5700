package org.example.instructions

import org.example.core.CPU

class AddInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        val rY = (instruction shr 4) and 0xF
        val rZ = instruction and 0xF
        
        val valueX = cpu.getRegister(rX).toInt() and 0xFF
        val valueY = cpu.getRegister(rY).toInt() and 0xFF
        val result = (valueX + valueY) and 0xFF
        
        cpu.setRegister(rZ, result.toByte())
        
        return true
    }
} 