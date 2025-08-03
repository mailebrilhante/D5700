package org.example.instructions

import org.example.core.CPU

class ReadTimerInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        
        val timerValue = cpu.getTimerComponent().getCounter().toByte()
        cpu.setRegister(rX, timerValue)
        
        return true
    }
} 