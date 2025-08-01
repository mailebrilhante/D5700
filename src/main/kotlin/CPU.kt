package org.example

class CPU private constructor() {
    companion object {
        private val instance = CPU()
        fun getInstance() = instance
    }

    fun start() {
        println("CPU started.")
        // Begin instruction loop here later
    }
}