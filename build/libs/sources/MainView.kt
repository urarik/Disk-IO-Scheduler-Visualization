import DataTypes.*
import SchedulingAlgorithms.*
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import tornadofx.*

class MainView : View() {
    private val visualization = find(Visualization::class)
    private val selectivitiy = find(Selectivity::class)
    private val requests = FXCollections.observableArrayList<Request>()
    private val results = FXCollections.observableArrayList<Result>()
    private val directionGroup = ToggleGroup()
    private val labels = mutableListOf<Label>()
    private val texts = mutableListOf<TextField>()
    lateinit var schedType: ComboBox<String>
    lateinit var currentTrackTextField: TextField
    lateinit var sectorTextField: TextField
    lateinit var trackTextField: TextField
    lateinit var nTextField: TextField
    lateinit var minTextField: TextField
    lateinit var maxTextField: TextField
    lateinit var isLimited: CheckBox

    lateinit var hboxForAdd: HBox
    lateinit var requestTable: TableView<Request>
    lateinit var resultTable: TableView<Result>

    override val root: Parent = HBox()

    init {
        with(root) {
            vbox {
                prefWidth = 1050.0
                hbox {
                    alignment = Pos.CENTER
                    schedType = combobox {
                        items = listOf(
                            "FCFS",
                            "N-Step SCAN",
                            "SCAN",
                            "SATF",
                            "SAATF"
                        ).asObservable()
                        style {
                            font = Font(13.0)
                        }
                        selectionModel.selectFirst()
                    }
                    label("# of tracks") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(15.0)
                    }
                    trackTextField = textfield() {
                        filterInput { change ->
                            !change.isAdded || change.controlNewText.let {
                                it.isInt()
                            }
                        }
                        maxWidth = 100.0
                        text = "200"
                        font = Font(15.0)
                    }
                    label("direction") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(15.0)
                    }
                    radiobutton("Down", directionGroup) {
                        isSelected = true
                        font = Font(12.0)
                    }
                    radiobutton("Up", directionGroup) {
                        font = Font(12.0)
                    }
                    label("sector") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(15.0)
                    }
                    sectorTextField = textfield() {
                        filterInput { change ->
                            !change.isAdded || change.controlNewText.let {
                                it.isInt()
                            }
                        }
                        maxWidth = 100.0
                        text = "10"
                        font = Font(15.0)
                    }
                    label("track") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(15.0)
                    }
                    currentTrackTextField = textfield() {
                        filterInput { change ->
                            !change.isAdded || change.controlNewText.let {
                                it.isInt()
                            }
                        }
                        maxWidth = 100.0
                        text = "53"
                        font = Font(15.0)
                    }
                    isLimited = checkbox("No drawing if\n # of requests > 100").apply {
                        isSelected = true
                    }

                }
                hboxForAdd = hbox {
                    label("n") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(20.0)
                    }
                    nTextField = textfield() {
                        filterInput { change ->
                            !change.isAdded || change.controlNewText.let {
                                it.isInt()
                            }
                        }
                        maxWidth = 100.0
                        text = "0.5"
                        font = Font(20.0)
                    }
                    label("min") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(20.0)
                    }
                    minTextField = textfield() {
                        filterInput { change ->
                            !change.isAdded || change.controlNewText.isInt()
                        }
                        maxWidth = 100.0
                        text = "10"
                        font = Font(20.0)
                    }
                    label("max") {
                        paddingLeft = 20
                        paddingRight = 10
                        paddingTop = 5
                        font = Font(20.0)
                    }
                    maxTextField = textfield() {
                        filterInput { change ->
                            !change.isAdded || change.controlNewText.let {
                                it.isInt()
                            }
                        }
                        maxWidth = 100.0
                        text = "20"
                        font = Font(20.0)
                    }
                    isVisible = false
                    schedType.valueProperty().addListener { e ->
                        hboxForAdd.isVisible = (schedType.value == "N-Step SCAN" || schedType.value == "SAATF")
                    }
                }
                gridpane {
                    style {
                        font = Font(15.0)
                    }
                    row {
                        labels.add(label("Execution time"))
                        texts.add(textfield())
                        labels.add(label("Seek time"))
                        texts.add(textfield())
                    }
                    row {
                        labels.add(label("Rotational latency"))
                        texts.add(textfield())
                        labels.add(label("response time"))
                        texts.add(textfield())
                    }
                    row {
                        labels.add(label("standard deviation"))
                        texts.add(textfield())
                        labels.add(label("coefficient of variation"))
                        texts.add(textfield())
                    }
                }
                add(visualization.root)
                add(selectivitiy.root)
                hbox {
                    button("File choose").apply {
                        font = Font(20.0)
                        spacing = 10.0
                        setOnAction {
                            val file = chooseFile(filters = emptyArray())
                            val temp = RequestTokenizer(file[0]).getProcesses()
                            requests.setAll(temp)

                        }
                    }
                    button("Run").apply {
                        font = Font(20.0)

                        setOnAction {
                            val currentTrack = currentTrackTextField.text.toInt()
                            val isDown = when (directionGroup.selectedToggle?.properties?.values.toString()) {
                                "[Down]" -> true
                                else -> false
                            }
                            val sector = sectorTextField.text.toInt()
                            Disk.tracksPerSurfaces = trackTextField.text.toInt()

                            val scheduler = when (schedType.selectedItem) {
                                "FCFS" -> FCFS(requests, currentTrack, sector)
                                "N-Step SCAN" -> NStepSCAN(requests, currentTrack, isDown, nTextField.text.toInt(), sector)
                                "SCAN" -> SCAN(requests, currentTrack, isDown, sector)
                                "SATF" -> SATF(requests, currentTrack, sector)
                                "SAATF" -> SAATF(
                                    requests,
                                    currentTrack,
                                    sector,
                                    minTextField.text.toInt(),
                                    maxTextField.text.toInt()
                                )
                                else -> FCFS(requests, currentTrack, sector)
                            }
                            val (_results, progresses) = scheduler.run()
                            results.setAll(_results)
                            texts[0].text = String.format("%.2f", scheduler.totalExecutionTime)
                            texts[1].text = String.format("%.2f", scheduler.averageSeekTime)
                            texts[2].text = String.format("%.2f", scheduler.averageRotationalLantency)
                            texts[3].text = String.format("%.2f", scheduler.averageResponseTime)
                            texts[4].text = String.format("%.2f", scheduler.standardDeviation)
                            texts[5].text = String.format("%.2f", scheduler.coefficientVariation)
                            visualization.draw(progresses, currentTrack, isLimited.isSelected)
                            selectivitiy.draw(results, schedType.selectedItem!!)
                        }
                    }
                }.apply { paddingAll = 10.0 }
            }

            vbox {
                requestTable = tableview(requests) {
                    readonlyColumn("no", Request::requestNo)
                    readonlyColumn("arrivalTime", Request::arrivalTime).prefWidth(150.0)
                    readonlyColumn("sector", Request::sector).prefWidth(150.0)
                    readonlyColumn("track", Request::track).prefWidth(150.0)
                    readonlyColumn("size", Request::size).prefWidth(150.0)
                    style {
                        font = Font(15.0)
                    }
                    //columnResizePolicy = SmartResize.POLICY
                }
                resultTable = tableview(results) {
                    readonlyColumn("no", Result::requestNo)
                    readonlyColumn("seek", Result::seekTime).cellFormat {
                        text = String.format("%.2f", it)
                    }
                    readonlyColumn("rotational", Result::rotationalLatency).cellFormat {
                        text = String.format("%.2f", it)
                    }
                    readonlyColumn("transmission", Result::transmissionTime).cellFormat {
                        text = String.format("%.2f", it)
                    }
                    readonlyColumn("access", Result::accessTime).cellFormat {
                        text = String.format("%.2f", it)
                    }
                    readonlyColumn("response", Result::responseTime).cellFormat {
                        text = String.format("%.2f", it)
                    }
                    //readonlyColumn("CPU Ratio", ResultProcessInfo::cpuRatio)
                    style {
                        font = Font(15.0)
                    }
                }

                paddingAll = 20.0
                spacing = 10.0
            }

        }
    }
}