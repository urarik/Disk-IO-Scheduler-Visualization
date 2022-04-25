package SchedulingAlgorithms

import DataTypes.*
import kotlin.math.absoluteValue
import kotlin.random.Random

class NStepSCAN(
    requestList: List<Request>,
    position: Int,
    private var isDown: Boolean,
    private val n: Int,
    currentSector: Int
) : Scheduling(requestList.size, position, currentSector) {
    private val queue = mutableListOf<Request>()
    private val requests = mutableListOf<Request>()
    private var positionInSubList: Int = 0

    init {
        requestList.forEach { requests.add(it) }
    }

    override fun run(): Pair<List<Result>, List<Progress>> {

        while (queue.isNotEmpty() || requests.isNotEmpty()) {
            fillQueue()
            val subRequests = mutableListOf<Request>().apply {
                for(j in 0 until n) {
                    if(queue.isEmpty()) break
                    add(queue.removeFirst())
                }
            }.sortedBy { it.track }.toMutableList()
            val subN = subRequests.size
            setPositionInSubList(subRequests, subN)

            while(subRequests.isNotEmpty()) {
                val request = subRequests.removeAt(positionInSubList)
                if(!isDown && request.track < currentTrack) {
                    accumulatedSeek = maxSeekTimeMilli / tracksPerSurface.toDouble() * (currentTrack - (tracksPerSurface - 1)).absoluteValue
                    currentTrack = tracksPerSurface - 1
                    progresses.add(Progress(currentTrack))
                    isDown = !isDown
                }
                if (isDown) {
                    positionInSubList--
                    addResult(request)
                    if (positionInSubList == -1) {
                        currentTrack = 0
                        accumulatedSeek = maxSeekTimeMilli / tracksPerSurface.toDouble() * (currentTrack - request.track).absoluteValue
                        progresses.add(Progress(currentTrack))
                        positionInSubList++
                        isDown = !isDown
                    }
                } else {
                    addResult(request)
                    if (positionInSubList == subRequests.size) {
                        currentTrack = tracksPerSurface - 1
                        accumulatedSeek = maxSeekTimeMilli / tracksPerSurface.toDouble() * (currentTrack - request.track).absoluteValue
                        progresses.add(Progress(currentTrack))
                        positionInSubList--
                        isDown = !isDown
                    }
                }
            }
        }

        progresses.removeLast()
        setTotalTime()
        return Pair(results, progresses)
    }

    private fun setPositionInSubList(subRequest: List<Request>, subN: Int) {
        positionInSubList = findCurrentTrack(subRequest)

        if ((positionInSubList in 0 until subN) && subRequest[positionInSubList].track != currentTrack) {
            if (isDown) {
                positionInSubList--
                if (positionInSubList == -1) {
                    accumulatedSeek = maxSeekTimeMilli / tracksPerSurface.toDouble() * (currentTrack).absoluteValue
                    currentTrack = 0
                    progresses.add(Progress(currentTrack))
                    positionInSubList++
                    isDown = !isDown
                }
            }
        } else if (positionInSubList >= subN)
            positionInSubList--
    }

    private fun findCurrentTrack(list: List<Request>): Int {
        var low = 0
        var high = list.size - 1

        while (low <= high) {
            val mid = (low + high) / 2
            val cmp = currentTrack.compareTo(list[mid].track)

            if (cmp < 0) high = mid - 1
            else if (cmp > 0) low = mid + 1
            else return mid
        }

        return low
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
}