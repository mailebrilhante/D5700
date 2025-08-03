package org.example.instructions

import org.example.core.CPU

class StoreInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        val value = instruction and 0xFF
        
        cpu.setRegister(rX, value.toByte())
        
        return true
    }
}

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

class SubInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        val rY = (instruction shr 4) and 0xF
        val rZ = instruction and 0xF
        
        val valueX = cpu.getRegister(rX).toInt() and 0xFF
        val valueY = cpu.getRegister(rY).toInt() and 0xFF
        val result = (valueX - valueY) and 0xFF
        
        cpu.setRegister(rZ, result.toByte())
        
        return true
    }
}

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

class ReadKeyboardInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        
        val value = cpu.waitForKeyboardInput()
        cpu.setRegister(rX, value)
        
        return true
    }
}

class SwitchMemoryInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        cpu.setMemoryFlag(!cpu.getMemoryFlag())
        
        return true
    }
}

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

class SkipNotEqualInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        val rY = (instruction shr 4) and 0xF
        
        val valueX = cpu.getRegister(rX)
        val valueY = cpu.getRegister(rY)
        
        if (valueX != valueY) {
            cpu.setProgramCounter(cpu.getProgramCounter() + 4)
            return false
        }
        
        return true
    }
}

class SetAddressInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val address = instruction and 0xFFF
        
        cpu.setAddress(address)
        
        return true
    }
}

class SetTimerInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val value = (instruction shr 4) and 0xFF
        
        cpu.setTimer(value.toByte())
        
        return true
    }
}

class ReadTimerInstruction(private val instruction: Int) : Instruction() {
    override fun execute(cpu: CPU): Boolean {
        val rX = (instruction shr 8) and 0xF
        
        val timerValue = cpu.getTimerComponent().getCounter().toByte()
        cpu.setRegister(rX, timerValue)
        
        return true
    }
} 