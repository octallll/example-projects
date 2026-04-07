package turingmachine

class TuringMachine(
    val startingState: String,
    val acceptedState: String,
    val rejectedState: String,
    val transitions: Collection<TransitionFunction>,
) {
    class Snapshot(
        val state: String,
        val tape: Tape,
    ) {
        fun applyTransition(transition: Transition): Snapshot {
            return Snapshot(transition.newState, tape.copy().applyTransition(transition.newSymbol, transition.move))
        }

        fun copy(): Snapshot {
            return Snapshot(state, tape)
        }

        override fun equals(other: Any?): Boolean {
            return other is Snapshot && state == other.state && tape == other.tape
        }

        override fun hashCode(): Int {
            var result = state.hashCode()
            result = 31 * result + tape.hashCode()
            return result
        }

        override fun toString(): String {
            val tapeContent = tape.content

            val pointerString = StringBuilder(tapeContent.size)
            repeat(tapeContent.size) {
                pointerString.append(" ")
            }
            pointerString[tape.position] = '^'

            return """
                State: $state
                $tape
                $pointerString
                """.trimIndent()
        }
    }

    class Tape(
        word: String,
    ) {
        private var tapeContent: ArrayDeque<Char> = ArrayDeque()
        private var mainPosition: Int = 0
        private var leftPosToOut: Int = 0
        private var rightPosToOut: Int = 0

        init {
            if (word.isEmpty()) {
                tapeContent.add(BLANK)
                mainPosition = 0
                leftPosToOut = 0
                rightPosToOut = 0
            } else {
                tapeContent.addAll(word.toList())
                mainPosition = 0
                leftPosToOut = 0
                rightPosToOut = word.length - 1
            }
        }

        val content: CharArray
            get() = tapeContent.subList(leftPosToOut, rightPosToOut + 1).toCharArray()

        val position: Int
            get() = mainPosition - leftPosToOut

        fun applyTransition(
            char: Char,
            move: TapeTransition,
        ): Tape {
            tapeContent[mainPosition] = char

            when (move) {
                TapeTransition.Left -> {
                    mainPosition--

                    if (mainPosition < 0) {
                        tapeContent.addFirst(BLANK)
                        mainPosition = 0
                        leftPosToOut = 0

                        if (tapeContent[rightPosToOut + 1] != BLANK) {
                            rightPosToOut++
                        }
                    } else {
                        leftPosToOut = minOf(leftPosToOut, mainPosition)

                        if (mainPosition + 1 == rightPosToOut && tapeContent[mainPosition] != BLANK) {
                            rightPosToOut--
                        }

                        if (rightPosToOut + 1 < tapeContent.size && tapeContent[rightPosToOut + 1] != BLANK) {
                            rightPosToOut++
                        }
                    }
                }

                TapeTransition.Right -> {
                    mainPosition++

                    if (mainPosition >= tapeContent.size) {
                        tapeContent.add(BLANK)
                        rightPosToOut++
                    }

                    if (mainPosition > leftPosToOut && tapeContent[leftPosToOut] == BLANK) {
                        leftPosToOut++
                    }
                }

                TapeTransition.Stay -> {}
            }

            return this
        }

        fun copy(): Tape {
            val result = Tape("")
            result.tapeContent = ArrayDeque(tapeContent)
            result.mainPosition = mainPosition
            result.leftPosToOut = leftPosToOut
            result.rightPosToOut = rightPosToOut
            return result
        }

        override fun equals(other: Any?): Boolean {
            return other is Tape && content.contentEquals(other.content) && position == other.position
        }

        override fun toString(): String {
            return content.joinToString("").replace(BLANK, BLANK)
        }

        override fun hashCode(): Int {
            var result = content.contentHashCode()
            result = 31 * result + position
            return result
        }
    }

    fun initialSnapshot(input: String): Snapshot {
        return Snapshot(startingState, Tape(input))
    }

    fun simulateStep(snapshot: Snapshot): Snapshot {
        val currentSymbol = snapshot.tape.content[snapshot.tape.position]
        val currentState = snapshot.state

        for (possibleTransition in transitions) {
            if (possibleTransition.state == currentState && possibleTransition.symbol == currentSymbol) {
                return Snapshot(
                    possibleTransition.transition.newState,
                    snapshot.tape.copy().applyTransition(
                        possibleTransition.transition.newSymbol,
                        possibleTransition.transition.move,
                    ),
                )
            }
        }

        return Snapshot(rejectedState, snapshot.tape.copy())
    }

    fun simulate(initialString: String): Sequence<Snapshot> {
        return sequence {
            var current = initialSnapshot(initialString)
            yield(current)

            while (current.state != rejectedState && current.state != acceptedState) {
                current = simulateStep(current)
                yield(current)
            }
        }
    }
}
