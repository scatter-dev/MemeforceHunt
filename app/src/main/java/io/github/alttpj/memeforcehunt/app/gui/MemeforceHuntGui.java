/*
 * Copyright 2020-2020 the ALttPJ Team @ https://github.com/alttpj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.alttpj.memeforcehunt.app.gui;


import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import io.github.alttpj.memeforcehunt.common.value.SpritemapWithSkin;
import io.github.alttpj.memeforcehunt.lib.AlttpRomPatcher;

public class MemeforceHuntGui implements Callable<Integer> {

  private static final String LINK = "https://github.com/alttpj/MemeforceHunt/releases";

  private static final int PER_ROW = 8;

  static final SpritemapWithSkin[] SPRITEMAPS_WITH_SKIN = SpritemapWithSkin.values();
  private JTextField fileName;
  private JLabel preview;
  private JLabel skinsText;
  private JComboBox<SpritemapWithSkin> skins;

  @Override
  public Integer call() throws Exception {
    javax.swing.SwingUtilities.invokeLater(() -> {
      try {
        printGUI();
      } catch (final Exception runEx) {
        runEx.printStackTrace();
      }
    });

    return 0;
  }

  public void printGUI() {
    // try to set LaF
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final Exception e) {
      // do nothing
    } //end System

    final BorderLayout borderLayout = new BorderLayout();
    borderLayout.setHgap(5);
    borderLayout.setVgap(5);
    final JFrame frame = new JFrame();
    frame.setTitle("Memeforce Hunt v" + AlttpRomPatcher.getVersion());
    frame.setLayout(borderLayout);

    /* *************************************
     * left side
     ***************************************/
    final JPanel topBar = createTopBar();
    frame.add(topBar, BorderLayout.NORTH);


    /* *************************************
     * right side
     ***************************************/
    final JPanel rightColumn = createRightColumn(frame);
    frame.add(rightColumn, BorderLayout.EAST);


    /* *************************************
     * center / main / icon chooser
     ***************************************/
    final JPanel iconSelector = createIconSelector();
    frame.add(iconSelector, BorderLayout.CENTER);

    /* *************************************
     * bottom for baseline
     ***************************************/
    final JPanel base = new JPanel();
    frame.add(base, BorderLayout.SOUTH);


    /* *************************************
     * window stuff
     ***************************************/
    // ico
    BufferedImage ico;
    try {
      ico = ImageIO.read(SpritemapWithSkin.class.getResourceAsStream("/triforce piece.png"));
    } catch (final IOException e) {
      ico = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);
    }

    frame.setIconImage(ico);

    // frame setting
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationByPlatform(true);
    frame.setResizable(true);
    frame.pack();
    frame.setSize(640, 544);

    frame.setVisible(true);
  }

  /**
   * Create the main icon selector.
   *
   * <p>It is just a little odd how this works.<br>
   * First there is a hidden combo box. It is hidden, but used for the internal selection
   * which item is currently selected. Kind of a 'state holder'.<br>
   * It is also used as a indexed list for the random button.</p>
   */
  private JPanel createIconSelector() {
    final JPanel iconGrid = new JPanel(new GridBagLayout());
    iconGrid.setBorder(SwingAppConstants.PADDING);
    final GridBagConstraints iconListGridConstraints = new GridBagConstraints();

    iconListGridConstraints.fill = GridBagConstraints.HORIZONTAL;
    iconListGridConstraints.gridy = 0;
    iconListGridConstraints.gridx = 0;

    /* *************************************
     * model (data only)
     ***************************************/
    this.skins = new JComboBox<>(SpritemapWithSkin.values());
    this.skins.setEditable(false);

    for (final SpritemapWithSkin spritemapWithSkin : SPRITEMAPS_WITH_SKIN) {
      final SkinButton sb = new SkinButton(spritemapWithSkin);
      sb.addActionListener(arg0 -> this.skins.setSelectedItem(sb.getSpritemapWithSkin()));
      iconGrid.add(sb, iconListGridConstraints);
      sb.setToolTipText("Use Skin \"" + spritemapWithSkin.getName() + "\".");
      iconListGridConstraints.gridx++;
      if (iconListGridConstraints.gridx == PER_ROW) {
        iconListGridConstraints.gridx = 0;
        iconListGridConstraints.gridy++;
      }
    }

    this.skins.addItemListener(
        (ItemEvent itemEvent) -> {
          final SpritemapWithSkin sel = (SpritemapWithSkin) this.skins.getSelectedItem();
          this.preview.setIcon(sel.getImageIcon());
          this.skinsText.setText(sel.toString());
        });

    return iconGrid;
  }

  private JPanel createRightColumn(final JFrame parent) {
    final GridBagLayout gridBagLayout = new GridBagLayout();
    final JPanel rightColumn = new JPanel(gridBagLayout);
    rightColumn.setBorder(SwingAppConstants.PADDING);

    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.ipadx = 10;
    gbc.ipady = 10;
    gbc.anchor = GridBagConstraints.NORTH;
    gridBagLayout.rowWeights = new double[] {
        // skintext, preview
        0.0, 0.0,
        // spacer 1
        0.2,
        // patch, random
        0.0, 0.0,
        // spacer 2+3
        1.0, 1.0, 1.0,
        // update
        0.0
    };
    gbc.weighty = 0.5;

    // skin text
    this.skinsText = new JLabel("---");
    this.skinsText.setHorizontalAlignment(SwingConstants.CENTER);
    this.skinsText.setText(SpritemapWithSkin.BENANA.toString());
    rightColumn.add(this.skinsText, gbc);
    gbc.gridy++;

    // preview
    this.preview = new JLabel();
    this.preview.setHorizontalAlignment(SwingConstants.CENTER);
    this.preview.setIcon(SpritemapWithSkin.BENANA.getImageIcon());
    rightColumn.add(this.preview, gbc);
    gbc.gridy++;

    // spacer
    rightColumn.add(new JLabel(" "), gbc);
    gbc.gridy++;

    // patch button
    final JButton doPatch = new JButton("Patch");
    rightColumn.add(doPatch, gbc);
    gbc.gridy++;

    doPatch.addActionListener(
        arg0 -> {
          final String fileNameText = this.fileName.getText();
          try {
            AlttpRomPatcher.patchROM(fileNameText, (SpritemapWithSkin) this.skins.getSelectedItem());
          } catch (final Exception e) {
            JOptionPane.showMessageDialog(parent,
                "Something went wrong: [" + e.getMessage() + "].",
                "PROBLEM",
                JOptionPane.WARNING_MESSAGE,
                SpritemapWithSkin.SCREAM.getImageIcon());
            return;
          }
          JOptionPane.showMessageDialog(parent,
              "SUCCESS",
              "Enjoy",
              JOptionPane.PLAIN_MESSAGE,
              SpritemapWithSkin.BENANA.getImageIcon());
        });

    // random patch button
    final JButton doRandom = new JButton("Surprise me");
    rightColumn.add(doRandom, gbc);
    gbc.gridy++;

    doRandom.addActionListener(
        arg0 -> {
          final String fileNameText = this.fileName.getText();
          final int r = (int) (Math.random() * SPRITEMAPS_WITH_SKIN.length);

          try {
            AlttpRomPatcher.patchROM(fileNameText, SPRITEMAPS_WITH_SKIN[r]);
          } catch (final Exception patchException) {
            JOptionPane.showMessageDialog(parent,
                "Something went wrong.\n\n"
                    + "Error Message:\n"
                    + "" + patchException.getMessage(),
                "PROBLEM",
                JOptionPane.WARNING_MESSAGE,
                SpritemapWithSkin.SCREAM.getImageIcon());
            return;
          }
          JOptionPane.showMessageDialog(parent,
              "SUCCESS",
              "Enjoy",
              JOptionPane.PLAIN_MESSAGE,
              SpritemapWithSkin.BENANA.getImageIcon());
        });

    rightColumn.add(new JLabel(""), gbc);
    gbc.gridy++;
    rightColumn.add(new JLabel(""), gbc);
    gbc.gridy++;
    rightColumn.add(new JLabel(""), gbc);
    gbc.gridy++;

    // update checks
    final JButton update = new JButton("Check for updates");
    rightColumn.add(update, gbc);

    update.addActionListener(
        (ActionEvent actionEvent) -> {
          final URL aa;
          try {
            aa = new URL(LINK);
            final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
              desktop.browse(aa.toURI());
            }
          } catch (final Exception e) {
            JOptionPane.showMessageDialog(parent,
                "uhhh",
                "Houston, we have a problem.",
                JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
          }
        });

    return rightColumn;
  }

  private JPanel createTopBar() {
    final JPanel topBar = new JPanel();
    topBar.setBorder(SwingAppConstants.PADDING);
    topBar.setLayout(new BoxLayout(topBar, BoxLayout.LINE_AXIS));

    // rom name
    this.fileName = new JTextField("");
    topBar.add(this.fileName);

    topBar.add(Box.createRigidArea(new Dimension(10, 0)));

    // file chooser
    final JButton find = new JButton("Open…");
    topBar.add(find);

    // file explorer
    final FileDialog explorer = new java.awt.FileDialog((java.awt.Frame) null);
    final FilenameFilter filenameFilter = (dir, name) -> name.endsWith("sfc");
    explorer.setFilenameFilter(filenameFilter);

    // load sprite file
    find.addActionListener(
        arg0 -> {
          //explorer.setSelectedFile(EEE);
          explorer.setMode(FileDialog.LOAD);
          explorer.setVisible(true);

          // read the file
          final String n = explorer.getFile();

          if (n == null) {
            return;
          }

          if (n.endsWith(".sfc")) {
            this.fileName.setText(n);
          }
        });

    return topBar;
  }
}