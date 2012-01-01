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
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;

import yajhfc.IDAndNameOptions;
import yajhfc.SenderIdentity;
import yajhfc.Utils;
import yajhfc.file.textextract.RecipientExtractionMode;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.send.LocalFileTFLItem;
import yajhfc.send.SendController;
import yajhfc.send.StreamTFLItem;
import yajhfc.server.Server;
import yajhfc.server.ServerManager;
import yajhfc.server.ServerOptions;

/**
 * @author jonas
 *
 */
public class SendControllerSubmitProtocol implements SubmitProtocol {
	protected SendController sendController;

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#addFiles(java.util.Collection)
	 */
	public void addFiles(Collection<String> fileNames) throws IOException {
        for (String file : fileNames) {
            sendController.getFiles().add(new LocalFileTFLItem(file));
        }
	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#setInputStream(java.io.InputStream, java.lang.String)
	 */
	public void setInputStream(InputStream stream, String sourceText)
			throws IOException {
		sendController.getFiles().add(new StreamTFLItem(stream, sourceText));
	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#addRecipients(java.util.Collection)
	 */
	public void addRecipients(Collection<String> recipients) throws IOException {
		DefaultPBEntryFieldContainer.parseCmdLineStrings(sendController.getNumbers(), recipients);
	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#setCover(boolean)
	 */
	public void setCover(boolean useCover) throws IOException {
		sendController.setUseCover(useCover);
	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#setExtractRecipients(yajhfc.file.textextract.RecipientExtractionMode)
	 */
	public void setExtractRecipients(RecipientExtractionMode extractRecipients)
			throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#setSubject(java.lang.String)
	 */
	public void setSubject(String subject) throws IOException {
		sendController.setSubject(subject);
	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#setComments(java.lang.String)
	 */
	public void setComments(String comments) throws IOException {
		sendController.setComment(comments);
	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#setModem(java.lang.String)
	 */
	public void setModem(String modem) throws IOException {
		sendController.setSelectedModem(modem);
	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#setCloseAfterSubmit(boolean)
	 */
	public void setCloseAfterSubmit(boolean closeAfterSumbit)
			throws IOException, OperationNotSupportedException {
		// Ignore
	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#setServer(java.lang.String)
	 */
	public void setServer(String serverToUse) throws IOException {
		Server server;
		ServerOptions so = IDAndNameOptions.getItemFromCommandLineCoding(Utils.getFaxOptions().servers, serverToUse);
		if (so != null) {
			server = ServerManager.getDefault().getServerByID(so.id);
		} else {
			Logger.getAnonymousLogger().warning("Server not found, using default instead: " + serverToUse);
			server = ServerManager.getDefault().getCurrent(); 
		}
		sendController.setServer(server); //TODO: identity???
	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#setIdentity(java.lang.String)
	 */
	public void setIdentity(String identityToUse) throws IOException {
        SenderIdentity identity = IDAndNameOptions.getItemFromCommandLineCoding(Utils.getFaxOptions().identities, identityToUse);
        if (identity != null) {
            sendController.setIdentity(identity);
        } else {
            Logger.getAnonymousLogger().warning("Identity not found, using default instead: " + identityToUse);
            //sendController.setFromIdentity(server.getDefaultIdentity());
        }
	}

	/* (non-Javadoc)
	 * @see yajhfc.launch.SubmitProtocol#submit(boolean)
	 */
	public long[] submit(boolean wait) throws IOException {
        if (sendController.validateEntries()) {
            sendController.sendFax();
            List<Long> idList = sendController.getSubmittedJobIDs();
            long[] ids = new long[idList.size()];
            for (int i=0; i<ids.length; i++) {
                ids[i] = idList.get(i).longValue();
            }
            return ids;
        } else {
        	return null;
        }
	}

}
