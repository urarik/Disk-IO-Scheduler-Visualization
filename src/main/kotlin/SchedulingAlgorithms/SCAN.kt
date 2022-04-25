package SchedulingAlgorithms

import DataTypes.*
import java.util.*
import kotlin.Comparator
import kotlin.math.absoluteValue
import kotlin.random.Random

class SCAN(
    requestList: List<Request>,
    position: Int,
    private var isDown: Boolean,
    currentSector: Int
) : Scheduling(requestList.size, position, currentSector) {
    val comparator = Comparator<Request> { request1, request2 ->
        when {
            request1.track < request2.track -> -1
            request1.track > request2.track -> 1
            else -> 0
        }
    }
    private val queue = mutableListOf<Request>()
    private val requests = mutableListOf<Request>()
    private var positionInList = 0

    init {
        requestList.forEach { requests.add(it) }
    }

    override fun run(): Pair<List<Result>, List<Progress>> {

        while (queue.isNotEmpty() || requests.isNotEmpty()) {
            if (fillQueue(if (queue.isNotEmpty()) maxSeekTimeMilli / tracksPerSurface.toDouble() * (currentTrack - queue[positionInList].track).absoluteValue else 0.0))
                setPositionInList()
            val request = queue.removeAt(positionInList)

            if (isDown) {
                positionInList--
                if (positionInList == -1) {
                    addResult(request)
                    accumulatedSeek =
                        maxSeekTimeMilli / tracksPerSurface.toDouble() * (currentTrack - request.track).absoluteValue
                    currentTrack = 0
                    progresses.add(Progress(currentTrack))
                    positionInList++
                    isDown = !isDown
                } else addResult(request)
            } else {
                if (positionInList == queue.size) {
                    val tempTrack = currentTrack
                    if (tempTrack <= request.track) {
                        addResult(request)
                        if(fillQueue(maxSeekTimeMilli / tracksPerSurface.toDouble() * (tracksPerSurface - 1 - tempTrack).absoluteValue)) {
                            setPositionInList()
                            continue
                        }
                        positionInList--
                    }
                    else {
                        fillQueue(maxSeekTimeMilli / tracksPerSurface.toDouble() * (tracksPerSurface - 1 - tempTrack).absoluteValue)
                        queue.add(request)
                        Collections.sort(queue, comparator)
                        setPositionInList()
                    }

                    accumulatedSeek =
                        maxSeekTimeMilli / tracksPerSurface.toDouble() * (currentTrack - request.track).absoluteValue
                    currentTrack = tracksPerSurface - 1
                    progresses.add(Progress(currentTrack))

                    isDown = !isDown
                } else addResult(request)
            }

        }

        progresses.removeLast()
        setTotalTime()
        return Pair(results, progresses)
    }

    private fun setPositionInList() {
        positionInList = findCurrentTrack(queue)

        if ((positionInList in 0 until queue.size) && queue[positionInList].track != currentTrack) {
            if (isDown) {
                positionInList--
                if (positionInList == -1) {
                    accumulatedSeek = maxSeekTimeMilli / tracksPerSurface.toDouble() * (currentTrack).absoluteValue
                    currentTrack = 0
                    progresses.add(Progress(currentTrack))
                    positionInList++
                    isDown = !isDown
                }
            }
        } else if (positionInList >= queue.size)
            positionInList--
    }

    private fun fillQueue(seekTime: Double): Boolean {
        var state = false
        var interval = currentTime + seekTime
        if (accumulatedSeek != 0.0) interval += accumulatedSeek
        with(requests.iterator()) {
            forEach {
                if (it.arrivalTime <= interval &&
                    (maxSeekTimeMilli / tracksPerSurface.toDouble() * (currentTrack - it.track).absoluteValue + accumulatedSeek + currentTime >= it.arrivalTime)
                ) {
                    queue.add(it)
                    state = true
                    remove()
                } else return@with
            }
        }
        if (queue.isEmpty() && requests.isNotEmpty()) {
            val request = requests.removeFirst()
            queue.add(request)
            currentTime = request.arrivalTime
            state = true
        }
        if (state)
            Collections.sort(queue, comparator)

        return state
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

}