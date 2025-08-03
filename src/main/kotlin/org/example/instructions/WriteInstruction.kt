package org.example.instructions

import org.example.core.CPU

class WriteInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        
        val address = cpu.getAddress()
        val value = cpu.getRegister(rX)
        val isROM = cpu.getMemoryFlag()
        
        cpu.getMemory().write(address, value, isROM)
        
        return true
    }
} 