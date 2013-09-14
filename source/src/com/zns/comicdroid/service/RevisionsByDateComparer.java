/*******************************************************************************
 * Copyright (c) 2013 Ulrik Andersson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Ulrik Andersson - initial API and implementation
 ******************************************************************************/
package com.zns.comicdroid.service;

import java.util.Comparator;

import com.google.api.services.drive.model.Revision;

public class RevisionsByDateComparer implements Comparator<Revision> {
	  @Override
	  public int compare(Revision x, Revision y) {
		  if (x != null && y == null)
			  return 1;
		  else if (x == null && y != null)
			  return -1;
		  else if (x == null && y == null)
			  return 0;
		  
		  long xVal = x.getModifiedDate().getValue();
		  long yVal = y.getModifiedDate().getValue(); 
		  if (xVal > yVal)
			  return 1;
		  else if (xVal < yVal)
			  return -1;
		  return 0;
	  }
}
