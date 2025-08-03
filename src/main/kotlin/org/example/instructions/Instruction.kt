package org.example.instructions

import org.example.core.CPU

abstract class Instruction {
    abstract fun execute(cpu: CPU): Boolean
} 