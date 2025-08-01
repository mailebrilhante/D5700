package org.example
import java.io.File

class Memory private constructor() {
    private val rom = ByteArray(256) // for example

    companion object {
        private val instance = Memory()
        fun getInstance() = instance
    }

    fun loadROM(path: String) {
        val file = File(path)
        val bytes = file.readBytes()
        for (i in bytes.indices) {
            rom[i] = bytes[i]
        }
        println("ROM loaded with ${bytes.size} bytes.")
    }

    fun read(address: Int): Byte = rom[address]
}