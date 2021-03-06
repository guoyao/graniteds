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
package org.granite.client.tide;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.charset.Charset;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;

import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.server.ExceptionHandler;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.SimpleTideResponder;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;


public abstract class BaseIdentity extends ComponentImpl implements Identity, ExceptionHandler {
	
	private boolean loggedIn;
	private String username;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);


    public BaseIdentity() {
        // proxying
    }

    public BaseIdentity(ServerSession serverSession) {
        super(serverSession);

        loggedIn = false;
    }

	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public void setLoggedIn(boolean loggedIn) {
		if (loggedIn == this.loggedIn)
			return;
		
		this.loggedIn = loggedIn;
		if (loggedIn)
			getServerSession().afterLogin();
		else
			setUsername(null);
		
		pcs.firePropertyChange("loggedIn", !loggedIn, loggedIn);
	}
	
	public String getUsername() {
		return username;
	}
	
	protected void setUsername(String username) {
		String oldUsername = this.username;
		this.username = username;
		
		if ((username == null && oldUsername != null) || (username != null && !username.equals(oldUsername)))
			pcs.firePropertyChange("username", oldUsername, username);
	}

    /**
     * 	Triggers a remote call to check is user is currently logged in
     *  Can be used at application startup to handle browser refresh cases
     * 
     *  @param tideResponder a responder for the remote call
     *  @return future result returning the username if logged in or null
     */
    public Future<String> checkLoggedIn(final TideResponder<String> tideResponder) {
    	return super.call("isLoggedIn", new SimpleTideResponder<String>() {
			@Override
			public void result(TideResultEvent<String> event) {
				if (event.getResult() != null) {
					setUsername(event.getResult());
					setLoggedIn(true);
				}
				else if (isLoggedIn()) {
					setLoggedIn(false);
					
					// Session expired, directly mark the channel as logged out
					getServerSession().sessionExpired();
				}
				
				if (tideResponder != null)
					tideResponder.result(event);
			}
			
			@Override
			public void fault(TideFaultEvent event) {
				if (event.getFault().getCode() == Code.ACCESS_DENIED) {
					// Not in role for the destination
					setLoggedIn(false);
					
					getServerSession().logout(null);
				}
				
				if (tideResponder != null)
					tideResponder.fault(event);
			}
    	});
    }
    
    
    public void login(final String username, String password, final TideResponder<String> tideResponder) {
    	getServerSession().login(username, password);
    	
    	clearSecurityCache();
    	
    	try {
    	    // Force synchronous operation to prevent issues with Spring session fixation protection
    	    // so next remote calls use the correct session id
    	    checkLoggedIn(tideResponder).get();
    	}
    	catch (Exception e) {
    	}
    }

    public void login(final String username, String password, Charset charset, final TideResponder<String> tideResponder) {
    	getServerSession().login(username, password, charset);
    	
    	clearSecurityCache();
    	
    	try {
    	    // Force synchronous operation to prevent issues with Spring session fixation protection
    	    // so next remote calls use the correct session id
    	    checkLoggedIn(tideResponder).get();
    	}
    	catch (Exception e) {
    	}
    }
    
    
    public void logout(final TideResponder<Void> tideResponder) {
    	final Observer observer = new Observer() {
			@SuppressWarnings("unchecked")
			@Override
			public void update(Observable logout, Object event) {
		        setLoggedIn(false);
		        
		        if (tideResponder != null) {
					if (event instanceof TideResultEvent)
				        tideResponder.result((TideResultEvent<Void>)event);
					else if (event instanceof TideFaultEvent)
				        tideResponder.fault((TideFaultEvent)event);
		        }
			}
    	};
    	
    	getServerSession().logout(observer);
    }

    
	@Override
	public boolean accepts(FaultMessage emsg) {
		return emsg.getCode() == Code.NOT_LOGGED_IN;
	}

	@Override
	public void handle(Context context, FaultMessage emsg, TideFaultEvent faultEvent) {
		if (isLoggedIn()) {
			setLoggedIn(false);
			
			// Session expired, directly mark the channel as logged out
			getServerSession().sessionExpired();
		}
	}
	
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}
	
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}
	
    /**
     *  Clear the security cache
     */
    public void clearSecurityCache() {        
    }
}
