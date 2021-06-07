/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.seek;

import free.jin.I18n;
import javax.swing.JCheckBox;

/**
 * A UI element allowing the user to specify whether he wants to confirm manually the start of a
 * sought game.
 */
public class ManualAcceptSelection {

  /** The checkbox where the user specifies his selection. */
  private final JCheckBox box;

  /** Creates a new <code>ManualAcceptSelection</code> with the specified initial state. */
  public ManualAcceptSelection(boolean isManualAccept) {
    I18n i18n = I18n.get(ManualAcceptSelection.class);

    this.box = i18n.createCheckBox("");
    box.setSelected(!isManualAccept);
  }

  /** Returns the checkbox. */
  public JCheckBox getBox() {
    return box;
  }

  /** Returns whether the user wants to manually confirm the start of a sought game. */
  public boolean isManualAccept() {
    return !box.isSelected();
  }
}
