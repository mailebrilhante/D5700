package org.example.core

import java.io.File

class Memory private constructor() {
    private val rom = ByteArray(4096)
    private val ram = ByteArray(4096)

    companion object {
        private val instance = Memory()
        fun getInstance() = instance
    }

    fun loadROM(path: String) {
        val file = File(path)
        if (!file.exists()) {
            throw IllegalArgumentException("ROM file not found: $path")
        }
        
        val bytes = file.readBytes()
        if (bytes.size > rom.size) {
            throw IllegalArgumentException("ROM file too large: ${bytes.size} bytes (max ${rom.size})")
        }
        
        rom.fill(0)
        
        System.arraycopy(bytes, 0, rom, 0, bytes.size)
        println("ROM loaded with ${bytes.size} bytes.")
    }

    fun read(addr: Int, isROM: Boolean): Byte {
        return if (isROM) {
            readROM(addr)
        } else {
            readRAM(addr)
        }
    }

    fun readROM(addr: Int): Byte {
        if (addr < 0 || addr >= rom.size) {
            throw IndexOutOfBoundsException("ROM address out of bounds: $addr")
        }
        return rom[addr]
    }

    fun readRAM(addr: Int): Byte {
        if (addr < 0 || addr >= ram.size) {
            throw IndexOutOfBoundsException("RAM address out of bounds: $addr")
        }
        return ram[addr]
    }

    fun write(addr: Int, value: Byte, isROM: Boolean) {
        if (isROM) {
            writeROM(addr, value)
        } else {
            writeRAM(addr, value)
        }
    }

    fun writeROM(addr: Int, value: Byte) {
        if (addr < 0 || addr >= rom.size) {
            throw IndexOutOfBoundsException("ROM address out of bounds: $addr")
        }
        println("Warning: Writing to ROM at address $addr (value: $value)")
        rom[addr] = value
    }

    fun writeRAM(addr: Int, value: Byte) {
        if (addr < 0 || addr >= ram.size) {
            throw IndexOutOfBoundsException("RAM address out of bounds: $addr")
        }
        ram[addr] = value
    }
} 