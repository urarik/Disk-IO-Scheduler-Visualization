package SchedulingAlgorithms

import DataTypes.*

class FCFS(private val requestList: List<Request>, position: Int, currentSector: Int) :
    Scheduling(requestList.size, position, currentSector) {

    override fun run(): Pair<List<Result>, List<Progress>> {

        for (request in requestList) {
            if(currentTime < request.arrivalTime) currentTime = request.arrivalTime
            addResult(request)
        }

        setTotalTime()
        return Pair(results, progresses)
    }

}