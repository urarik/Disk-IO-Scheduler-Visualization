import DataTypes.Progress
import javafx.geometry.Orientation
import javafx.scene.control.ScrollBar
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import javafx.scene.text.Font
import tornadofx.*
import tornadofx.Stylesheet.Companion.fitToHeight
import tornadofx.Stylesheet.Companion.line
import tornadofx.Stylesheet.Companion.scrollBar

class Visualization:View() {
    lateinit var palette: Form
    override val root = scrollpane() {
        prefHeight = 500.0
        palette = form() {
            setPrefSize(1000.0, 400.0)
        }

    }

    fun draw(progresses: List<Progress>, position: Int, isLimited: Boolean) {
        if(isLimited && progresses.size > 100)
            return
        with(palette) {
            add(ScrollBar().apply {
                orientation = Orientation.VERTICAL
                min = 0.0
                max = 100.0
                layoutX = width - this.width
                isManaged = false
            })
            clear()
            line {
                startX = 10.0
                startY = 30.0
                endX = width - 10
                endY = 30.0
                isManaged = false
            }

            var y = 30.0
            var prevX = 10 + (position * (width - 10)) / Disk.tracksPerSurfaces
            val sectorsPerTrack = Disk.sectorsPerTrack
            for (i in 0 until progresses.size) {
                val progress = progresses[i]
                val x = 10 + (progress.track * (width - 10)) / Disk.tracksPerSurfaces
                y += 40
                if (progress.sector != -1) {
                    circle(x, y, 10).apply {
                        fill = Color.BLUE
                        isManaged = false
                    }
                    arc(x, y, 10, 10).apply {
                        startAngle = (progress.currentSector * 360.0) / sectorsPerTrack
                        val requestAngle = (progress.sector * 360.0) / sectorsPerTrack
                        length =
                            if (requestAngle < startAngle) 360 - startAngle + requestAngle else requestAngle - startAngle
                        fill = Color.ORANGE
                        type = ArcType.ROUND
                        isManaged = false
                    }
                }
                line {
                    startX = prevX
                    startY = y - 40
                    endX = x
                    endY = y
                    isManaged = false
                }
                text(progress.track.toString()) {
                    this.x = x - 5
                    this.y = y - 20
                    font = Font(15.0)
                    isManaged = false
                }
                prevX = x
            }
            prefHeight = y + 30.0
        }
    }


}