package org.example.instructions

import org.example.core.CPU

class ConvertToBase10Instruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        
        val value = cpu.getRegister(rX).toInt() and 0xFF
        val address = cpu.getAddress()
        
        val hundreds = value / 100
        val tens = (value % 100) / 10
        val ones = value % 10
        
        val isROM = cpu.getMemoryFlag()
        cpu.getMemory().write(address, hundreds.toByte(), isROM)
        cpu.getMemory().write(address + 1, tens.toByte(), isROM)
        cpu.getMemory().write(address + 2, ones.toByte(), isROM)
        
        return true
    }
} 