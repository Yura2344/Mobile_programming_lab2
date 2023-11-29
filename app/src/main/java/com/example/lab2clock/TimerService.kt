package com.example.lab2clock

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class TimerService: Service() {
    private var binder: TimerBinder = TimerBinder()

    private var milliseconds: Long = 0
    private var startTime: Long = 0
    private var lastRound: Long = 0

    var isTimerActive: Boolean = false
        private set
    var isTimerPaused: Boolean = false
        private set

    var handler: Handler = Handler(Looper.getMainLooper())
        private set

    var roundsList: ArrayList<String> = arrayListOf()
        private set

    private var observers: ArrayList<TimerActivity> = arrayListOf()

    private var timeCounter: Runnable = object : Runnable {
        override fun run() {
            milliseconds = System.currentTimeMillis() - startTime
            updateObservers()

            if (isTimerActive && !isTimerPaused)
                handler.post(this)
        }
    }

    private fun getTimeString(milliseconds: Long): String {
        return String.format(
            "%02d:%02d.%02d",
            milliseconds / 60000,
            milliseconds / 1000,
            (milliseconds % 1000) / 10
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    inner class TimerBinder: Binder(){
        val service: TimerService
            get() = this@TimerService
    }

    fun startTimer(){
        isTimerActive = true
        isTimerPaused = false
        lastRound = 0
        startTime = System.currentTimeMillis()
        roundsList.clear()
        handler.post(timeCounter)
    }

    fun stopTimer() {
        isTimerPaused = false
        isTimerActive = false

    }

    fun pauseTimer() {
        if(isTimerActive){
            isTimerPaused = !isTimerPaused

            if (!isTimerPaused) {
                startTime += System.currentTimeMillis() - (startTime + milliseconds)
                handler.post(timeCounter)
            }
        }
    }

    fun addRound() {
        if(isTimerActive){
            val timeDifference: Long = milliseconds - lastRound
            roundsList.add("+${getTimeString(timeDifference)}")
            lastRound = milliseconds
        }
    }

    fun addObserver(observer: TimerActivity)
    {
        if(!observers.contains(observer)) {
            observers.add(observer)
            observer.updatePauseButton(isTimerPaused)
            observer.updateTimer(getTimeString(milliseconds))
        }
    }

    fun removeObserver(observer: TimerActivity)
    {
        observers.remove(observer)
    }

    fun updateObservers(){
        for(observer in observers) {
            observer.updatePauseButton(isTimerPaused)
            observer.updateTimer(getTimeString(milliseconds))
        }
    }
}