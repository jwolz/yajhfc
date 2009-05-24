package yajhfc.phonebook;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2007 Jonas Wolz
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
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import yajhfc.Utils;
import yajhfc.filters.Filter;
import yajhfc.filters.ui.SearchWin;

public final class NewSearchWin extends SearchWin<PhoneBookEntry,PBEntryField> {
    private NewPhoneBookWin owner;

    @Override
    protected PBEntryField[] getAvailableFields() {
        return PBEntryField.values();
    }

    @Override
    protected void performSearch(
            Filter<PhoneBookEntry, PBEntryField> filter,
            boolean searchBackwards, boolean wrapAroundSearch) {

        int idx, startIdx, pbIdx, pbStartIdx;
        boolean hitEnd = false;
        JTree tree = owner.getPhoneBookTree();
        List<PhoneBook> availPBs = owner.getAvailablePhoneBooks();
        TreePath selPath = tree.getSelectionPath();
        PhoneBook pb;

        if (selPath == null || selPath.getPathCount() <= 1) {
            pbStartIdx = 0;
            startIdx = -1;
        } else {
            pbStartIdx = availPBs.indexOf(selPath.getPathComponent(1));
            if (selPath.getPathCount() == 3) {
                startIdx = ((PhoneBook)selPath.getPathComponent(1)).getEntries().indexOf(selPath.getPathComponent(2));
            } else {
                startIdx = -1;
            }
        }

        pbIdx = pbStartIdx;
        do {
            pb = availPBs.get(pbIdx);
            if ((searchBackwards && startIdx == 0) ||
                    (!searchBackwards && startIdx >= pb.getEntries().size()-1)) {
                idx = -1;
            } else {
                idx = pb.findEntry(startIdx + (searchBackwards ? -1 : 1),
                        searchBackwards,
                        filter);
            }
            if (idx >= 0)
                break;

            pbIdx += (searchBackwards ? -1 : 1);
            if (hitEnd && ((!searchBackwards && pbIdx > pbStartIdx) || (searchBackwards && pbIdx < pbStartIdx)))
                break;

            if (pbIdx >= availPBs.size() || pbIdx < 0) {
                hitEnd = true;
                if (wrapAroundSearch)
                    pbIdx = searchBackwards ? availPBs.size()-1 : 0;
                    else 
                        break;   
            }
            startIdx = (searchBackwards ? availPBs.get(pbIdx).getEntries().size() : -1);
        } while (true);

        if (idx < 0) {
            JOptionPane.showMessageDialog(this, Utils._("No matching phone book entry found."));
        } else {
            selPath = new TreePath(new Object[] { tree.getModel().getRoot(), pb, pb.getEntries().get(idx)});
            tree.setSelectionPath(selPath);
            tree.scrollPathToVisible(selPath);
        }
    }


    public NewSearchWin(NewPhoneBookWin owner) {
        super(owner, Utils._("Find phone book entry"));

        this.owner = owner;
    }
}
