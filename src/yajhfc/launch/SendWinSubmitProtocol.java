/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */
package yajhfc.launch;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import yajhfc.NoGUISender;
import yajhfc.file.textextract.RecipientExtractionMode;
import yajhfc.send.SendController;
import yajhfc.send.SendControllerListener;
import yajhfc.send.SendWinControl;
import yajhfc.server.ServerManager;

/**
 * @author jonas
 *
 */
public class SendWinSubmitProtocol extends FaxSenderSubmitProtocol implements SubmitProtocol, Runnable, SendControllerListener {
    
    protected List<Long> submittedIDs = null;
    private boolean ready = false;
    
    /* (non-Javadoc)
     * @see yajhfc.launch.SubmitProtocol#submitNoWait()
     */
    public long[] submit(boolean wait) throws IOException {
        prepareSubmit();
        
        if (wait) {
            if (SwingUtilities.isEventDispatchThread()) {
                run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(this);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error submitting the fax:", e);
                }
            }
        } else {
            SwingUtilities.invokeLater(this);
        }

        if (submittedIDs == null) {
            return null;
        } else {
            return listToArray(submittedIDs);
        }
    }
    
    private static long[] listToArray(List<Long> list) {
        long[] ids = new long[list.size()];
        for (int i=0; i<ids.length; i++) {
            ids[i] = list.get(i).longValue();
        }
        return ids;
    }

    private void dispatchToSendController() {
        Launcher2.application.bringToFront();
        
        SendController controller = new SendController(ServerManager.getDefault().getCurrent(), Launcher2.application.getFrame(), false);
        if (Launcher2.application instanceof NoGUISender) {
            controller.setProgressMonitor(((NoGUISender)Launcher2.application).progressPanel);
        }
        submitTo(controller);
        controller.addSendControllerListener(this);
        if (controller.validateEntries()) {
            controller.sendFax();
        } else {
            setReady();
        }
    }
    
    private void dispatchToSendWin() {
//      log.fine("Running...");
//      Launcher2.application.bringToFront();

      log.fine("Initializing SendWin");
      SendWinControl sw = SendController.getSendWindow(Launcher2.application.getFrame(), ServerManager.getDefault().getCurrent(), false, true);

      submitTo(sw);
      
      if (Launcher2.application.getFrame().isVisible()) {
          Launcher2.application.getFrame().toFront();
      }
      log.fine("Showing SendWin");
      sw.setVisible(true);
      log.fine("SendWin closed");
      
      if (sw.getModalResult()) {
          submittedIDs = sw.getSubmittedJobIDs();
      } else {
          submittedIDs = null;
      }
      
      if (closeAfterSubmit) {
          Launcher2.application.dispose();
      }
      
      setReady();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {        
        if (numExtractedRecipients > 0 && getEffectiveExtractRecipients() == RecipientExtractionMode.AUTO) {
            dispatchToSendController();
        } else {
            dispatchToSendWin();
        }
    }

    private void setReady() {
        synchronized (this) {
            ready = true;
            notifyAll();
        }
    }
    
    public void waitReady() {
        try {
            synchronized (this) {
                while (!ready) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "Error submitting the fax:", e);
        }
    }

    public void sendOperationComplete(boolean success) {
        setReady();
    }
}
