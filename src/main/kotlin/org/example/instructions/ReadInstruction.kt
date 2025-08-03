package org.example.instructions

import org.example.core.CPU

class ReadInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        
        val address = cpu.getAddress()
        val isROM = cpu.getMemoryFlag()
        val value = cpu.getMemory().read(address, isROM)
        
        cpu.setRegister(rX, value)
        
        return true
    }
} 