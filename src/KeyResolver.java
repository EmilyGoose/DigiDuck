// Resolves DuckyScript key codes to what's in DigiKeyboard.h

import java.util.regex.Pattern;

public class KeyResolver {

    // Check if a key exists on the keyboard
    public static boolean isKey(String key) {
        return !KeyResolver.getKey(key).equals("");
    }

    // Check if a key is a modifier
    public static boolean isModifier(String key) {
        return KeyResolver.getKey(key).startsWith("MOD");
    }

    // Get DigiKeyboard key code from DuckyScript key
    public static String getKey(String key) {
        switch (key) {
            case "WINDOWS", "GUI" -> {
                return "MOD_GUI_LEFT";
            }
            case "SHIFT" -> {
                return "MOD_SHIFT_LEFT";
            }
            case "CTRL", "CONTROL" -> {
                return "MOD_CONTROL_LEFT";
            }
            case "ALT" -> {
                return "MOD_ALT_LEFT";
            }
            case "ESC", "ESCAPE" -> {
                return "KEY_ESC";
            }
            case "DOWNARROW", "DOWN" -> {
                return "KEY_ARROW_DOWN";
            }
            case "UPARROW", "UP" -> {
                return "KEY_ARROW_UP";
            }
            case "RIGHTARROW", "RIGHT" -> {
                return "KEY_ARROW_RIGHT";
            }
            case "LEFTARROW", "LEFT" -> {
                return "KEY_ARROW_LEFT";
            }
            // This can probably be rewritten in a less ugly way
            case "ENTER", "BACKSPACE", "TAB", "SPACE", "CAPSLOCK", "DELETE", "END", "HOME", "PRINTSCREEN", "SCROLLLOCK" -> {
                return "KEY_".concat(key);
            }
        }
        // Function keys
        Pattern fKeyPattern = Pattern.compile("F([0-9][0-2]?)");
        if (fKeyPattern.matcher(key).matches()) {
            return "KEY_".concat(key);
        }
        // Alphanumeric
        Pattern alphaPattern = Pattern.compile("[A-Za-z0-9]");
        if (alphaPattern.matcher(key).matches()) {
            return "KEY_".concat(key.toUpperCase());
        }
        return "";
    }
}
