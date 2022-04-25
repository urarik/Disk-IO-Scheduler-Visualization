object Disk {
    private const val bytesPerSector = 512
    const val sectorsPerTrack = 50
    var tracksPerSurfaces = 200
    private const val RPM = 5400
    const val maxRotationalLatencyMilli = ((1.0 / RPM) * 60) * 1000
    val maxSeekTimeMilli = 20
    const val revolutionsPerSec = RPM / 60
    const val bytesPerTrack = bytesPerSector * sectorsPerTrack
}