package boozilla.houston.console;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AnsiCodes {
    public final boolean ANSI_ENABLED = System.console() != null;

    public final String RESET = "\033[0m";
    public final String BOLD = "\033[1m";
    public final String DIM = "\033[2m";
    public final String CYAN = "\033[36m";
    public final String GREEN = "\033[32m";
    public final String YELLOW = "\033[33m";
    public final String BOLD_CYAN = "\033[1;36m";
    public final String HIDE_CURSOR = "\033[?25l";
    public final String SHOW_CURSOR = "\033[?25h";
    public final String CLEAR_LINE = "\033[2K";

    public String cursorUp(final int lines)
    {
        return lines > 0 ? "\033[" + lines + "A" : "";
    }
}
