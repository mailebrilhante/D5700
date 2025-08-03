package org.example.instructions

import org.example.core.CPU

class JumpInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val address = instruction and 0xFFF
        
        if (address % 2 != 0) {
            throw IllegalArgumentException("Jump address must be even: $address")
        }
        
        cpu.setProgramCounter(address)
        
        return false
    }
} 