package de.blu.common.logging;

import lombok.Getter;

@Getter
public enum Color {
    BLACK('0', "38;5;0"),
    DARK_BLUE('1', "38;5;17"),
    DARK_GREEN('2', "38;5;22"),
    DARK_AQUA('3', "38;5;23"),
    DARK_RED('4', "38;5;88"),
    DARK_PURPLE('5', "38;5;55"),
    GOLD('6', "38;5;130"),
    GRAY('7', "38;5;246"),
    DARK_GRAY('8', "38;5;235"),
    BLUE('9', "38;5;26"),
    GREEN('a', "38;5;34"),
    AQUA('b', "38;5;39"),
    RED('c', "38;5;9"),
    LIGHT_PURPLE('d', "38;5;213"),
    YELLOW('e', "38;5;226"),
    WHITE('f', "38;5;255"),
    BRIGHT('l', "1"),
    RESET('r', "0");

    private char colorCode;
    private String loggingColorId;

    Color(char colorCode, String loggingColorId) {
        this.colorCode = colorCode;
        this.loggingColorId = loggingColorId;
    }

    @Override
    public String toString() {
        return "&" + this.getColorCode();
    }
}
