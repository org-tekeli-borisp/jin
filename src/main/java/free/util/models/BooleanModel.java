/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The utillib library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * <p>The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util.models;

/** A two state, on/off, model. <strong>Note:</strong> This model is not thread-safe. */
public class BooleanModel extends Model implements UnmodifiableBooleanModel {

  /** The current state. */
  private boolean state;

  /** Creates a new <code>BooleanModel</code> with the specified initial state. */
  public BooleanModel(boolean initState) {
    this(null, initState);
  }

  /** Creates a new <code>BooleanModel</code> with the specified name and initial state. */
  public BooleanModel(String name, boolean initState) {
    super(name);

    this.state = initState;
  }

  /** Sets the state. */
  public void set(boolean state) {
    boolean hasChanged = (this.state != state);

    this.state = state;

    if (hasChanged) fireChange();
  }

  /** Returns the current state. */
  @Override
  public boolean get() {
    return state;
  }

  /** Sets the current state to "on". */
  public void setOn() {
    set(true);
  }

  /** Sets the current state to "off". */
  public void setOff() {
    set(false);
  }

  /** Flips the current state, changing "on" to "off" and vice versa. */
  public void flip() {
    set(!state);
  }

  /** Returns whether the model's state is currently "on" (<code>true</code>). */
  @Override
  public boolean isOn() {
    return state;
  }

  /** Returns whether the model's state is currently "off" (<code>false</code>). */
  @Override
  public boolean isOff() {
    return !state;
  }

  /** Adds a listener to changes in this model. */
  @Override
  public void addListener(BooleanListener l) {
    listenerList.add(BooleanListener.class, l);
  }

  /** Removes a listener to changes in this model. */
  @Override
  public void removeListener(BooleanListener l) {
    listenerList.remove(BooleanListener.class, l);
  }

  /** Notifies all registered listeners of a change in this model. */
  private void fireChange() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2) {
      if (listeners[i] == BooleanListener.class)
        ((BooleanListener) listeners[i + 1]).modelChanged(this);
    }
  }
}
