package org.example.instructions

import org.example.core.CPU

class SwitchMemoryInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        cpu.setMemoryFlag(!cpu.getMemoryFlag())
        
        return true
    }
} 