package org.example.instructions

class InstructionFactory {
    fun createInstruction(instruction: Int): Instruction {
        val opcode = (instruction shr 12) and 0xF
        
        return when (opcode) {
            0x0 -> StoreInstruction(instruction)
            0x1 -> AddInstruction(instruction)
            0x2 -> SubInstruction(instruction)
            0x3 -> ReadInstruction(instruction)
            0x4 -> WriteInstruction(instruction)
            0x5 -> JumpInstruction(instruction)
            0x6 -> ReadKeyboardInstruction(instruction)
            0x7 -> SwitchMemoryInstruction(instruction)
            0x8 -> SkipEqualInstruction(instruction)
            0x9 -> SkipNotEqualInstruction(instruction)
            0xA -> SetAddressInstruction(instruction)
            0xB -> SetTimerInstruction(instruction)
            0xC -> ReadTimerInstruction(instruction)
            0xD -> ConvertToBase10Instruction(instruction)
            0xE -> ConvertByteToAsciiInstruction(instruction)
            0xF -> DrawInstruction(instruction)
            else -> throw IllegalArgumentException("Unknown instruction opcode: ${opcode.toString(16)}")
        }
    }
} 