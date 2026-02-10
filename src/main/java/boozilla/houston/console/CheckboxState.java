package boozilla.houston.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckboxState {
    private final boolean[] selected;
    private int cursor;

    public CheckboxState(final int size)
    {
        this.selected = new boolean[size];
        this.cursor = 0;
    }

    public void toggle()
    {
        selected[cursor] = !selected[cursor];
    }

    public void toggleAll()
    {
        final var allSelected = selectedCount() == selected.length;
        Arrays.fill(selected, !allSelected);
    }

    public void moveCursor(final int delta, final int size)
    {
        cursor = (cursor + delta + size) % size;
    }

    public int cursor()
    {
        return cursor;
    }

    public boolean isSelected(final int index)
    {
        return selected[index];
    }

    public int selectedCount()
    {
        var count = 0;
        for(final var s : selected)
        {
            if(s)
                count++;
        }
        return count;
    }

    public List<Integer> selectedIndices()
    {
        final var result = new ArrayList<Integer>();
        for(var i = 0; i < selected.length; i++)
        {
            if(selected[i])
            {
                result.add(i);
            }
        }
        return result;
    }
}
