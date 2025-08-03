package org.example.core

import org.example.instructions.InstructionFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class CPU private constructor() {
    companion object {
        private val instance = CPU()
        fun getInstance() = instance
    }

    private val registers = ByteArray(8) { 0 }
    
    private var pc: Int = 0
    private var timer: Byte = 0
    private var address: Int = 0
    private var memoryFlag: Boolean = false
    
    private var running = false
    private var executor: ScheduledExecutorService? = null
    private var cpuFuture: ScheduledFuture<*>? = null
    
    private val memory = Memory.getInstance()
    private val screen = Screen.getInstance()
    private val timerComponent = Timer.getInstance()
    private val instructionFactory = InstructionFactory()

    fun start() {
        println("CPU started at 500Hz.")
        running = true
        pc = 0
        
        if (cpuFuture != null && !cpuFuture!!.isCancelled) {
            cpuFuture!!.cancel(true)
            cpuFuture = null
        }
        
        if (executor?.isShutdown != false) {
            executor = Executors.newSingleThreadScheduledExecutor()
        }
        
        timerComponent.start()
        
        val cpuRunnable = Runnable {
            if (running) {
                execute()
            }
        }
        
        cpuFuture = executor?.scheduleAtFixedRate(
            cpuRunnable,
            0,
            1000L / 500L,
            TimeUnit.MILLISECONDS
        )
    }
    
    fun execute() {
        try {
            if (pc % 2 != 0) {
                println("Error: Program counter must be even. PC = $pc")
                stop()
                return
            }
            
            val byte1 = memory.readROM(pc)
            val byte2 = memory.readROM(pc + 1)
            val instruction = ((byte1.toInt() and 0xFF) shl 8) or (byte2.toInt() and 0xFF)
            
            if (instruction == 0x0000) {
                println("Program terminated (halt instruction 0000)")
                stop()
                return
            }
            
            val instructionObj = instructionFactory.createInstruction(instruction)
            val shouldIncrementPC = instructionObj.execute(this)
            
            if (shouldIncrementPC) {
                pc += 2
            }
            
        } catch (e: Exception) {
            println("CPU Error: ${e.message}")
            e.printStackTrace()
            stop()
        }
    }
    
    fun stop() {
        running = false
        cpuFuture?.cancel(true)
        cpuFuture = null
        timerComponent.stop()
        executor?.shutdown()
        executor = null
    }
    
    fun getRegister(index: Int): Byte {
        if (index < 0 || index >= 8) {
            throw IndexOutOfBoundsException("Register index out of bounds: $index")
        }
        return registers[index]
    }
    
    fun setRegister(index: Int, value: Byte) {
        if (index < 0 || index >= 8) {
            throw IndexOutOfBoundsException("Register index out of bounds: $index")
        }
        registers[index] = value
    }
    
    fun getProgramCounter(): Int = pc
    fun setProgramCounter(value: Int) { pc = value }
    
    fun getTimer(): Byte = timer
    fun setTimer(value: Byte) { 
        timer = value
        timerComponent.setCounter(value.toInt() and 0xFF)
    }
    
    fun getAddress(): Int = address
    fun setAddress(value: Int) { address = value }
    
    fun getMemoryFlag(): Boolean = memoryFlag
    fun setMemoryFlag(value: Boolean) { memoryFlag = value }
    
    fun getMemory(): Memory = memory
    fun getScreen(): Screen = screen
    fun getTimerComponent(): Timer = timerComponent
    
    fun waitForKeyboardInput(): Byte {
        print("Enter hex value (0-F): ")
        val input = readlnOrNull()?.trim()?.lowercase() ?: ""
        
        if (input.isEmpty()) {
            return 0
        }
        
        return try {
            val value = input.take(2).toInt(16)
            (value and 0xFF).toByte()
        } catch (e: NumberFormatException) {
            println("Invalid input, using 0")
            0
        }
    }
} 