package SchedulingAlgorithms

import DataTypes.Progress
import DataTypes.Request
import DataTypes.Result
import kotlin.math.absoluteValue
import kotlin.math.pow

abstract class Scheduling(protected val size: Int, protected var currentTrack: Int, protected var currentSector: Int) {
    var totalExecutionTime = 0.0
    var averageSeekTime = 0.0
    var averageRotationalLantency =0.0
    var averageResponseTime = 0.0
    var standardDeviation = 0.0
    var coefficientVariation = 0.0
    protected val results = mutableListOf<Result>()
    protected val progresses = mutableListOf<Progress>()

    protected val maxRotationalLatencyMilli = Disk.maxRotationalLatencyMilli
    protected val maxSeekTimeMilli = Disk.maxSeekTimeMilli
    protected val revolutionsPerSec = Disk.revolutionsPerSec
    protected val bytesPerTrack = Disk.bytesPerTrack
    protected val sectorsPerTrack = Disk.sectorsPerTrack
    protected val tracksPerSurface = Disk.tracksPerSurfaces
    protected var currentTime = 0.0
    protected var accumulatedSeek = 0.0

    protected var seekTime = 0.0
    protected var length = 0
    protected var rotationalLatency = 0.0
    protected var transmissionTime = 0.0
    protected var accessTime = 0.0
    protected var responseTime = 0.0

    abstract fun run():Pair<List<Result>, List<Progress>>
    protected fun setTotalTime() {
        results.forEach {
            averageRotationalLantency += it.rotationalLatency
            averageSeekTime += it.seekTime
            averageResponseTime += it.responseTime
        }

        totalExecutionTime = averageSeekTime + averageRotationalLantency
        averageRotationalLantency /= size
        averageSeekTime /= size
        averageResponseTime /= size

        standardDeviation = results.fold(0.0, { total, num ->
            total + (averageResponseTime - num.responseTime).pow(2)
        }).div(size)
            .pow(0.5)
        coefficientVariation = standardDeviation.pow(2)/averageResponseTime.pow(2)
    }

    protected fun addResult(request: Request) {
        with(request) {
            seekTime = (currentTrack - track).absoluteValue / tracksPerSurface.toDouble() * maxSeekTimeMilli
            if(accumulatedSeek != 0.0) {
                seekTime += accumulatedSeek
                accumulatedSeek = 0.0
            }


            var timeTaken = transmissionTime + rotationalLatency + seekTime
            timeTaken %= maxRotationalLatencyMilli
            currentSector += ((timeTaken * sectorsPerTrack)/maxRotationalLatencyMilli).toInt()
            currentSector %= 50
            length = if (this.sector < currentSector) 50 - currentSector + this.sector else this.sector - currentSector
            rotationalLatency = (length / 50.0 * maxRotationalLatencyMilli)

            transmissionTime = size.toDouble() / (revolutionsPerSec * bytesPerTrack) * 1000

            progresses.add(Progress(track, this.sector, currentSector))
            accessTime = seekTime + rotationalLatency + transmissionTime
            currentTime += accessTime
            responseTime = currentTime - arrivalTime

            results.add(Result(requestNo,seekTime, rotationalLatency, transmissionTime, accessTime, responseTime))

            currentTrack = track
        }
    }
}