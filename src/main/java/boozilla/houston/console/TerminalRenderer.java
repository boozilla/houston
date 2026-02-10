package boozilla.houston.console;

import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class TerminalRenderer {
    private static final String RESET = AnsiCodes.ANSI_ENABLED ? AnsiCodes.RESET : "";
    private static final String BOLD = AnsiCodes.ANSI_ENABLED ? AnsiCodes.BOLD : "";
    private static final String CYAN = AnsiCodes.ANSI_ENABLED ? AnsiCodes.CYAN : "";
    private static final String YELLOW = AnsiCodes.ANSI_ENABLED ? AnsiCodes.YELLOW : "";

    private static final Pattern HEADER_PATTERN = Pattern.compile("^##\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern CODE_PATTERN = Pattern.compile("`([^`]+)`");
    private static final Pattern BULLET_PATTERN = Pattern.compile("^[*-]\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern QUOTE_PATTERN = Pattern.compile("^>\\s?(.*)$", Pattern.MULTILINE);
    private static final Pattern BLANK_LINES_PATTERN = Pattern.compile("\n{3,}");

    public String render(final String message)
    {
        if(message == null || message.isBlank())
        {
            return "";
        }

        var result = renderTables(message);
        result = cleanHtmlTags(result);
        result = renderMarkdown(result);
        result = cleanup(result);

        return result;
    }

    private String renderTables(final String message)
    {
        if(!message.contains("<table"))
        {
            return message;
        }

        final Document doc = Jsoup.parseBodyFragment(message);
        final Elements tables = doc.select("table");

        if(tables.isEmpty())
        {
            return message;
        }

        var result = message;

        for(final Element table : tables)
        {
            final var rendered = renderTable(table);
            result = result.replace(table.outerHtml(), rendered);
        }

        return result;
    }

    private String renderTable(final Element table)
    {
        final var rows = table.select("tr");
        if(rows.isEmpty())
        {
            return "";
        }

        final List<List<String[]>> parsedRows = new ArrayList<>();
        boolean hasHeader = false;
        int colCount = 0;

        for(final Element row : rows)
        {
            final Elements cells = row.select("th, td");
            if(!cells.isEmpty() && row.select("th").size() == cells.size())
            {
                hasHeader = true;
            }

            final List<String[]> rowCells = new ArrayList<>();
            for(final Element cell : cells)
            {
                var html = cell.html();
                html = html.replaceAll("<br\\s*/?>", "\n");
                html = Jsoup.parse(html).text();
                html = html.strip();
                final String[] lines = html.split("\n");
                rowCells.add(lines);
            }

            colCount = Math.max(colCount, rowCells.size());
            parsedRows.add(rowCells);
        }

        if(colCount == 0)
        {
            return "";
        }

        // Calculate column widths
        final int[] colWidths = new int[colCount];
        for(final List<String[]> row : parsedRows)
        {
            for(int col = 0; col < row.size(); col++)
            {
                for(final String line : row.get(col))
                {
                    colWidths[col] = Math.max(colWidths[col], displayWidth(line));
                }
            }
        }

        final var sb = new StringBuilder();

        // Top border
        sb.append(horizontalLine('┌', '┬', '┐', '─', colWidths)).append('\n');

        for(int rowIdx = 0; rowIdx < parsedRows.size(); rowIdx++)
        {
            final var row = parsedRows.get(rowIdx);

            // Calculate max lines in this row
            int maxLines = 1;
            for(final String[] cell : row)
            {
                maxLines = Math.max(maxLines, cell.length);
            }

            // Render each line of the row
            for(int line = 0; line < maxLines; line++)
            {
                sb.append('│');
                for(int col = 0; col < colCount; col++)
                {
                    final String content;
                    if(col < row.size() && line < row.get(col).length)
                    {
                        content = row.get(col)[line].strip();
                    }
                    else
                    {
                        content = "";
                    }
                    sb.append(' ');
                    sb.append(content);
                    sb.append(" ".repeat(Math.max(0, colWidths[col] - displayWidth(content))));
                    sb.append(" │");
                }
                sb.append('\n');
            }

            // Separator after header row (double line)
            if(rowIdx == 0 && hasHeader)
            {
                sb.append(horizontalLine('╞', '╪', '╡', '═', colWidths)).append('\n');
            }
            // Separator between data rows
            else if(rowIdx < parsedRows.size() - 1)
            {
                sb.append(horizontalLine('├', '┼', '┤', '─', colWidths)).append('\n');
            }
        }

        // Bottom border
        sb.append(horizontalLine('└', '┴', '┘', '─', colWidths));

        return sb.toString();
    }

    private String horizontalLine(final char left, final char cross, final char right,
                                  final char fill, final int[] colWidths)
    {
        final var sb = new StringBuilder();
        sb.append(left);
        for(int i = 0; i < colWidths.length; i++)
        {
            sb.append(String.valueOf(fill).repeat(colWidths[i] + 2));
            if(i < colWidths.length - 1)
            {
                sb.append(cross);
            }
        }
        sb.append(right);
        return sb.toString();
    }

    private int displayWidth(final String text)
    {
        int width = 0;
        for(int i = 0; i < text.length(); )
        {
            final int codePoint = text.codePointAt(i);
            if(isCjk(codePoint))
            {
                width += 2;
            }
            else
            {
                width += 1;
            }
            i += Character.charCount(codePoint);
        }
        return width;
    }

    private boolean isCjk(final int codePoint)
    {
        return Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.HANGUL_SYLLABLES
                || Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.HANGUL_JAMO
                || Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.KATAKANA
                || Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.HIRAGANA
                || (codePoint >= 0xFF01 && codePoint <= 0xFF60)
                || (codePoint >= 0xFFE0 && codePoint <= 0xFFE6);
    }

    private String cleanHtmlTags(final String text)
    {
        var result = text;
        result = result.replaceAll("<br\\s*/?>", "\n");
        result = result.replaceAll("<[^>]+>", "");
        return result;
    }

    private String renderMarkdown(final String text)
    {
        var result = text;

        result = HEADER_PATTERN.matcher(result).replaceAll(match ->
                "  " + BOLD + CYAN + match.group(1) + RESET);

        result = BOLD_PATTERN.matcher(result).replaceAll(match ->
                BOLD + match.group(1) + RESET);

        result = CODE_PATTERN.matcher(result).replaceAll(match ->
                YELLOW + match.group(1) + RESET);

        result = BULLET_PATTERN.matcher(result).replaceAll(match ->
                "  - " + match.group(1));

        result = QUOTE_PATTERN.matcher(result).replaceAll(match ->
                "  | " + match.group(1));

        return result;
    }

    private String cleanup(final String text)
    {
        var result = BLANK_LINES_PATTERN.matcher(text).replaceAll("\n\n");
        result = result.lines()
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"));
        return result.strip();
    }
}
