/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package WindowResizeDetector;

/**
 *
 * @author Gaz
 */

import java.awt.Dimension;
import java.util.EventObject;

public class WindowResizeEvent extends EventObject {
  private Dimension oldSize;
  private Dimension newSize;

  public WindowResizeEvent(Object source, Dimension oldSize, Dimension newSize) {
    super(source);
    this.oldSize = oldSize;
    this.newSize = newSize;
  }

  public Dimension getOldSize() {
  return oldSize;
  }

  public Dimension getNewSize() {
  return newSize;
  }
}
