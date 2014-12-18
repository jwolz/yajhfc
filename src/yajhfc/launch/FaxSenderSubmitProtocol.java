package yajhfc.launch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.IDAndNameOptions;
import yajhfc.SenderIdentity;
import yajhfc.Utils;
import yajhfc.file.FileFormat;
import yajhfc.file.FileUtils;
import yajhfc.file.FormattedFile;
import yajhfc.file.textextract.FaxnumberExtractor;
import yajhfc.file.textextract.RecipientExtractionMode;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.send.FaxSender;
import yajhfc.send.HylaTFLItem;
import yajhfc.send.LocalFileTFLItem;
import yajhfc.send.StreamTFLItem;
import yajhfc.server.ServerManager;
import yajhfc.server.ServerOptions;

public abstract class FaxSenderSubmitProtocol implements SubmitProtocol {
    protected static final Logger log = Logger.getLogger(FaxSenderSubmitProtocol.class.getName());
    
    protected String subject;
    protected String comments;
    protected String modem;
    protected Boolean useCover;
    protected RecipientExtractionMode extractRecipients;
    protected boolean closeAfterSubmit = false;
    protected final List<String> recipients = new ArrayList<String>();
    protected final List<String> files = new ArrayList<String>();
    protected InputStream inStream;
    protected String streamDesc = null;
    protected StreamTFLItem tflInStream;
    protected String server;
    protected String identity;
    protected boolean preparedSubmit = false;
    
    protected int numExtractedRecipients = 0;

    public FaxSenderSubmitProtocol() {
        super();
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setCover(boolean useCover) {
        this.useCover = useCover;
    }

    public void setExtractRecipients(RecipientExtractionMode extractRecipients) throws IOException {
        this.extractRecipients = extractRecipients;
    }

    public void addFiles(Collection<String> fileNames) {
        this.files.addAll(fileNames);
    }

    public void setInputStream(InputStream stream, String sourceText) {
        this.inStream = stream;
        this.streamDesc = sourceText;
    }

    public void addRecipients(Collection<String> recipients) {
        this.recipients.addAll(recipients);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setModem(String modem) throws IOException {
        this.modem = modem;
    }

    public void setCloseAfterSubmit(boolean closeAfterSumbit) {
        this.closeAfterSubmit = closeAfterSumbit;
    }

    public void setIdentity(String identityToUse) throws IOException {
        this.identity = identityToUse;
    }

    public void setServer(String serverToUse) throws IOException {
        this.server = serverToUse;
    }

    /**
     * Prepares the submit
     */
    public void prepareSubmit() throws IOException {
        if (preparedSubmit)
            return;
    
        if (inStream != null) {
            tflInStream = new StreamTFLItem(inStream, streamDesc);

            if (subject == null) {
                log.fine("No subject specified, trying to extract one from the file...");
                try {
                    final FormattedFile docFile = tflInStream.getPreviewFilename();
                    if (docFile.getFormat() == FileFormat.PostScript) {
                        log.fine("File is PostScript.");
                        String extractedSubject = FileUtils.extractTitleFromPSFile(docFile.file);
                        if (extractedSubject != null) {
                            this.subject = extractedSubject;
                        }
                    }
                } catch (Exception e1) {
                    log.log(Level.WARNING, "Error extracting title from document.", e1);
                }
            }
        }

        if (Utils.debugMode)
            log.fine("Check for extracting recipients: extractRecipients=" + extractRecipients + "; Utils.getFaxOptions().extractRecipients=" + Utils.getFaxOptions().extractRecipients);
        RecipientExtractionMode effExtractRecipients = getEffectiveExtractRecipients();
        
        if ((effExtractRecipients == RecipientExtractionMode.YES)
         || (effExtractRecipients == RecipientExtractionMode.AUTO)) {
            try {
                if (inStream != null) {
                    log.fine("Extracting recipients from stdin");
                    FaxnumberExtractor extractor = new FaxnumberExtractor(FaxnumberExtractor.getDefaultPattern(), FaxnumberExtractor.getDefaultSubjectPattern());
                    Set<String> subjects = new TreeSet<String>();
                    numExtractedRecipients = extractor.extractFromMultipleFiles(Collections.singletonList(tflInStream.getPreviewFilename()), recipients, subjects);
                    if (subjects.size() >= 1) {
                        numExtractedRecipients -= subjects.size(); // Subjects are no recipients
                        subject = subjects.iterator().next(); // Use the first subject found
                    }
                } else if (files.size() > 0) {
                    log.fine("Extracting recipients from input files");
                    FaxnumberExtractor extractor = new FaxnumberExtractor(FaxnumberExtractor.getDefaultPattern(), FaxnumberExtractor.getDefaultSubjectPattern());
                    Set<String> subjects = new TreeSet<String>();
                    numExtractedRecipients = extractor.extractFromMultipleFileNames(files, recipients, subjects);
                    if (subjects.size() >= 1) {
                        numExtractedRecipients -= subjects.size(); // Subjects are no recipients
                        subject = subjects.iterator().next(); // Use the first subject found
                    }
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Error extracting recipients", e);
            }
        }
        
        preparedSubmit = true;
    }
    

    protected RecipientExtractionMode getEffectiveExtractRecipients() {
        RecipientExtractionMode effExtractRecipients = extractRecipients;
        if (effExtractRecipients == null)
        	effExtractRecipients = Utils.getFaxOptions().extractRecipients;
        
        return effExtractRecipients;
    }

    /**
     * Submits the data to the specified FaxSender
     * @param sw
     */
    protected void submitTo(FaxSender sw) {
        if (server != null) {
            ServerOptions so = IDAndNameOptions.getItemFromCommandLineCoding(Utils.getFaxOptions().servers, server);
            if (server != null) {
                sw.setServer(ServerManager.getDefault().getServerByID(so.id));
            } else {
                log.warning("Server not found, using default instead");
            }
        }
        if (identity != null) {
            SenderIdentity si = IDAndNameOptions.getItemFromCommandLineCoding(Utils.getFaxOptions().identities, identity);
            if (identity != null) {
                sw.setIdentity(si);
            } else {
                log.warning("Identity not found, using default instead");
            }
        }
        final Collection<HylaTFLItem> documents = sw.getDocuments();
        if (inStream != null) {                
            documents.add(tflInStream);
        } else {
            for (String fileName : files) {
                documents.add(new LocalFileTFLItem(fileName));
            }
        }
        if (recipients != null && recipients.size() > 0) {
            DefaultPBEntryFieldContainer.parseCmdLineStrings(sw.getRecipients(), recipients);
        }
        if (useCover != null) {
            sw.setUseCover(useCover);
        }
        if (subject != null) {
            sw.setSubject(subject);
        }
        if (comments != null) {
            sw.setComment(comments);
        }
        if (modem != null) {
            sw.setModem(modem);
        }
    }
}