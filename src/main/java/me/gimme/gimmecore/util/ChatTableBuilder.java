package me.gimme.gimmecore.util;

import org.bukkit.ChatColor;
import org.bukkit.map.MinecraftFont;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatTableBuilder implements TableBuilder {

    private static final int CHAT_WIDTH = 320;
    private static final char PIXEL_CHAR = '\u17f2';
    private static final ChatColor PIXEL_CHAR_COLOR = ChatColor.DARK_GRAY;
    private static final String ELLIPSIS = "...";

    private List<List<String>> table = new ArrayList<>();
    private List<Alignment> colAlignments = new ArrayList<>();
    private List<Integer> colWidths = new ArrayList<>();
    private List<String> rowColors = new ArrayList<>();
    private boolean ellipsize = false;

    /**
     * Fixes automatic column widths (if any) and returns the formatted table as a string.
     *
     * @return the formatted table as a string.
     */
    @Override
    public String build() {
        fixAutomaticColWidths();
        return this.toString();
    }

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
    @Override
    public ChatTableBuilder addCol(@NotNull Alignment alignment, double widthWeight) {
        return addCol(alignment, (int) (CHAT_WIDTH * widthWeight));
    }

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
    @Override
    public ChatTableBuilder addCol(@NotNull Alignment alignment, int width) {
        colAlignments.add(alignment);
        colWidths.add(width);
        return this;
    }

    /**
     * Adds a row to the table.
     * Only as many strings as there are columns will be shown in the table.
     *
     * @param strings the row entry with one string for each column
     * @return this
     */
    @Override
    public ChatTableBuilder addRow(String... strings) {
        return addFormattedRow("", strings);
    }

    /**
     * Adds a row to the table with common formatting.
     * For example, this can be used to add a row of column-titles that are all bold and underlined.
     *
     * @param chatColors the common formatting for the strings (e.g. "" + ChatColor.BOLD + ChatColor.UNDERLINE)
     * @param strings    the row entry with one string for each column
     * @return this
     */
    @Override
    public ChatTableBuilder addFormattedRow(String chatColors, String... strings) {
        table.add(Arrays.asList(strings));
        rowColors.add(chatColors);
        return this;
    }

    /**
     * Sets if overflowing strings should have a trailing ellipsis or just be cut off.
     *
     * @param ellipsize if there should be an ellipsis at the end of overflowing strings
     * @return this
     */
    @Override
    public ChatTableBuilder setEllipsize(boolean ellipsize) {
        this.ellipsize = ellipsize;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < table.size(); row++) {
            List<String> rowText = table.get(row);
            if (!sb.toString().isEmpty()) sb.append("\n");
            for (int col = 0; col < colAlignments.size(); col++) {
                if (col >= rowText.size()) {
                    sb.append(" ");
                    continue;
                }
                String cellText = rowText.get(col);
                sb.append(ChatColor.RESET);
                sb.append(getCell(rowColors.get(row) + cellText, colAlignments.get(col), colWidths.get(col)));
            }
        }
        return sb.toString();
    }

    private String getCell(String text, Alignment alignment, int width) {
        int emptySpace = width;
        int messagePxSize = 0;
        boolean isColorCode = false;
        boolean isBold = false;

        String ellipsizedText = null;
        int ellipsizedTextEmptySpace = 0;
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == ChatColor.COLOR_CHAR) {
                isColorCode = true;
            } else if (isColorCode) {
                if (c == 'l' || c == 'L') isBold = true;
                else if (c == 'r' || c == 'R') isBold = false;
                isColorCode = false;
            } else {
                messagePxSize += isBold ? FontInfo.getBoldWidth(c) : FontInfo.getWidth(c);
            }

            if (ellipsize) {
                int ellipsisLength = FontInfo.getWidth(ELLIPSIS, isBold);

                if (ellipsizedText == null && messagePxSize + ellipsisLength > width) {
                    ellipsizedText = sb.toString();
                    ellipsizedTextEmptySpace = emptySpace;
                }
                if (messagePxSize > width) {
                    sb = new StringBuilder(ellipsizedText);
                    emptySpace = ellipsizedTextEmptySpace;
                    if (sb.toString().length() + ellipsisLength <= width) {
                        sb.append(ELLIPSIS);
                        emptySpace -= ellipsisLength;
                    }
                    break;
                }
            } else if (messagePxSize > width) {
                break;
            }

            sb.append(c);
            emptySpace = width - messagePxSize;
        }
        text = sb.toString();

        if (alignment.equals(Alignment.LEFT)) {
            text = text + getSpacing(emptySpace, false);
        } else if (alignment.equals(Alignment.RIGHT)) {
            text = getSpacing(emptySpace, true) + text;
        } else if (alignment.equals(Alignment.CENTER)) {
            text = getSpacing(emptySpace / 2, true) +
                    text +
                    getSpacing(emptySpace - (emptySpace / 2), false);
        }
        return text;
    }

    private String getSpacing(int pixels, boolean fillerCharToTheRight) {
        if (pixels <= 0) return "";
        int spaces = pixels / FontInfo.SPACE.getWidth();
        int leftover = pixels % FontInfo.SPACE.getWidth();

        return "" + ChatColor.RESET + PIXEL_CHAR_COLOR +
                String.format("%" + (fillerCharToTheRight ? "" : "-") + (spaces + leftover) + "s",
                        new String(new char[leftover]).replaceAll("\0", PIXEL_CHAR + ""))
                + ChatColor.RESET;
    }

    private void fixAutomaticColWidths() {
        for (int i = 0; i < colWidths.size(); i++) {
            int width = colWidths.get(i);
            if (width >= 0) continue;
            int maxWidth = -width;

            int widestInRow = 0;
            for (List<String> row : table) {
                if (row.size() < i + 1) continue;
                String cell = row.get(i);
                int cellWidth = FontInfo.getWidth(cell);
                widestInRow = Math.min(maxWidth, Math.max(widestInRow, cellWidth));
            }
            colWidths.set(i, widestInRow);
        }
    }

    /**
     * Contains info about the default Minecraft font widths.
     * Inspired by: https://www.spigotmc.org/threads/free-code-sending-perfectly-centered-chat-message.95872/
     */
    private enum FontInfo {

        // Exceptions from MinecraftFont
        ASTERISK('*', 3),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        TICK('`', 2),
        OPEN_PARENTHESIS('(', 3),
        CLOSE_PARENTHESIS(')', 3),
        OPEN_CURLY('{', 3),
        CLOSE_CURLY('}', 3),
        SPACE(' ', 3),
        DEFAULT('a', 5); // For everything that is not defined here or in MinecraftFont

        private char character;
        private int length;

        FontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        private int getWidth() {
            return length + 1;
        }

        private static int getWidth(char c) {
            for (FontInfo fontInfo : FontInfo.values()) {
                if (fontInfo.character == c) return fontInfo.getWidth();
            }
            if (MinecraftFont.Font.isValid(c + "")) return MinecraftFont.Font.getWidth(c + "") + 1;
            return FontInfo.DEFAULT.getWidth();
        }

        private static int getBoldWidth(char c) {
            return getWidth(c) + (c == ' ' ? 0 : 1);
        }

        private static int getWidth(String s) {
            return getWidth(s, false);
        }

        private static int getBoldWidth(String s) {
            return getWidth(s, true);
        }

        private static int getWidth(String s, boolean isBold) {
            int result = 0;
            boolean isColorCode = false;
            for (char c : s.toCharArray()) {
                if (c == ChatColor.COLOR_CHAR) {
                    isColorCode = true;
                } else if (isColorCode) {
                    if (c == 'l' || c == 'L') isBold = true;
                    else if (c == 'r' || c == 'R') isBold = false;
                    isColorCode = false;
                } else {
                    result += (isBold ? getBoldWidth(c) : getWidth(c));
                }

            }
            return result;
        }

    }

}
