import tornadofx.*

fun main(args: Array<String>) {
    launch<Scheduler>(args)
}

open class Scheduler: App(MainView::class)