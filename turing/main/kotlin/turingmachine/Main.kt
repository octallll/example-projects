package turingmachine

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class TuringMachineImpl : CliktCommand() {
    private val machineFile by argument().file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)

    private val inputFile by argument().file(mustExist = true, canBeDir = false, mustBeReadable = true).optional()

    private val autoMode by option("--auto").flag()

    private val delay by option("--delay").double().default(0.5)

    override fun run() {
        val turingMachine = parseMachineData(machineFile.readText())
        val input = inputFile?.readText() ?: readInputWordFromConsole()

        simulateTuringMachine(turingMachine, input)
    }

    private fun simulateTuringMachine(
        machine: TuringMachine,
        input: String,
    ) {
        val snapshots = machine.simulate(input)

        for (snapshot in snapshots) {
            println(snapshot.toString())

            if (snapshot.state == machine.acceptedState) {
                println("The end: Accepted")
                break
            }

            if (snapshot.state == machine.rejectedState) {
                println("The end: Rejected")
                break
            }

            if (autoMode) {
                runBlocking { delay((delay * 1000).toLong()) }
            } else {
                println("Press Enter to continue...")
                readln()
            }
        }
    }

    private fun readInputWordFromConsole(): String {
        println("Enter input word${System.lineSeparator()}")
        return readLine()?.trim() ?: ""
    }

    fun parseMachineData(fileData: String): TuringMachine {
        var startingState: String? = null
        var acceptedState: String? = null
        var rejectedState: String? = null
        var blank: Char = BLANK
        var transitions: MutableList<TransitionFunction> = mutableListOf()

        for (line in fileData.lines()) {
            val trimLine = line.trim()

            if (trimLine.isEmpty()) {
                continue
            }

            if (line.startsWith("start:", true)) {
                startingState = line.substring("start:".length).trim()
                continue
            }

            if (line.startsWith("accept:", true)) {
                acceptedState = line.substring("accept:".length).trim()
                continue
            }

            if (line.startsWith("reject:", true)) {
                rejectedState = line.substring("reject:".length).trim()
                continue
            }

            if (line.startsWith("blank:", true)) {
                blank = line.substring("blank:".length).trim().first()
                continue
            }

            val parts = line.split(" ")

            require(parts.size == 6) { "Incorrect transition description: $parts" }
            require(parts[1].length == 1) { "Must be one symbol, got ${parts[1]}" }
            require(parts[4].length == 1) { "Must be one symbol, got ${parts[4]}" }

            val state = parts[0]
            val symbol = parts[1].single()
            val newState = parts[3]
            val newSymbol = parts[4].single()
            val move =
                when (parts[5]) {
                    "<" -> TapeTransition.Left
                    ">" -> TapeTransition.Right
                    "^" -> TapeTransition.Stay
                    else -> throw IllegalArgumentException("Invalid move direction: ${parts[5]}")
                }

            transitions.add(TransitionFunction(state, symbol, move, newSymbol, newState))
        }

        require(startingState != null) { "Turing machine must contains starting state" }
        require(acceptedState != null) { "Turing machine must contains accepted state" }
        require(rejectedState != null) { "Turing machine must contains rejected state" }

        BLANK = blank

        return TuringMachine(startingState, acceptedState, rejectedState, transitions)
    }
}

fun main(args: Array<String>) {
    return TuringMachineImpl().main(args)
}
