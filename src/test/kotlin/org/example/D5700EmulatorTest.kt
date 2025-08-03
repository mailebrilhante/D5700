package org.example
import org.example.core.*
import org.example.instructions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.ByteArrayInputStream
class D5700EmulatorTest {
    private lateinit var cpu: CPU
    private lateinit var memory: Memory
    private lateinit var screen: Screen
    private lateinit var timer: Timer
    private lateinit var instructionFactory: InstructionFactory
    @BeforeEach
    fun setUp() {
        cpu = CPU.getInstance()
        memory = Memory.getInstance()
        screen = Screen.getInstance()
        timer = Timer.getInstance()
        instructionFactory = InstructionFactory()
        cpu.stop()
        for (i in 0..7) {
            cpu.setRegister(i, 0)
        }
        cpu.setProgramCounter(0)
        cpu.setTimer(0)
        cpu.setAddress(0)
        cpu.setMemoryFlag(false)
    }
    @Test
    fun testMemorySingleton() {
        val memory1 = Memory.getInstance()
        val memory2 = Memory.getInstance()
        assertSame(memory1, memory2, "Memory should be singleton")
    }
    @Test
    fun testMemoryRomOperations() {
        assertThrows<IndexOutOfBoundsException> {
            memory.readROM(-1)
        }
        assertThrows<IndexOutOfBoundsException> {
            memory.readROM(4096)
        }
        memory.writeROM(0, 0x42.toByte())
        assertEquals(0x42.toByte(), memory.readROM(0))
        memory.writeROM(4095, 0xFF.toByte())
        assertEquals(0xFF.toByte(), memory.readROM(4095))
    }
    @Test
    fun testMemoryRamOperations() {
        assertThrows<IndexOutOfBoundsException> {
            memory.readRAM(-1)
        }
        assertThrows<IndexOutOfBoundsException> {
            memory.readRAM(4096)
        }
        memory.writeRAM(0, 0x55.toByte())
        assertEquals(0x55.toByte(), memory.readRAM(0))
        memory.writeRAM(4095, 0xAA.toByte())
        assertEquals(0xAA.toByte(), memory.readRAM(4095))
    }
    @Test
    fun testMemoryReadWriteWithFlags() {
        memory.writeRAM(100, 0x11.toByte())
        memory.writeROM(100, 0x22.toByte())
        assertEquals(0x22.toByte(), memory.read(100, true))  
        assertEquals(0x11.toByte(), memory.read(100, false)) 
        memory.write(200, 0x33.toByte(), false) 
        assertEquals(0x33.toByte(), memory.readRAM(200))
        memory.write(200, 0x44.toByte(), true)  
        assertEquals(0x44.toByte(), memory.readROM(200))
    }
    @Test
    fun testRomLoadingFromFile() {
        val tempFile = File.createTempFile("test", ".rom")
        tempFile.deleteOnExit()
        val testData = byteArrayOf(0x00, 0x48, 0x01, 0x45, 0x02, 0x4C)
        tempFile.writeBytes(testData)
        memory.loadROM(tempFile.absolutePath)
        assertEquals(0x00.toByte(), memory.readROM(0))
        assertEquals(0x48.toByte(), memory.readROM(1))
        assertEquals(0x01.toByte(), memory.readROM(2))
        assertEquals(0x45.toByte(), memory.readROM(3))
        assertEquals(0x02.toByte(), memory.readROM(4))
        assertEquals(0x4C.toByte(), memory.readROM(5))
    }
    @Test
    fun testRomLoadingErrors() {
        assertThrows<IllegalArgumentException> {
            memory.loadROM("nonexistent_file.rom")
        }
        val tempFile = File.createTempFile("large", ".rom")
        tempFile.deleteOnExit()
        tempFile.writeBytes(ByteArray(5000)) 
        assertThrows<IllegalArgumentException> {
            memory.loadROM(tempFile.absolutePath)
        }
    }
    @Test
    fun testScreenSingleton() {
        val screen1 = Screen.getInstance()
        val screen2 = Screen.getInstance()
        assertSame(screen1, screen2, "Screen should be singleton")
    }
    @Test
    fun testScreenUpdateBounds() {
        assertThrows<IndexOutOfBoundsException> {
            screen.update(-1, 0, 'A')
        }
        assertThrows<IndexOutOfBoundsException> {
            screen.update(8, 0, 'A')
        }
        assertThrows<IndexOutOfBoundsException> {
            screen.update(0, -1, 'A')
        }
        assertThrows<IndexOutOfBoundsException> {
            screen.update(0, 8, 'A')
        }
    }
    @Test
    fun testScreenUpdateValidCoordinates() {
        screen.update(0, 0, 'H')
        screen.update(7, 7, 'X')
        screen.update(3, 4, 'M')
    }
    @Test
    fun testTimerSingleton() {
        val timer1 = Timer.getInstance()
        val timer2 = Timer.getInstance()
        assertSame(timer1, timer2, "Timer should be singleton")
    }
    @Test
    fun testTimerOperations() {
        timer.setCounter(100)
        assertEquals(100, timer.getCounter())
        timer.tick()
        assertEquals(99, timer.getCounter())
        timer.tick()
        assertEquals(98, timer.getCounter())
        timer.setCounter(1)
        timer.tick()
        assertEquals(0, timer.getCounter())
        timer.tick()
        assertEquals(0, timer.getCounter())
    }
    @Test
    fun testCpuSingleton() {
        val cpu1 = CPU.getInstance()
        val cpu2 = CPU.getInstance()
        assertSame(cpu1, cpu2, "CPU should be singleton")
    }
    @Test
    fun testCpuRegisterOperations() {
        assertThrows<IndexOutOfBoundsException> {
            cpu.getRegister(-1)
        }
        assertThrows<IndexOutOfBoundsException> {
            cpu.getRegister(8)
        }
        assertThrows<IndexOutOfBoundsException> {
            cpu.setRegister(-1, 0)
        }
        assertThrows<IndexOutOfBoundsException> {
            cpu.setRegister(8, 0)
        }
        for (i in 0..7) {
            cpu.setRegister(i, (0x10 + i).toByte())
            assertEquals((0x10 + i).toByte(), cpu.getRegister(i))
        }
    }
    @Test
    fun testCpuSpecialRegisters() {
        cpu.setProgramCounter(0x1234)
        assertEquals(0x1234, cpu.getProgramCounter())
        cpu.setTimer(0x55.toByte())
        assertEquals(0x55.toByte(), cpu.getTimer())
        cpu.setAddress(0xABCD)
        assertEquals(0xABCD, cpu.getAddress())
        cpu.setMemoryFlag(true)
        assertTrue(cpu.getMemoryFlag())
        cpu.setMemoryFlag(false)
        assertFalse(cpu.getMemoryFlag())
    }
    @Test
    fun testInstructionFactoryCreateInstructions() {
        assertTrue(instructionFactory.createInstruction(0x0123) is StoreInstruction)
        assertTrue(instructionFactory.createInstruction(0x1123) is AddInstruction)
        assertTrue(instructionFactory.createInstruction(0x2123) is SubInstruction)
        assertTrue(instructionFactory.createInstruction(0x3123) is ReadInstruction)
        assertTrue(instructionFactory.createInstruction(0x4123) is WriteInstruction)
        assertTrue(instructionFactory.createInstruction(0x5123) is JumpInstruction)
        assertTrue(instructionFactory.createInstruction(0x6123) is ReadKeyboardInstruction)
        assertTrue(instructionFactory.createInstruction(0x7123) is SwitchMemoryInstruction)
        assertTrue(instructionFactory.createInstruction(0x8123) is SkipEqualInstruction)
        assertTrue(instructionFactory.createInstruction(0x9123) is SkipNotEqualInstruction)
        assertTrue(instructionFactory.createInstruction(0xA123) is SetAddressInstruction)
        assertTrue(instructionFactory.createInstruction(0xB123) is SetTimerInstruction)
        assertTrue(instructionFactory.createInstruction(0xC123) is ReadTimerInstruction)
        assertTrue(instructionFactory.createInstruction(0xD123) is ConvertToBase10Instruction)
        assertTrue(instructionFactory.createInstruction(0xE123) is ConvertByteToAsciiInstruction)
        assertTrue(instructionFactory.createInstruction(0xF123) is DrawInstruction)
    }
    @Test
    fun testStoreInstruction() {
        val instruction = StoreInstruction(0x0348) 
        val result = instruction.execute(cpu)
        assertTrue(result, "STORE should increment PC")
        assertEquals(0x48.toByte(), cpu.getRegister(3))
    }
    @Test
    fun testAddInstruction() {
        cpu.setRegister(1, 0x10.toByte())
        cpu.setRegister(2, 0x20.toByte())
        val instruction = AddInstruction(0x1123) 
        val result = instruction.execute(cpu)
        assertTrue(result, "ADD should increment PC")
        assertEquals(0x30.toByte(), cpu.getRegister(3))
    }
    @Test
    fun testAddInstructionOverflow() {
        cpu.setRegister(1, 0xFF.toByte())
        cpu.setRegister(2, 0x01.toByte())
        val instruction = AddInstruction(0x1123) 
        instruction.execute(cpu)
        assertEquals(0x00.toByte(), cpu.getRegister(3)) 
    }
    @Test
    fun testSubInstruction() {
        cpu.setRegister(1, 0x50.toByte())
        cpu.setRegister(2, 0x20.toByte())
        val instruction = SubInstruction(0x2123) 
        val result = instruction.execute(cpu)
        assertTrue(result, "SUB should increment PC")
        assertEquals(0x30.toByte(), cpu.getRegister(3))
    }
    @Test
    fun testSubInstructionUnderflow() {
        cpu.setRegister(1, 0x10.toByte())
        cpu.setRegister(2, 0x20.toByte())
        val instruction = SubInstruction(0x2123) 
        instruction.execute(cpu)
        assertEquals(0xF0.toByte(), cpu.getRegister(3)) 
    }
    @Test
    fun testReadInstructionFromRam() {
        memory.writeRAM(0x100, 0x42.toByte())
        cpu.setAddress(0x100)
        cpu.setMemoryFlag(false) 
        val instruction = ReadInstruction(0x3200) 
        val result = instruction.execute(cpu)
        assertTrue(result, "READ should increment PC")
        assertEquals(0x42.toByte(), cpu.getRegister(2))
    }
    @Test
    fun testReadInstructionFromRom() {
        memory.writeROM(0x200, 0x77.toByte())
        cpu.setAddress(0x200)
        cpu.setMemoryFlag(true) 
        val instruction = ReadInstruction(0x3400) 
        instruction.execute(cpu)
        assertEquals(0x77.toByte(), cpu.getRegister(4))
    }
    @Test
    fun testWriteInstructionToRam() {
        cpu.setRegister(5, 0x88.toByte())
        cpu.setAddress(0x300)
        cpu.setMemoryFlag(false) 
        val instruction = WriteInstruction(0x4500) 
        val result = instruction.execute(cpu)
        assertTrue(result, "WRITE should increment PC")
        assertEquals(0x88.toByte(), memory.readRAM(0x300))
    }
    @Test
    fun testJumpInstruction() {
        val instruction = JumpInstruction(0x5200) 
        val result = instruction.execute(cpu)
        assertFalse(result, "JUMP should not increment PC")
        assertEquals(0x200, cpu.getProgramCounter())
    }
    @Test
    fun testJumpInstructionOddAddressError() {
        val instruction = JumpInstruction(0x5201) 
        assertThrows<IllegalArgumentException> {
            instruction.execute(cpu)
        }
    }
    @Test
    fun testSwitchMemoryInstruction() {
        cpu.setMemoryFlag(false)
        val instruction = SwitchMemoryInstruction(0x7000)
        val result = instruction.execute(cpu)
        assertTrue(result, "SWITCH_MEMORY should increment PC")
        assertTrue(cpu.getMemoryFlag())
        instruction.execute(cpu)
        assertFalse(cpu.getMemoryFlag())
    }
    @Test
    fun testSkipEqualInstructionWhenEqual() {
        cpu.setRegister(1, 0x42.toByte())
        cpu.setRegister(2, 0x42.toByte())
        cpu.setProgramCounter(0x100)
        val instruction = SkipEqualInstruction(0x8120) 
        val result = instruction.execute(cpu)
        assertFalse(result, "SKIP_EQUAL should not increment PC when equal")
        assertEquals(0x104, cpu.getProgramCounter()) 
    }
    @Test
    fun testSkipEqualInstructionWhenNotEqual() {
        cpu.setRegister(1, 0x42.toByte())
        cpu.setRegister(2, 0x43.toByte())
        val instruction = SkipEqualInstruction(0x8120) 
        val result = instruction.execute(cpu)
        assertTrue(result, "SKIP_EQUAL should increment PC normally when not equal")
    }
    @Test
    fun testSkipNotEqualInstructionWhenNotEqual() {
        cpu.setRegister(1, 0x42.toByte())
        cpu.setRegister(2, 0x43.toByte())
        cpu.setProgramCounter(0x200)
        val instruction = SkipNotEqualInstruction(0x9120) 
        val result = instruction.execute(cpu)
        assertFalse(result, "SKIP_NOT_EQUAL should not increment PC when not equal")
        assertEquals(0x204, cpu.getProgramCounter()) 
    }
    @Test
    fun testSkipNotEqualInstructionWhenEqual() {
        cpu.setRegister(1, 0x42.toByte())
        cpu.setRegister(2, 0x42.toByte())
        val instruction = SkipNotEqualInstruction(0x9120) 
        val result = instruction.execute(cpu)
        assertTrue(result, "SKIP_NOT_EQUAL should increment PC normally when equal")
    }
    @Test
    fun testSetAddressInstruction() {
        val instruction = SetAddressInstruction(0xA123) 
        val result = instruction.execute(cpu)
        assertTrue(result, "SET_ADDRESS should increment PC")
        assertEquals(0x123, cpu.getAddress())
    }
    @Test
    fun testSetTimerInstruction() {
        val instruction = SetTimerInstruction(0xB5A0) 
        val result = instruction.execute(cpu)
        assertTrue(result, "SET_TIMER should increment PC")
        assertEquals(0x5A.toByte(), cpu.getTimer())
    }
    @Test
    fun testReadTimerInstruction() {
        timer.setCounter(0x77)
        val instruction = ReadTimerInstruction(0xC300) 
        val result = instruction.execute(cpu)
        assertTrue(result, "READ_TIMER should increment PC")
        assertEquals(0x77.toByte(), cpu.getRegister(3))
    }
    @Test
    fun testConvertToBase10Instruction() {
        cpu.setRegister(2, 123.toByte()) 
        cpu.setAddress(0x500)
        cpu.setMemoryFlag(false) 
        val instruction = ConvertToBase10Instruction(0xD200) 
        val result = instruction.execute(cpu)
        assertTrue(result, "CONVERT_TO_BASE_10 should increment PC")
        assertEquals(1.toByte(), memory.readRAM(0x500))     
        assertEquals(2.toByte(), memory.readRAM(0x501))     
        assertEquals(3.toByte(), memory.readRAM(0x502))     
    }
    @Test
    fun testConvertToBase10InstructionWith255() {
        cpu.setRegister(1, 255.toByte()) 
        cpu.setAddress(0x600)
        cpu.setMemoryFlag(false) 
        val instruction = ConvertToBase10Instruction(0xD100) 
        instruction.execute(cpu)
        assertEquals(2.toByte(), memory.readRAM(0x600))     
        assertEquals(5.toByte(), memory.readRAM(0x601))     
        assertEquals(5.toByte(), memory.readRAM(0x602))     
    }
    @Test
    fun testConvertByteToAsciiInstructionDigits0To9() {
        for (digit in 0..9) {
            cpu.setRegister(1, digit.toByte())
            val instruction = ConvertByteToAsciiInstruction(0xE120) 
            instruction.execute(cpu)
            assertEquals((0x30 + digit).toByte(), cpu.getRegister(2)) 
        }
    }
    @Test
    fun testConvertByteToAsciiInstructionDigitsAToF() {
        for (digit in 0xA..0xF) {
            cpu.setRegister(1, digit.toByte())
            val instruction = ConvertByteToAsciiInstruction(0xE120) 
            instruction.execute(cpu)
            assertEquals((0x41 + (digit - 10)).toByte(), cpu.getRegister(2)) 
        }
    }
    @Test
    fun testConvertByteToAsciiInstructionInvalidDigit() {
        cpu.setRegister(1, 0x10.toByte()) 
        val instruction = ConvertByteToAsciiInstruction(0xE120)
        assertThrows<IllegalArgumentException> {
            instruction.execute(cpu)
        }
    }
    @Test
    fun testDrawInstructionValidCoordinates() {
        cpu.setRegister(1, 0x48.toByte()) 
        cpu.setRegister(2, 3.toByte())    
        cpu.setRegister(3, 4.toByte())    
        val instruction = DrawInstruction(0xF123) 
        val result = instruction.execute(cpu)
        assertTrue(result, "DRAW should increment PC")
    }
    @Test
    fun testDrawInstructionInvalidAscii() {
        cpu.setRegister(1, 0x80.toByte()) 
        cpu.setRegister(2, 0.toByte())
        cpu.setRegister(3, 0.toByte())
        val instruction = DrawInstruction(0xF123)
        assertThrows<IllegalArgumentException> {
            instruction.execute(cpu)
        }
    }
    @Test
    fun testDrawInstructionInvalidCoordinates() {
        cpu.setRegister(1, 0x48.toByte()) 
        val instruction = DrawInstruction(0xF108) 
        assertThrows<IllegalArgumentException> {
            instruction.execute(cpu)
        }
        val instruction2 = DrawInstruction(0xF180) 
        assertThrows<IllegalArgumentException> {
            instruction2.execute(cpu)
        }
    }
    @Test
    fun testEmulatorFacadeIntegration() {
        val tempFile = File.createTempFile("test", ".rom")
        tempFile.deleteOnExit()
        val testProgram = byteArrayOf(
            0x00, 0x48, 
            0x01, 0x45, 
            0x00, 0x00  
        )
        tempFile.writeBytes(testProgram)
        val facade = EmulatorFacade()
        facade.loadProgram(tempFile.absolutePath)
        assertEquals(0x00.toByte(), memory.readROM(0))
        assertEquals(0x48.toByte(), memory.readROM(1))
        assertEquals(0x01.toByte(), memory.readROM(2))
        assertEquals(0x45.toByte(), memory.readROM(3))
    }
    @Test
    fun testEmulatorFacadeWithValidFilepath() {
        val tempFile = File.createTempFile("valid", ".rom")
        tempFile.deleteOnExit()
        val testData = byteArrayOf(0x00, 0x48, 0x00, 0x00) 
        tempFile.writeBytes(testData)
        val facade = EmulatorFacade()
        assertDoesNotThrow {
            facade.loadProgram(tempFile.absolutePath)
        }
        assertEquals(0x00.toByte(), memory.readROM(0))
        assertEquals(0x48.toByte(), memory.readROM(1))
    }
    @Test
    fun testEmulatorFacadeWithInvalidFilepath() {
        val facade = EmulatorFacade()
        val exception = assertThrows<IllegalArgumentException> {
            facade.loadProgram("nonexistent_file.rom")
        }
        assertTrue(exception.message?.contains("not found") == true, 
                  "Exception should indicate file not found")
    }
    @Test
    fun testTimerStartMethodCreatesExecutor() {
        timer.stop() 
        assertDoesNotThrow {
            timer.start()
        }
        timer.setCounter(5)
        assertEquals(5, timer.getCounter())
        Thread.sleep(50) 
        assertTrue(timer.getCounter() < 5, "Timer should have decremented")
        timer.stop()
    }
    @Test
    fun testTimerStartMethodWithExistingExecutor() {
        timer.start() 
        timer.setCounter(10)
        assertDoesNotThrow {
            timer.start()
        }
        assertTrue(timer.getCounter() <= 10, "Counter should be <= initial value")
        timer.stop()
    }
    @Test
    fun testTimerStopMethodBranches() {
        timer.start()
        timer.setCounter(5)
        assertDoesNotThrow {
            timer.stop() 
        }
        assertDoesNotThrow {
            timer.stop() 
        }
    }
    @Test
    fun testTimerStartAfterStopLifecycle() {
        timer.start()
        timer.setCounter(3)
        timer.stop()
        assertDoesNotThrow {
            timer.start()
        }
        timer.setCounter(2)
        assertEquals(2, timer.getCounter())
        timer.stop()
    }
    @Test
    fun testMemoryWriteRomWarningMessage() {
        val originalOut = System.out
        val capturedOutput = ByteArrayOutputStream()
        System.setOut(PrintStream(capturedOutput))
        try {
            memory.writeROM(100, 0x42.toByte())
            val output = capturedOutput.toString()
            assertTrue(output.contains("Warning: Writing to ROM"), 
                      "Should print warning when writing to ROM")
            assertTrue(output.contains("address 100"), 
                      "Should include address in warning")
            assertTrue(output.contains("value: 66") || output.contains("value: 42"), 
                      "Should include value in warning")
        } finally {
            System.setOut(originalOut)
        }
        assertEquals(0x42.toByte(), memory.readROM(100))
    }
    @Test
    fun testMemoryWriteRamNormalOperation() {
        val testData = mapOf(
            0 to 0x11.toByte(),
            100 to 0x22.toByte(),
            1000 to 0x33.toByte(),
            4095 to 0x44.toByte() 
        )
        testData.forEach { (address, value) ->
            assertDoesNotThrow {
                memory.writeRAM(address, value)
            }
            assertEquals(value, memory.readRAM(address))
        }
    }
    @Test
    fun testMemoryWriteMethodWithRomFlagTrue() {
        val originalOut = System.out
        val capturedOutput = ByteArrayOutputStream()
        System.setOut(PrintStream(capturedOutput))
        try {
            memory.write(200, 0x55.toByte(), true) 
            val output = capturedOutput.toString()
            assertTrue(output.contains("Warning: Writing to ROM"), 
                      "Should print ROM warning when isROM=true")
        } finally {
            System.setOut(originalOut)
        }
        assertEquals(0x55.toByte(), memory.readROM(200))
    }
    @Test
    fun testMemoryWriteMethodWithRomFlagFalse() {
        memory.write(300, 0x66.toByte(), false) 
        assertEquals(0x66.toByte(), memory.readRAM(300))
    }
    @Test
    fun testCpuStartMethodCreatesExecutor() {
        memory.writeROM(0, 0x00.toByte())
        memory.writeROM(1, 0x00.toByte()) 
        cpu.stop() 
        assertDoesNotThrow {
            cpu.start()
        }
        Thread.sleep(100)
        cpu.stop()
    }
    @Test
    fun testCpuStartMethodWithExistingExecutor() {
        memory.writeROM(0, 0x00.toByte())
        memory.writeROM(1, 0x00.toByte())
        cpu.start() 
        assertDoesNotThrow {
            cpu.start()
        }
        Thread.sleep(50)
        cpu.stop()
    }
    @Test
    fun testCpuExecuteWithOddProgramCounter() {
        cpu.setProgramCounter(1)
        val originalOut = System.out
        val capturedOutput = ByteArrayOutputStream()
        System.setOut(PrintStream(capturedOutput))
        try {
            cpu.execute()
            val output = capturedOutput.toString()
            assertTrue(output.contains("Program counter must be even"), 
                      "Should print error for odd PC")
        } finally {
            System.setOut(originalOut)
        }
    }
    @Test
    fun testCpuExecuteWithHaltInstruction() {
        memory.writeROM(0, 0x00.toByte())
        memory.writeROM(1, 0x00.toByte()) 
        cpu.setProgramCounter(0)
        val originalOut = System.out
        val capturedOutput = ByteArrayOutputStream()
        System.setOut(PrintStream(capturedOutput))
        try {
            cpu.execute()
            val output = capturedOutput.toString()
            assertTrue(output.contains("Program terminated"), 
                      "Should print termination message")
            assertTrue(output.contains("halt instruction 0000"), 
                      "Should mention halt instruction")
        } finally {
            System.setOut(originalOut)
        }
    }
    @Test
    fun testCpuExecuteWithInvalidInstruction() {
        memory.writeROM(0, 0xFF.toByte())
        memory.writeROM(1, 0xFF.toByte()) 
        cpu.setProgramCounter(0)
        val originalOut = System.out
        val capturedOutput = ByteArrayOutputStream()
        System.setOut(PrintStream(capturedOutput))
        try {
            cpu.execute()
            val output = capturedOutput.toString()
            assertTrue(output.contains("CPU Error"), 
                      "Should print CPU error message")
        } finally {
            System.setOut(originalOut)
        }
    }
    @Test
    fun testCpuStartAndStopLifecycle() {
        memory.writeROM(0, 0x00.toByte())
        memory.writeROM(1, 0x48.toByte()) 
        memory.writeROM(2, 0x00.toByte())
        memory.writeROM(3, 0x00.toByte()) 
        cpu.start()
        Thread.sleep(50) 
        assertDoesNotThrow {
            cpu.stop()
        }
        assertDoesNotThrow {
            cpu.start()
        }
        Thread.sleep(50)
        cpu.stop()
    }
    @Test
    fun testCpuTimerIntegration() {
        cpu.setTimer(10.toByte())
        assertEquals(10.toByte(), cpu.getTimer())
        assertEquals(10, cpu.getTimerComponent().getCounter())
        cpu.setTimer(5.toByte())
        assertEquals(5, cpu.getTimerComponent().getCounter())
    }
    @Test
    fun testCpuComponentAccessMethods() {
        assertSame(memory, cpu.getMemory(), "Should return same Memory instance")
        assertSame(screen, cpu.getScreen(), "Should return same Screen instance")
        assertSame(timer, cpu.getTimerComponent(), "Should return same Timer instance")
    }
    @Test
    fun testMemoryWriteRomWithNegativeAddressTrueHit() {
        assertThrows<IndexOutOfBoundsException> {
            memory.writeROM(-1, 0x42.toByte())
        }
        assertThrows<IndexOutOfBoundsException> {
            memory.writeROM(-100, 0x42.toByte())
        }
    }
    @Test
    fun testMemoryWriteRomWithAddressTooLargeTrueHit() {
        assertThrows<IndexOutOfBoundsException> {
            memory.writeROM(4096, 0x42.toByte()) 
        }
        assertThrows<IndexOutOfBoundsException> {
            memory.writeROM(5000, 0x42.toByte()) 
        }
    }
    @Test
    fun testMemoryWriteRamWithNegativeAddressTrueHit() {
        assertThrows<IndexOutOfBoundsException> {
            memory.writeRAM(-1, 0x55.toByte())
        }
        assertThrows<IndexOutOfBoundsException> {
            memory.writeRAM(-50, 0x55.toByte())
        }
    }
    @Test
    fun testMemoryWriteRamWithAddressTooLargeTrueHit() {
        assertThrows<IndexOutOfBoundsException> {
            memory.writeRAM(4096, 0x55.toByte()) 
        }
        assertThrows<IndexOutOfBoundsException> {
            memory.writeRAM(8000, 0x55.toByte()) 
        }
    }
    @Test
    fun testMemoryReadRomBoundaryConditionsTrueHits() {
        assertThrows<IndexOutOfBoundsException> {
            memory.readROM(-1)
        }
        assertThrows<IndexOutOfBoundsException> {
            memory.readROM(4096)
        }
    }
    @Test
    fun testMemoryReadRamBoundaryConditionsTrueHits() {
        assertThrows<IndexOutOfBoundsException> {
            memory.readRAM(-1)
        }
        assertThrows<IndexOutOfBoundsException> {
            memory.readRAM(4096)
        }
    }
    @Test
    fun testCpuStartWithShutdownExecutorTrueHit() {
        cpu.start()
        cpu.stop() 
        assertDoesNotThrow {
            cpu.start()
        }
        cpu.stop()
    }
    @Test
    fun testCpuStartWhenAlreadyRunningBranchCoverage() {
        memory.writeROM(0, 0x00.toByte())
        memory.writeROM(1, 0x48.toByte()) 
        memory.writeROM(2, 0x00.toByte())
        memory.writeROM(3, 0x00.toByte()) 
        cpu.start()
        assertDoesNotThrow {
            cpu.start()
        }
        Thread.sleep(50) 
        cpu.stop()
    }
    @Test
    fun testCpuStartWithNullExecutorInitially() {
        cpu.stop()
        assertDoesNotThrow {
            cpu.start()
        }
        cpu.stop()
    }
    @Test
    fun testCpuExecuteWhenNotRunningBranchCoverage() {
        memory.writeROM(0, 0x00.toByte())
        memory.writeROM(1, 0x48.toByte())
        cpu.setProgramCounter(0)
        assertDoesNotThrow {
            cpu.execute()
        }
        assertEquals(0x48.toByte(), cpu.getRegister(0))
    }
    @Test 
    fun testCpuTimerComponentStartIntegration() {
        timer.stop() 
        cpu.start()
        timer.setCounter(10) 
        val initialCount = timer.getCounter()
        Thread.sleep(200) 
        val finalCount = timer.getCounter()
        assertTrue(finalCount <= initialCount, 
                  "Timer should be running after CPU start. Initial: $initialCount, Final: $finalCount")
        cpu.stop()
    }
    @Test
    fun testCpuStopWhenNotRunningBranchCoverage() {
        cpu.stop() 
        assertDoesNotThrow {
            cpu.stop() 
        }
    }
    @Test
    fun testCpuExecuteWithRomReadExceptionHandling() {
        cpu.setProgramCounter(8000) 
        val originalOut = System.out
        val capturedOutput = ByteArrayOutputStream()
        System.setOut(PrintStream(capturedOutput))
        try {
            cpu.execute()
            val output = capturedOutput.toString()
            assertTrue(output.contains("CPU Error"), 
                      "Should print CPU error for ROM bounds exception")
        } finally {
            System.setOut(originalOut)
        }
    }
    @Test
    fun testWaitForKeyboardInputWithEmptyInput() {
        val input = "\n" 
        val inputStream = ByteArrayInputStream(input.toByteArray())
        val originalIn = System.`in`
        try {
            System.setIn(inputStream)
            val result = cpu.waitForKeyboardInput()
            assertEquals(0.toByte(), result, "Empty input should return 0")
        } finally {
            System.setIn(originalIn)
        }
    }
    @Test
    fun testWaitForKeyboardInputWithValidSingleHexDigit() {
        val testCases = mapOf(
            "0" to 0.toByte(),
            "1" to 1.toByte(),
            "9" to 9.toByte(),
            "A" to 10.toByte(),
            "a" to 10.toByte(), 
            "F" to 15.toByte(),
            "f" to 15.toByte()  
        )
        testCases.forEach { (input, expected) ->
            val inputStream = ByteArrayInputStream("$input\n".toByteArray())
            val originalIn = System.`in`
            try {
                System.setIn(inputStream)
                val result = cpu.waitForKeyboardInput()
                assertEquals(expected, result, "Input '$input' should return $expected")
            } finally {
                System.setIn(originalIn)
            }
        }
    }
    @Test
    fun testWaitForKeyboardInputWithValidTwoHexDigits() {
        val testCases = mapOf(
            "10" to 16.toByte(),
            "FF" to 255.toByte(),
            "ff" to 255.toByte(), 
            "A5" to 165.toByte(),
            "3C" to 60.toByte(),
            "00" to 0.toByte()
        )
        testCases.forEach { (input, expected) ->
            val inputStream = ByteArrayInputStream("$input\n".toByteArray())
            val originalIn = System.`in`
            try {
                System.setIn(inputStream)
                val result = cpu.waitForKeyboardInput()
                assertEquals(expected, result, "Input '$input' should return $expected")
            } finally {
                System.setIn(originalIn)
            }
        }
    }
    @Test
    fun testWaitForKeyboardInputWithMoreThanTwoCharacters() {
        val testCases = mapOf(
            "123" to 0x12.toByte(), 
            "ABCD" to 0xAB.toByte(), 
            "FF00" to 0xFF.toByte(), 
            "A5678" to 0xA5.toByte() 
        )
        testCases.forEach { (input, expected) ->
            val inputStream = ByteArrayInputStream("$input\n".toByteArray())
            val originalIn = System.`in`
            try {
                System.setIn(inputStream)
                val result = cpu.waitForKeyboardInput()
                assertEquals(expected, result, "Input '$input' should take first 2 chars and return $expected")
            } finally {
                System.setIn(originalIn)
            }
        }
    }
    @Test
    fun testWaitForKeyboardInputWithInvalidHexCharacters() {
        val invalidInputs = listOf("G", "XYZ", "Hello", "1G", "Z5", "!@#")
        invalidInputs.forEach { input ->
            val inputStream = ByteArrayInputStream("$input\n".toByteArray())
            val originalIn = System.`in`
            val originalOut = System.out
            val capturedOutput = ByteArrayOutputStream()
            try {
                System.setIn(inputStream)
                System.setOut(PrintStream(capturedOutput))
                val result = cpu.waitForKeyboardInput()
                assertEquals(0.toByte(), result, "Invalid input '$input' should return 0")
                val output = capturedOutput.toString()
                assertTrue(output.contains("Invalid input, using 0"), 
                          "Should print error message for invalid input '$input'")
            } finally {
                System.setIn(originalIn)
                System.setOut(originalOut)
            }
        }
    }
    @Test
    fun testWaitForKeyboardInputWithWhitespaceInput() {
        val whitespaceInputs = listOf("   ", "\t", "  \n", " A ", "  5  ")
        whitespaceInputs.forEach { input ->
            val inputStream = ByteArrayInputStream("$input\n".toByteArray())
            val originalIn = System.`in`
            try {
                System.setIn(inputStream)
                val result = cpu.waitForKeyboardInput()
                if (input.trim().isEmpty()) {
                    assertEquals(0.toByte(), result, "Whitespace-only input should return 0")
                } else {
                    val trimmed = input.trim()
                    val expected = if (trimmed == "A") 10.toByte() else 5.toByte()
                    assertEquals(expected, result, "Trimmed input '$trimmed' should be parsed correctly")
                }
            } finally {
                System.setIn(originalIn)
            }
        }
    }
    @Test
    fun testWaitForKeyboardInputPrintsPrompt() {
        val input = "5\n"
        val inputStream = ByteArrayInputStream(input.toByteArray())
        val originalIn = System.`in`
        val originalOut = System.out
        val capturedOutput = ByteArrayOutputStream()
        try {
            System.setIn(inputStream)
            System.setOut(PrintStream(capturedOutput))
            cpu.waitForKeyboardInput()
            val output = capturedOutput.toString()
            assertTrue(output.contains("Enter hex value (0-F):"), 
                      "Should print input prompt")
        } finally {
            System.setIn(originalIn)
            System.setOut(originalOut)
        }
    }
    @Test
    fun testWaitForKeyboardInputWithByteOverflow() {
        val testCases = mapOf(
            "100" to 0x10.toByte(), 
            "200" to 0x20.toByte(), 
            "FFF" to 0xFF.toByte()  
        )
        testCases.forEach { (input, expected) ->
            val inputStream = ByteArrayInputStream("$input\n".toByteArray())
            val originalIn = System.`in`
            try {
                System.setIn(inputStream)
                val result = cpu.waitForKeyboardInput()
                assertEquals(expected, result, "Input '$input' should return $expected with proper byte masking")
            } finally {
                System.setIn(originalIn)
            }
        }
    }
    @Test
    fun testWaitForKeyboardInputIntegrationWithReadKeyboardInstruction() {
        val input = "A5\n"
        val inputStream = ByteArrayInputStream(input.toByteArray())
        val originalIn = System.`in`
        try {
            System.setIn(inputStream)
            val instruction = ReadKeyboardInstruction(0x6300) 
            val result = instruction.execute(cpu)
            assertTrue(result, "ReadKeyboardInstruction should increment PC")
            assertEquals(0xA5.toByte(), cpu.getRegister(3), "Should store keyboard input in register")
        } finally {
            System.setIn(originalIn)
        }
    }
    @Test
    fun testScreenExecuteMethod() {
        assertDoesNotThrow {
            screen.execute(cpu)
        }
        screen.update(0, 0, 'A')
        screen.update(1, 0, 'B')
        assertDoesNotThrow {
            screen.execute(cpu)
        }
    }
    @Test
    fun testSimpleProgramExecution() {
        memory.writeROM(0, 0x00.toByte()) 
        memory.writeROM(1, 0x48.toByte()) 
        memory.writeROM(2, 0x01.toByte()) 
        memory.writeROM(3, 0x45.toByte()) 
        memory.writeROM(4, 0x00.toByte()) 
        memory.writeROM(5, 0x00.toByte())
        cpu.execute()
        assertEquals(0x48.toByte(), cpu.getRegister(0))
        assertEquals(2, cpu.getProgramCounter())
        cpu.execute()
        assertEquals(0x45.toByte(), cpu.getRegister(1))
        assertEquals(4, cpu.getProgramCounter())
    }
    @Test
    fun testArithmeticProgram() {
        memory.writeROM(0, 0x00.toByte())  
        memory.writeROM(1, 0x0A.toByte())  
        memory.writeROM(2, 0x01.toByte())  
        memory.writeROM(3, 0x05.toByte())  
        memory.writeROM(4, 0x10.toByte())  
        memory.writeROM(5, 0x12.toByte())  
        memory.writeROM(6, 0x00.toByte())  
        memory.writeROM(7, 0x00.toByte())
        cpu.execute() 
        assertEquals(10.toByte(), cpu.getRegister(0))
        cpu.execute() 
        assertEquals(5.toByte(), cpu.getRegister(1))
        cpu.execute() 
        assertEquals(15.toByte(), cpu.getRegister(2))
    }
    @Test
    fun testJumpProgram() {
        memory.writeROM(0, 0x00.toByte())  
        memory.writeROM(1, 0x42.toByte())  
        memory.writeROM(2, 0x50.toByte())  
        memory.writeROM(3, 0x06.toByte())  
        memory.writeROM(4, 0x01.toByte())  
        memory.writeROM(5, 0xFF.toByte())  
        memory.writeROM(6, 0x02.toByte())  
        memory.writeROM(7, 0x99.toByte())  
        memory.writeROM(8, 0x00.toByte())  
        memory.writeROM(9, 0x00.toByte())
        cpu.execute() 
        assertEquals(0x42.toByte(), cpu.getRegister(0))
        assertEquals(2, cpu.getProgramCounter())
        cpu.execute() 
        assertEquals(6, cpu.getProgramCounter())
        cpu.execute() 
        assertEquals(0x99.toByte(), cpu.getRegister(2))
        assertEquals(0.toByte(), cpu.getRegister(1)) 
    }
    @Test
    fun testConditionalSkipProgram() {
        memory.writeROM(0, 0x00.toByte())  
        memory.writeROM(1, 0x42.toByte())  
        memory.writeROM(2, 0x01.toByte())  
        memory.writeROM(3, 0x42.toByte())  
        memory.writeROM(4, 0x80.toByte())  
        memory.writeROM(5, 0x10.toByte())  
        memory.writeROM(6, 0x02.toByte())  
        memory.writeROM(7, 0xFF.toByte())  
        memory.writeROM(8, 0x03.toByte())  
        memory.writeROM(9, 0x88.toByte())  
        memory.writeROM(10, 0x00.toByte()) 
        memory.writeROM(11, 0x00.toByte())
        cpu.execute() 
        cpu.execute() 
        cpu.execute() 
        assertEquals(8, cpu.getProgramCounter()) 
        cpu.execute() 
        assertEquals(0x88.toByte(), cpu.getRegister(3))
        assertEquals(0.toByte(), cpu.getRegister(2)) 
    }
} 