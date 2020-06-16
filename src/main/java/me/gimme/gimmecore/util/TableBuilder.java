package me.gimme.gimmecore.util;

import org.jetbrains.annotations.NotNull;

public interface TableBuilder {

    enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * Fixes automatic column widths (if any) and returns the formatted table as a string.
     *
     * @return the formatted table as a string.
     */
    String build();

    /**
     * Adds a column with formatting information to the table.
     * <p>
     * The width weight is a proportion of the total chat width and is converted to pixels (rounded down).
     * <p>
     * If the width is a negative number, the column width is automatically fit to the widest string that is not wider
     * than the number in the negative width argument (e.g. for widthWeight = -0.5 the column cannot be wider than
     * half the chat window).
     *
     * @param alignment   the alignment of all cells in the column
     * @param widthWeight the width weight (0-1) of all cells in the column
     * @return this
     */
    ChatTableBuilder addCol(@NotNull Alignment alignment, double widthWeight);

    /**
     * Adds a column with formatting information to the table.
     * <p>
     * If the width is a negative number, the column width is automatically fit to the widest string that is not wider
     * than the number in the negative width argument (e.g. for width = -20 the column cannot be wider than 20 pixels).
     *
     * @param alignment the alignment of all cells in the column
     * @param width     the width in pixels of all cells in the column, or a negative number for automatic fitting
     * @return this
     */
    ChatTableBuilder addCol(@NotNull Alignment alignment, int width);

    /**
     * Adds a row to the table.
     * Only as many strings as there are columns will be shown in the table.
     *
     * @param strings the row entry with one string for each column
     * @return this
     */
    ChatTableBuilder addRow(String... strings);

    /**
     * Adds a row to the table with common formatting.
     * For example, this can be used to add a row of column-titles that are all bold and underlined.
     *
     * @param chatColors the common formatting for the strings (e.g. "" + ChatColor.BOLD + ChatColor.UNDERLINE)
     * @param strings    the row entry with one string for each column
     * @return this
     */
    ChatTableBuilder addFormattedRow(String chatColors, String... strings);

    /**
     * Sets if overflowing strings should have a trailing ellipsis or just be cut off.
     *
     * @param ellipsize if there should be an ellipsis at the end of overflowing strings
     * @return this
     */
    ChatTableBuilder setEllipsize(boolean ellipsize);

}
