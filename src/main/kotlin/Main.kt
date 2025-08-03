package org.example

import org.example.core.EmulatorFacade

fun main() {
    println("Welcome to the D5700 Emulator.")
    print("Enter the path to the ROM file: ")

    val path = readlnOrNull()?.trim()
    if (path.isNullOrEmpty()) {
        println("No path entered.")
        return
    }

    try {
        val emulator = EmulatorFacade()
        emulator.loadProgram(path)
        emulator.run()
    } catch (e: IllegalArgumentException) {
        println("Please enter correct filepath.")
    }
}