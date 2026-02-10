package boozilla.houston.console;

import boozilla.houston.Application;
import lombok.experimental.UtilityClass;

import java.io.PrintWriter;
import java.util.List;

import static boozilla.houston.console.AnsiCodes.*;

@UtilityClass
public class CheckboxRenderer {
    private static final int PAGE_SIZE = 10;

    public int render(final PrintWriter writer, final String title,
                      final List<CheckboxPrompt.Item> items,
                      final CheckboxState state, final int termWidth)
    {
        final var messageSourceAccessor = Application.messageSourceAccessor();
        var totalLines = 0;

        // Title line
        final var selectedCount = state.selectedCount();
        if(selectedCount > 0)
        {
            final var titleFormat = messageSourceAccessor.getMessage("CONSOLE_CHECKBOX_TITLE_SELECTED");
            final var titleText = titleFormat.formatted(title, selectedCount, items.size());
            writer.println(BOLD + "? " + titleText + RESET);
        }
        else
        {
            final var titleFormat = messageSourceAccessor.getMessage("CONSOLE_CHECKBOX_TITLE");
            final var titleText = titleFormat.formatted(title, items.size());
            writer.println(BOLD + "? " + titleText + RESET);
        }
        totalLines++;

        // Page calculation
        final var totalPages = (items.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        final var pageStart = (state.cursor() / PAGE_SIZE) * PAGE_SIZE;
        final var pageEnd = Math.min(pageStart + PAGE_SIZE, items.size());

        // Item rendering
        for(var i = pageStart; i < pageEnd; i++)
        {
            final var item = items.get(i);
            final var isCursor = i == state.cursor();
            final var isSelected = state.isSelected(i);

            final var pointer = isCursor ? BOLD_CYAN + "❯" + RESET : " ";
            final var checkbox = isSelected
                    ? GREEN + "◼" + RESET
                    : DIM + "◻" + RESET;

            final var labelText = isCursor
                    ? CYAN + item.label() + RESET
                    : item.label();

            final var fixedWidth = 4 + item.label().length() + 3;
            final var availableForDesc = termWidth - fixedWidth - 1;

            final String descText;
            if(availableForDesc > 5 && !item.description().isEmpty())
            {
                final var truncated = truncate(item.description(), availableForDesc);
                descText = DIM + " — " + truncated + RESET;
            }
            else
            {
                descText = "";
            }

            writer.println(pointer + " " + checkbox + " " + labelText + descText);
            totalLines++;
        }

        // Page indicator
        if(totalPages > 1)
        {
            final var currentPage = (state.cursor() / PAGE_SIZE) + 1;
            final var pageFormat = messageSourceAccessor.getMessage("CONSOLE_CHECKBOX_PAGE");
            final var pageText = pageFormat.formatted(currentPage, totalPages);
            writer.println(DIM + "  " + pageText + RESET);
            totalLines++;
        }

        // Help line
        writer.println(DIM + "  " + messageSourceAccessor.getMessage("CONSOLE_CHECKBOX_HELP") + RESET);
        totalLines++;

        writer.flush();
        return totalLines;
    }

    public void renderConfirmation(final PrintWriter writer, final String title,
                                    final List<CheckboxPrompt.Item> items,
                                    final List<Integer> selectedIndices)
    {
        final var messageSourceAccessor = Application.messageSourceAccessor();

        if(!selectedIndices.isEmpty())
        {
            final var names = selectedIndices.stream()
                    .map(idx -> items.get(idx).label())
                    .toList();
            writer.println(GREEN + "✔ " + RESET + BOLD + title + RESET
                    + DIM + "  " + String.join(", ", names) + RESET);
        }
        else
        {
            writer.println(DIM + "✘ " + title + "  "
                    + messageSourceAccessor.getMessage("CONSOLE_CHECKBOX_NONE_SELECTED") + RESET);
        }
        writer.flush();
    }

    public void renderCancelled(final PrintWriter writer, final String title)
    {
        final var messageSourceAccessor = Application.messageSourceAccessor();
        writer.println(DIM + "✘ " + title + "  "
                + messageSourceAccessor.getMessage("CONSOLE_CHECKBOX_CANCELLED") + RESET);
        writer.flush();
    }

    public void clearOutput(final PrintWriter writer, final int lines)
    {
        if(lines > 0)
        {
            writer.print(cursorUp(lines));
            for(var i = 0; i < lines; i++)
            {
                writer.print(CLEAR_LINE + "\n");
            }
            writer.print(cursorUp(lines));
        }
        writer.flush();
    }

    private String truncate(final String text, final int maxWidth)
    {
        if(text.length() <= maxWidth)
        {
            return text;
        }

        if(maxWidth <= 1)
        {
            return "\u2026";
        }

        return text.substring(0, maxWidth - 1) + "\u2026";
    }
}
