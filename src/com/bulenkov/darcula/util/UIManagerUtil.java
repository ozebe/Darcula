package com.bulenkov.darcula.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.EventObject;

/**
 * @author Konstantin Bulenkov
 */
public class UIManagerUtil {

  public static void showInfo() {
    JFrame frame = new JFrame();
    frame.setSize(500, 800);
    frame.setTitle("Edit LaF Defaults");

    final UIDefaults defaults = UIManager.getDefaults();
    Enumeration keys = defaults.keys();
    final Object[][] data = new Object[defaults.size()][2];
    int i = 0;
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      data[i][0] = key;
      data[i][1] = defaults.get(key);
      i++;
    }

    Arrays.sort(data, new Comparator<Object[]>() {
      @Override
      public int compare(Object[] o1, Object[] o2) {
        return StringUtil.naturalCompare(o1[0].toString(), o2[0].toString());
      }
    });


    JTable table = new JTable(new DefaultTableModel(data, new Object[]{"Name", "Value"}) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return column == 1 && getValueAt(row, column) instanceof Color;
      }
    }) {
      @Override
      public boolean editCellAt(int row, int column, EventObject e) {
        if (isCellEditable(row, column) && e instanceof MouseEvent) {
          final Object color = getValueAt(row, column);
          final Color newColor = JColorChooser.showDialog(null, "Set Color", (Color)color);
          if (newColor != null) {
            final ColorUIResource colorUIResource = new ColorUIResource(newColor);
            final Object key = getValueAt(row, 0);
            UIManager.put(key, colorUIResource);
            setValueAt(colorUIResource, row, column);
          }
        }
        return false;
      }
    };
    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {
        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel label = new JLabel(value == null ? "" : value.toString());
        panel.add(label, BorderLayout.CENTER);
        if (value instanceof Color) {
          final Color c = (Color) value;
          label.setText(String.format("[r=%d,g=%d,b=%d] hex=0x%s", c.getRed(), c.getGreen(), c.getBlue(), ColorUtil.toHex(c)));
          label.setForeground(ColorUtil.isDark(c) ? Color.white : Color.black);
          panel.setBackground(c);
          return panel;
        } else if (value instanceof Icon) {
          try {
            final Icon icon = new IconWrap((Icon) value);
            if (icon.getIconHeight() <= 20) {
              label.setIcon(icon);
            }
            label.setText(String.format("(%dx%d) %s)", icon.getIconWidth(), icon.getIconHeight(), label.getText()));
          } catch (Throwable e1) {//
          }
          return panel;
        } else if (value instanceof Border) {
          try {
            final Insets i = ((Border) value).getBorderInsets(null);
            label.setText(String.format("[%d, %d, %d, %d] %s", i.top, i.left, i.bottom, i.right, label.getText()));
            return panel;
          } catch (Exception ignore) {
          }
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
    });
    final JScrollPane pane = new JScrollPane(table);
    table.setShowGrid(false);
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(pane, BorderLayout.CENTER);

    frame.getContentPane().add(panel);
    frame.setVisible(true);
  }

  private static class IconWrap implements Icon {
    private final Icon myIcon;

    public IconWrap(Icon icon) {
      myIcon = icon;
    }


    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      try {
        myIcon.paintIcon(c, g, x, y);
      } catch (Exception e) {
      }
    }

    @Override
    public int getIconWidth() {
      return myIcon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
      return myIcon.getIconHeight();
    }
  }
}
