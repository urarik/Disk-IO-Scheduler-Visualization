
import DataTypes.Result
import javafx.scene.chart.NumberAxis
import tornadofx.View
import tornadofx.form
import tornadofx.linechart
import tornadofx.series
import tornadofx.*


class Selectivity : View("My View") {
    override val root = form {
        prefWidth = 250.0
        prefHeight = 250.0
    }

    fun draw(list: List<Result>, name: String) {
        val items = mutableMapOf<Int, Int>()
        list.forEach {
                items[it.responseTime.toInt()/10] = items[it.responseTime.toInt()/10]?.plus(1) ?: 1
        }
        with(root) {
            clear()
            linechart("Response Time Distribution", NumberAxis(), NumberAxis()) {
                series(name) {
                    items.forEach {
                        data(it.key, it.value)
                    }
                }
            }

        }

    }
}