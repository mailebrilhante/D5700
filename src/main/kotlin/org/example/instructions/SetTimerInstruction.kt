package org.example.instructions

import org.example.core.CPU

class SetTimerInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val value = (instruction shr 4) and 0xFF
        
        cpu.setTimer(value.toByte())
        
        return true
    }
} 