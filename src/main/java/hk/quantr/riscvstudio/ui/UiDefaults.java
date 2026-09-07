package hk.quantr.riscvstudio.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public final class UiDefaults {

	private UiDefaults() {
	}

	public static Font mono() {
		return new Font(Font.MONOSPACED, Font.PLAIN, 12);
	}

	public static Font ui() {
		Font base = UIManager.getFont("Label.font");
		if (base != null) {
			return base.deriveFont(12f);
		}
		return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	}

	public static Color pcHighlight() {
		return darkTheme() ? new Color(92, 78, 18) : new Color(255, 243, 128);
	}

	public static boolean darkTheme() {
		return Boolean.TRUE.equals(UIManager.getBoolean("nb.dark.theme"));
	}

	public static void configureTable(JTable table) {
		table.setFont(mono());
		table.setRowHeight(20);
		table.setShowGrid(true);
		table.setGridColor(gridColor());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFillsViewportHeight(true);
		table.setRowSelectionAllowed(true);
		table.setIntercellSpacing(new Dimension(1, 1));
		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		header.setFont(ui().deriveFont(Font.BOLD, 11f));
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setFont(mono());
		table.setDefaultRenderer(Object.class, renderer);
	}

	public static void setColumnWidths(JTable table, int... widths) {
		for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
		}
	}

	public static JScrollPane scroll(JComponent component) {
		JScrollPane scrollPane = new JScrollPane(component);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return scrollPane;
	}

	public static TitledBorder title(String text) {
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(4, 6, 0, 6), text);
		border.setTitleFont(ui().deriveFont(Font.BOLD, 11f));
		return border;
	}

	public static Color gridColor() {
		Color color = UIManager.getColor("Table.gridColor");
		return color != null ? color : new Color(210, 210, 210);
	}

	public static TableCellRenderer pcRenderer(int addressColumn, java.util.function.LongSupplier pc) {
		return new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setFont(mono());
				Object addressValue = table.getValueAt(row, addressColumn);
				boolean current = addressValue != null && addressValue.toString().equals(formatHex(pc.getAsLong()));
				if (current) {
					component.setBackground(pcHighlight());
					component.setForeground(table.getForeground());
				} else if (!isSelected) {
					component.setBackground(table.getBackground());
					component.setForeground(table.getForeground());
				}
				if (component instanceof JLabel label) {
					label.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
				}
				return component;
			}
		};
	}

	public static String formatHex(long value) {
		return String.format("0x%08X", value & 0xFFFFFFFFL);
	}

	public static String formatWord(int value) {
		return String.format("0x%08X", value);
	}

	public static String ascii(byte[] memory, int offset, int length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length && offset + i < memory.length; i++) {
			int value = memory[offset + i] & 0xFF;
			builder.append(value >= 32 && value < 127 ? (char) value : '.');
		}
		return builder.toString();
	}
}
