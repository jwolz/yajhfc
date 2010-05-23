/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc;

import gnu.hylafax.Job;

import javax.swing.ImageIcon;

/**
 * @author jonas
 *
 */
public enum FaxNotification implements IconMap {
    NEVER(Utils._("Never"), Job.NOTIFY_NONE, Utils.loadCustomIcon("notify_NONE.png")),
    DONE(Utils._("Delivered"), Job.NOTIFY_DONE, Utils.loadCustomIcon("notify_DONE.png")),
    REQUEUE(Utils._("Requeued"), Job.NOTIFY_REQUEUE, Utils.loadCustomIcon("notify_REQUEUE.png")),
    DONE_AND_REQUEUE(Utils._("Delivered or requeued"), Job.NOTIFY_ALL, Utils.loadCustomIcon("notify_ALL.png"))
    ;
    private final String text;
    private final ImageIcon icon;
    private final String type;
    
    
    public String getDescription() {
        return null;
    }

    public ImageIcon getDisplayIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return text;
    }
    
    private FaxNotification(String text, String type, ImageIcon icon) {
        this.icon = icon;
        this.text = text;
        this.type = type;
    }
    
    
}
