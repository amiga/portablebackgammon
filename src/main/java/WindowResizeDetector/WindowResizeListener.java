/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package WindowResizeDetector;

import java.util.EventListener;

public interface WindowResizeListener extends EventListener {
  void windowResized(WindowResizeEvent e);
}
