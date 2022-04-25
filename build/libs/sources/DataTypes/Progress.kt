package DataTypes

data class Progress(val track: Int,
                    val sector: Int = -1,
                    val currentSector: Int = -1) {
}