package org.example.core

class EmulatorFacade {
    private val cpu = CPU.getInstance()
    private val memory = Memory.getInstance()

    fun loadProgram(path: String) {
        memory.loadROM(path)
    }

    fun run() {
        cpu.start()
    }
} 