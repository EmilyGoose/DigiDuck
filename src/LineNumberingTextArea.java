import javax.swing.*;
import javax.swing.text.Element;
import java.awt.*;

public class LineNumberingTextArea extends JTextArea {
    private final JTextArea textArea;

    public LineNumberingTextArea(JTextArea textArea) {
        this.textArea = textArea;
        setBorder(BorderFactory.createCompoundBorder(
                textArea.getBorder(),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)
        ));
        setBackground(Color.LIGHT_GRAY);
        setEditable(false);
    }

    public void updateLineNumbers() {
        String lineNumbersText = getLineNumbersText();
        setText(lineNumbersText);
    }

    private String getLineNumbersText() {
        int caretPosition = textArea.getDocument().getLength();
        Element root = textArea.getDocument().getDefaultRootElement();
        StringBuilder lineNumbersTextBuilder = new StringBuilder();
        lineNumbersTextBuilder.append("1").append(System.lineSeparator());

        for (int elementIndex = 2; elementIndex < root.getElementIndex(caretPosition) + 2; elementIndex++) {
            lineNumbersTextBuilder.append(elementIndex).append(System.lineSeparator());
        }

        return lineNumbersTextBuilder.toString();
    }
}