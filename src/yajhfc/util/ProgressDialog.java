/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
package yajhfc.util;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import yajhfc.Utils;

/**
 * A simple progress dialog
 * @author jonas
 *
 */
public class ProgressDialog extends JDialog {
    public final ProgressContentPane progressPanel = new ProgressContentPane();
    protected JButton cancelButton;
    
    public ProgressDialog(Frame owner, String title, ActionListener cancelAction) {
        super(owner, title);
        initialize(cancelAction);
    }
    
    public ProgressDialog(Dialog owner, String title, ActionListener cancelAction) {
        super(owner, title);
        initialize(cancelAction);
    }
    
    private void initialize(ActionListener cancelAction) {
        JPanel contentPane;
        if (cancelAction != null) {
            contentPane = new JPanel(new BorderLayout());
            contentPane.add(progressPanel, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton cancelButton = new JButton(Utils._("Cancel"));
            cancelButton.addActionListener(cancelAction);
            buttonPanel.add(cancelButton);
            
            contentPane.add(buttonPanel, BorderLayout.SOUTH);
        } else {
            contentPane = progressPanel;
        }
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        setLocationRelativeTo(getOwner());
    }
    
    public JButton getCancelButton() {
        return cancelButton;
    }
}
