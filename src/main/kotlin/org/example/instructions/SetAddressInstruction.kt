package org.example.instructions

import org.example.core.CPU

class SetAddressInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val address = instruction and 0xFFF
        
        cpu.setAddress(address)
        
        return true
    }
} 