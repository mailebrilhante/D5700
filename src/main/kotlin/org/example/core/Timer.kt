package org.example.core

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class Timer private constructor() {
    private var counter: Int = 0
    private var executor: ScheduledExecutorService? = null
    private var timerFuture: ScheduledFuture<*>? = null

    companion object {
        private val instance = Timer()
        fun getInstance() = instance
    }

    fun start() {
        if (executor?.isShutdown != false) {
            executor = Executors.newSingleThreadScheduledExecutor()
        }
        
        val timerRunnable = Runnable {
            tick()
        }
        
        timerFuture = executor?.scheduleAtFixedRate(
            timerRunnable,
            0,
            1000L / 60L,
            TimeUnit.MILLISECONDS
        )
    }

    fun tick() {
        if (counter > 0) {
            counter--
        }
    }

    fun stop() {
        timerFuture?.cancel(true)
        executor?.shutdown()
    }

    fun setCounter(value: Int) {
        counter = value
    }

    fun getCounter(): Int = counter
} 