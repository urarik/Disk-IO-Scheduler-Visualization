package SchedulingAlgorithms

import DataTypes.Progress
import DataTypes.Request
import DataTypes.Result
import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random

class SATF(requestList: List<Request>, position: Int, currentSector: Int) :
    Scheduling(requestList.size, position, currentSector) {

    private val queue = mutableListOf<Request>()
    private val requests = mutableListOf<Request>()

    init {
        requestList.forEach { requests.add(it) }
        fillQueue()
    }


    override fun run(): Pair<List<Result>, List<Progress>> {

        while(queue.isNotEmpty() || requests.isNotEmpty()) {
            val request = getRequest()
            addResult(request)
            fillQueue()
        }

        setTotalTime()
        return Pair(results, progresses)
    }

    private fun fillQueue(): Boolean {
        var state = false
        with(requests.iterator()) {
            forEach {
                if (it.arrivalTime <= currentTime) {
                    queue.add(it)
                    state = true
                    remove()
                } else return@with
            }
        }
        if(queue.isEmpty() && requests.isNotEmpty()) {
            val request = requests.removeFirst()
            queue.add(request)
            currentTime = request.arrivalTime
            state = true
        }

        return state
    }

    private fun getRequest(): Request {
        var minAccessTime = Double.MAX_VALUE
        var minRequest: Request? = null
        for (request in queue) {
            val accessTime = getAccessTime(request)
            if(accessTime < minAccessTime) {
                minAccessTime = accessTime
                minRequest = request
            }
        }

        queue.remove(minRequest!!)
        return minRequest
    }
    private fun getAccessTime(request: Request): Double {
        return with(request) {
            var seekTime = maxSeekTimeMilli / tracksPerSurface.toDouble() * (currentTrack - track).absoluteValue
            if(accumulatedSeek != 0.0) {
                seekTime += accumulatedSeek
                accumulatedSeek = 0.0
            }

            var timeTaken = transmissionTime + rotationalLatency + seekTime
            timeTaken %= maxRotationalLatencyMilli
            var currentSector = super.currentSector + (((360 * timeTaken) / maxRotationalLatencyMilli) / (360 / sectorsPerTrack)).toInt()
            currentSector %= 50
            val length = if (this.sector < currentSector) 50 - currentSector + this.sector else this.sector - currentSector
            val rotationalLatency = (length / 50.0 * maxRotationalLatencyMilli)

            val transmissionTime = size.toDouble() / (revolutionsPerSec * bytesPerTrack) * 1000

            seekTime + rotationalLatency + transmissionTime
        }
    }

}