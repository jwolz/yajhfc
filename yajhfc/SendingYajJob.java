package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import gnu.hylafax.HylaFAXClient;
import gnu.hylafax.Job;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;

public class SendingYajJob extends SentYajJob {

    @Override
    public void delete(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        synchronized (hyfc) {
            Job job = getJob(hyfc);
        //  hyfc.suspend(job);
            hyfc.kill(job);
        }
    }
    
    public void suspend(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        synchronized (hyfc) {
            Job job = getJob(hyfc);

            hyfc.suspend(job);
        }
    }
    
    public void resume(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        synchronized (hyfc) {
            Job job = getJob(hyfc);
            hyfc.submit(job);
        }
    }
    
    public SendingYajJob(FmtItemList cols, String[] stringData) {
        super(cols, stringData);
    }

}
