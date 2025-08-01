package org.example
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    println("Welcome to the D5700 Emulator.")
    print("Enter the path to the ROM file: ")

    val path = readlnOrNull()?.trim()
    if (path.isNullOrEmpty()) {
        println("No path entered.")
        return
    }

    val emulator = EmulatorFacade()
    emulator.loadProgram(path)
    emulator.run()
}