// Compiler to compile DuckyScript to Arduino
// The code is spaghetti, I'll fix it later
// If you don't like it submit a PR

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Compiler {
    StringBuilder outputCode;
    Editor editor;
    int defaultDelay = 0;
    String codeTemplate = "#include \"DigiKeyboard.h\"\n" +
            "void setup() {}\n" +
            "void loop() {\n" +
            "DigiKeyboard.sendKeyStroke(0);\n" +
            "%s" +
            "for(;;){}}";

    Compiler(Editor editor) {
        this.editor = editor;
        this.outputCode = new StringBuilder();
    }

    // Compiles the code
    // Todo compiler errors
    public String compile() {
        BufferedReader reader = new BufferedReader(new StringReader(this.editor.getText()));
        String line;
        // The line separator needs to be here so repeat doesn't break on the first line
        this.outputCode.append("// Compiled by DigiDuck").append(System.getProperty("line.separator"));
        try {
            while ((line = reader.readLine()) != null) {
                // Check if a default delay is present at the beginning of the file
                if ((line.startsWith("DEFAULT_DELAY ") || line.startsWith("DEFAULTDELAY "))) {
                    if (this.outputCode.toString().equals("")) {
                        try {
                            // Todo handle too many arguments passed
                            String delayLength = line.split(" ")[1];
                            this.defaultDelay = Integer.parseInt(delayLength);
                            // Make sure default delay isn't negative
                            this.defaultDelay = Math.max(this.defaultDelay, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Todo throw error
                        }
                    } else {
                        System.out.println("Default delay must be at start of file. Skipping...");
                        // Todo throw error
                    }
                } else if (line.startsWith("REPEAT ")) {
                    System.out.println("Repeating");
                    // Repeat repeats previous line n times, resulting in the line being run n+1 times
                    // Todo throw error for more than one param
                    int repeatNum;
                    try {
                        repeatNum = Integer.parseInt(line.split(" ")[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    // Get second-last line separator (end of the line before the one we want)
                    int lastLineSeparator = this.outputCode.substring(0, this.outputCode.length()-1).lastIndexOf(System.getProperty("line.separator"));
                    // Get the last line of code
                    String lastLine = this.outputCode.substring(lastLineSeparator + 1,this.outputCode.length());
                    this.outputCode.append(String.format("for (int i = 0; i < %d; i++) {", repeatNum));
                    this.outputCode.append(lastLine).append(System.getProperty("line.separator"));
                    this.outputCode.append("}").append(System.getProperty("line.separator"));
                } else if (!line.startsWith("REM")) {
                    String[] command = line.split(" ");
                    this.outputCode.append(this.parseCommand(command)).append(System.getProperty("line.separator"));
                    if (this.defaultDelay > 0) {
                        this.outputCode.append(this.cmd("delay", Integer.toString(this.defaultDelay))).append(System.getProperty("line.separator"));
                    }
                }
            }
            // Return the output code
            return String.format(this.codeTemplate, this.outputCode.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String parseCommand(String[] command) {
        String key = command[0];
        String[] args = Arrays.copyOfRange(command, 1, command.length);
        // Switch statement for special commands
        switch (key) {
            case "DELAY": {
                // Make sure we have only one parameter
                if (args.length == 1) {
                    try {
                        // Make sure the argument is an integer
                        int sleepTime = Integer.parseInt(command[1]);
                        if (sleepTime >= 0 && sleepTime <= 10000) {
                            return this.cmd("delay", command[1]);
                        } // Todo throw error
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Todo throw error
                    }
                } // Todo throw error
            }
            case "STRING": {
                if (args.length >= 1) {
                    // Join the remaining arguments and escape quotes
                    String outputString = String.join(" ", args).replace("\"", "\\\"");
                    // Make sure characters are valid
                    Pattern charPattern = Pattern.compile("[A-Za-z0-9!-)`~+=_\\-\"':;<,>.?/\\\\|]+");
                    if (charPattern.matcher(key).matches()) {
                        return this.cmd("print", String.format("\"%s\"", outputString));
                    }
                } // Todo throw error
            }
            case "APP":
            case "MENU": {
                if (args.length == 0) {
                    // Can't get menu key to work on the digispark board for some reason
                    // SHIFT+F10 should do the same thing in most cases, but not always (e.g. in IntelliJ)
                    return this.cmd("sendKeyStroke", "KEY_F10", "MOD_SHIFT_LEFT");
                }
            }
        }
        if (KeyResolver.isKey(key)) {
            if (KeyResolver.isModifier(key) && args.length == 1 && KeyResolver.isKey(args[0])) {
                // DuckyScript has more constrained parameters for modifiers, not implemented here
                return this.cmd("sendKeyStroke", KeyResolver.getKey(args[0]), KeyResolver.getKey(key));
            } else if (args.length == 0) {
                return this.cmd("sendKeyStroke", KeyResolver.getKey(key));
            }
        }

        System.out.println("Unsupported command. Skipping...");
        return "";
    }

    private String cmd(String command, String... params) {
        return String.format("DigiKeyboard.%s(%s);", command, String.join(",", params));
    }
}
