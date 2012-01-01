package yajhfc.send;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import yajhfc.SenderIdentity;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.server.Server;

public interface FaxSender {

    /**
     * Returns true if this dialog is in "poll fax" style
     * @return
     */
    public boolean isPollMode();

    /**
     * Returns a list of the job IDs of the submitted jobs if getModalResult() == true
     * If getModalResult() == false, the result is undefined
     * @return
     */
    public List<Long> getSubmittedJobIDs();

    /**
     * Returns the collection of recipients. 
     * It is at least partially modifiable: The add() method is implemented.
     */
    public Collection<PBEntryFieldContainer> getRecipients();

    /**
     * Returns the collection of documents to send.
     * It is at least partially modifiable: The add() method is implemented.
     */
    public Collection<HylaTFLItem> getDocuments();

    /**
     * Sets the subject of the new fax
     */
    public void setSubject(String subject);

    /**
     * Specifies if a cover page should be used
     * @param useCover
     */
    public void setUseCover(boolean useCover);

    /**
     * Specifies the comment for the new fax
     * @param comment
     */
    public void setComment(String comment);

    /**
     * Sets the modem to send
     * @param modem
     */
    public void setModem(String modem);

    /**
     * Sets the server to use for the new fax
     * @param serverToUse
     * @throws IOException
     */
    public void setServer(Server serverToUse);

    /**
     * Sets the identity to use for the new fax
     * @param identityToUse
     * @throws IOException
     */
    public void setIdentity(SenderIdentity identityToUse);

}