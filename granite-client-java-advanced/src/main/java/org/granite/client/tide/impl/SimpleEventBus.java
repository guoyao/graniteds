/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.tide.impl;

import org.granite.client.tide.Context;
import org.granite.client.tide.EventBus;
import org.granite.client.tide.events.TideEvent;
import org.granite.client.tide.events.TideEventObserver;

/**
 * @author William DRAI
 */
public class SimpleEventBus implements EventBus {
	
    @Override
    public void raiseEvent(Context context, String type, Object... args) {
    	SimpleTideEvent event = new SimpleTideEvent(context, type, args);
    	
    	raiseEvent(event);
    }
    
    protected void raiseEvent(TideEvent event) {
    	TideEventObserver[] observers = event.getContext().allByType(TideEventObserver.class, false);
    	if (observers == null)
    		return;
    	
    	for (TideEventObserver observer : observers)
    		observer.handleEvent(event);
    }

}
