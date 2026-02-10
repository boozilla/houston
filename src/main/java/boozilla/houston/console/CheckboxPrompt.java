package boozilla.houston.console;

import boozilla.houston.Application;
import lombok.experimental.UtilityClass;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static boozilla.houston.console.AnsiCodes.*;

@UtilityClass
public class CheckboxPrompt {
    private static final int MIN_WIDTH = 40;

    public record Item(String label, String description) {}

    /**
     * 체크박스 선택 UI를 표시하고 사용자가 선택한 항목의 인덱스를 반환합니다.
     *
     * @param title 제목
     * @param items 선택 가능한 항목 목록
     * @return 선택된 항목의 인덱스 리스트 (취소 시 빈 리스트)
     */
    public List<Integer> show(final String title, final List<Item> items)
    {
        if(items.isEmpty())
        {
            return List.of();
        }

        try(final var terminal = TerminalBuilder.builder()
                .system(true)
                .jna(false)
                .jansi(false)
                .build())
        {
            if(terminal.getType() != null && terminal.getType().startsWith(Terminal.TYPE_DUMB))
            {
                return showFallback(terminal, title, items);
            }

            return showInteractive(terminal, title, items);
        }
        catch(final IOException e)
        {
            return List.of();
        }
    }

    private List<Integer> showInteractive(final Terminal terminal, final String title, final List<Item> items)
            throws IOException
    {
        terminal.enterRawMode();
        final var reader = terminal.reader();
        final var writer = terminal.writer();
        final var termWidth = Math.max(terminal.getWidth(), MIN_WIDTH);

        final var state = new CheckboxState(items.size());
        var totalLines = 0;

        writer.print(HIDE_CURSOR);
        writer.flush();

        try
        {
            while(true)
            {
                CheckboxRenderer.clearOutput(writer, totalLines);
                totalLines = CheckboxRenderer.render(writer, title, items, state, termWidth);

                final var key = reader.read();

                if(key == -1 || key == 'q' || key == 'Q')
                {
                    CheckboxRenderer.clearOutput(writer, totalLines);
                    CheckboxRenderer.renderCancelled(writer, title);
                    return List.of();
                }

                if(key == 27) // ESC or arrow sequence
                {
                    final var next = reader.read();
                    if(next == -1 || next != 91)
                    {
                        CheckboxRenderer.clearOutput(writer, totalLines);
                        CheckboxRenderer.renderCancelled(writer, title);
                        return List.of();
                    }

                    final var arrow = reader.read();
                    if(arrow == 65) // ↑
                    {
                        state.moveCursor(-1, items.size());
                    }
                    else if(arrow == 66) // ↓
                    {
                        state.moveCursor(1, items.size());
                    }
                }
                else if(key == 32) // Space
                {
                    state.toggle();
                }
                else if(key == 13 || key == 10) // Enter
                {
                    final var result = state.selectedIndices();

                    CheckboxRenderer.clearOutput(writer, totalLines);
                    CheckboxRenderer.renderConfirmation(writer, title, items, result);

                    return result;
                }
                else if(key == 'a' || key == 'A')
                {
                    state.toggleAll();
                }
            }
        }
        finally
        {
            writer.print(SHOW_CURSOR);
            writer.flush();
        }
    }

    private List<Integer> showFallback(final Terminal terminal, final String title, final List<Item> items)
    {
        final var writer = terminal.writer();
        final var messageSourceAccessor = Application.messageSourceAccessor();
        final var titleFormat = messageSourceAccessor.getMessage("CONSOLE_CHECKBOX_TITLE");

        writer.println(BOLD + "? " + titleFormat.formatted(title, items.size()) + RESET);

        for(var i = 0; i < items.size(); i++)
        {
            final var item = items.get(i);
            final var desc = item.description().isEmpty()
                    ? ""
                    : DIM + " — " + item.description() + RESET;
            writer.println("  " + BOLD + "[" + (i + 1) + "]" + RESET + " " + item.label() + desc);
        }

        writer.println(DIM + "  " + messageSourceAccessor.getMessage("CONSOLE_CHECKBOX_FALLBACK_HELP") + RESET);
        writer.flush();

        try
        {
            System.out.print("  > ");
            System.out.flush();

            final var termReader = terminal.reader();
            final var sb = new StringBuilder();
            int ch;
            while((ch = termReader.read()) != -1)
            {
                if(ch == '\n' || ch == '\r')
                {
                    break;
                }
                sb.append((char) ch);
            }
            final var line = sb.toString().trim();

            if(line.isEmpty())
            {
                CheckboxRenderer.renderCancelled(writer, title);
                return List.of();
            }

            final List<Integer> result;
            if(line.equalsIgnoreCase("a"))
            {
                result = IntStream.range(0, items.size()).boxed().toList();
            }
            else
            {
                result = Arrays.stream(line.split("[,\\s]+"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .flatMap(s -> {
                            try
                            {
                                return Stream.of(Integer.parseInt(s) - 1);
                            }
                            catch(final NumberFormatException e)
                            {
                                return Stream.empty();
                            }
                        })
                        .filter(i -> i >= 0 && i < items.size())
                        .distinct()
                        .sorted()
                        .toList();
            }

            if(result.isEmpty())
            {
                CheckboxRenderer.renderCancelled(writer, title);
            }
            else
            {
                CheckboxRenderer.renderConfirmation(writer, title, items, result);
            }

            return result;
        }
        catch(final Exception e)
        {
            CheckboxRenderer.renderCancelled(writer, title);
            return List.of();
        }
    }
}
