import kotlin.math.max

// Compiler to transpile DuckyScript to Arduino
// Slightly less spaghetti than before I guess
// Still incredibly janky

class Compiler(private val editor: Editor) {
    private var outputCode: StringBuilder = StringBuilder()
    var defaultDelay = 0

    // Compat while I rewrite in Kotlin
    private val legacyCompiler = LegacyCompiler(editor)
    val lineSeparator = System.getProperty("line.separator")

    // Template for inserting user code
    private val codeTemplate = """
        #include "DigiKeyboard.h"
        void setup() {}
        void loop() {
        DigiKeyboard.sendKeyStroke(0);
        %s
        for(;;){}}
        """.trimIndent()

    // Compiles the code
    // Todo compiler errors
    fun compile(): String {
        val reader = editor.text.reader()
        // Keeps track of last line for repeat command
        var lastLine = ""
        // The line separator needs to be here so repeat doesn't break on the first line
        outputCode.append("// Compiled by DigiDuck").append(lineSeparator)
        for (line in reader.readLines()) {
            println(line)
            // Split line by spaces to examine part
            val splitLine = line.trim().split(" ")
            // Default delay is the ONLY ducky script command with this underscore BS
            if (splitLine[0] == "DEFAULT_DELAY" || splitLine[0] == "DEFAULTDELAY") {
                try {
                    // Change defaultDelay based on passed delay
                    defaultDelay = splitLine[1].toInt()
                    // Make sure delay isn't negative
                    defaultDelay = max(defaultDelay, 0)
                    println("Changed default delay to $defaultDelay")
                } catch (e: NumberFormatException) {
                    println("Default delay must be integer")
                    // Todo throw error
                }
            } else {
                when (splitLine[0]) {
                    "REPEAT" -> {
                        println("Repeating previous line")
                        try {
                            val repeatAmount = splitLine[1].toInt()
                            // Repeat last line specified amount of times
                            for (i in 1..repeatAmount) {
                                outputCode.append(lastLine)
                            }
                            lastLine = ""
                        } catch (e: NumberFormatException) {
                            println("Repeat count must be integer")
                        }
                    }
                    "REM" -> {
                        println("Comment, skipping line")
                    }
                    else -> {
                        // Call old compiler
                        // Todo native implementation
                        outputCode.append(legacyCompiler.parseCommand(splitLine.toTypedArray())).append(lineSeparator)
                        // Append delay if required
                        if (defaultDelay > 0) {
                            outputCode.append(cmd("delay", defaultDelay.toString()))
                                .append(lineSeparator)
                        }
                    }
                }

            }
        }
        // Return generated code inserted into template
        return codeTemplate.format(outputCode.toString())
    }

    private fun cmd(command: String, vararg params: String): String {
        return String.format("DigiKeyboard.%s(%s);", command, params.joinToString(","))
    }

}
