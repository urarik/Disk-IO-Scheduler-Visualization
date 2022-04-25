import DataTypes.Request
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.Comparator

class RequestTokenizer(private val file: File) {
    fun getProcesses(): List<Request> {
        val reader = FileReader(file)
        val lines = reader.readLines()
        val requests = mutableListOf<Request>()
        for(line in lines) {
            val words = line.split(" ")
            if(words[0] == "request")
                requests.add(Request(words[1].toInt(), words[2].toDouble(), words[3].toInt(), words[4].toInt(), words[5].toInt()))
        }

        val comparator = Comparator<Request> { request1, request2 ->
            when {
                request1.arrivalTime < request2.arrivalTime -> -1
                request1.arrivalTime > request2.arrivalTime -> 1
                else -> 0
            }
        }.apply { thenComparator { request1, request2 ->
            when {
                request1.requestNo < request2.requestNo -> -1
                request1.requestNo > request2.requestNo -> 1
                else -> 0
            }
        }}

        Collections.sort(requests, comparator)
        return requests
    }
}