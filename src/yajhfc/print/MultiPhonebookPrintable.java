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
package yajhfc.print;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;

import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.ui.PBEntryFieldTableModel;
import yajhfc.print.tableprint.Alignment;
import yajhfc.print.tableprint.TablePrintable;

/**
 * @author jonas
 *
 */
public class MultiPhonebookPrintable extends TablePrintable {

	protected int pbNumber;
	protected int pageOffset;
	protected PhoneBook[] phoneBooks;
	protected PBEntryFieldTableModel model;
	protected int lastPageIndex = -1;
	
	/**
	 * @param model
	 */
	public MultiPhonebookPrintable(PhoneBook[] phoneBooks) {
		super(null);
		this.phoneBooks = phoneBooks;
		setModel(model = new PBEntryFieldTableModel(null));
	}

	@Override
	protected double drawHeaderOrFooter(Graphics2D g2d,
			Map<Alignment, MessageFormat> fmts, Rectangle2D imageableArea,
			int pageIndex, boolean isFooter) {
		return super.drawHeaderOrFooter(g2d, fmts, imageableArea, pageIndex+pageOffset, isFooter);
	}
	
	private void setNewPhonebook() {
		model.setList(Collections.<PBEntryFieldContainer>unmodifiableList(phoneBooks[pbNumber].getEntries()));
		pageHeader.put(Alignment.CENTER, new MessageFormat("'" + phoneBooks[pbNumber] + "'")); 
	}
	
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		if (pageIndex == 0 && lastPageIndex != 0) {
			pbNumber = 0;
			pageOffset = 0;
			setNewPhonebook();
		}
		lastPageIndex = pageIndex;
		
		AffineTransform origTrans = ((Graphics2D)graphics).getTransform();
		int rv = super.print(graphics, pageFormat, pageIndex-pageOffset);
		if (rv == PAGE_EXISTS)
			return PAGE_EXISTS;
		
		pbNumber++;
		if (pbNumber >= phoneBooks.length)
			return NO_SUCH_PAGE;
		
		((Graphics2D)graphics).setTransform(origTrans);
		pageOffset = pageIndex;
		setNewPhonebook();
		graphics.clearRect(0, 0, (int)Math.ceil(pageFormat.getWidth()), (int)Math.ceil(pageFormat.getHeight()));
		return print(graphics, pageFormat, pageIndex);
	}
}
