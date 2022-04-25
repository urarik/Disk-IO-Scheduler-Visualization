package DataTypes

data class Result(
    val requestNo: Int,
    val seekTime: Double,
    val rotationalLatency: Double,
    val transmissionTime: Double,
    val accessTime: Double,
    val responseTime: Double,
    ) {
}