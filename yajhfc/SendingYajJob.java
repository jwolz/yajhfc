package yajhfc;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.Vector;

public class SendingYajJob extends SentYajJob {

    @Override
    public void delete(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        hyfc.kill(getJob(hyfc));
    }
    
    public SendingYajJob(Vector<FmtItem> cols, String[] stringData) {
        super(cols, stringData);
    }

}
