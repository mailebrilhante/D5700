package org.example.core

class Screen private constructor() {
    private val grid = Array(8) { Array(8) { ' ' } }

    companion object {
        private val instance = Screen()
        fun getInstance() = instance
    }

    fun update(x: Int, y: Int, character: Char) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw IndexOutOfBoundsException("Screen coordinates out of bounds: ($x, $y)")
        }
        grid[y][x] = character
    }

    fun draw() {
        print("\u001b[2J\u001b[H")
        
        println("┌────────────────┐")
        for (row in grid) {
            print("│")
            for (char in row) {
                print("$char ")
            }
            println("│")
        }
        println("└────────────────┘")
        println()
    }

    fun execute(cpu: CPU) {
        draw()
    }
} 