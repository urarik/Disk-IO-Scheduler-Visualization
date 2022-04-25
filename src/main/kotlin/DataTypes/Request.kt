package DataTypes

data class Request(val requestNo: Int,
                   val arrivalTime: Double,
                   val sector: Int,
                   val track: Int,
                   val size: Int) {
}