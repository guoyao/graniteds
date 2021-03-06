/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.context;

import java.util.Map;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

/**
 * @author Franck WOLFF
 */
public abstract class GraniteContext {

	public static final String SESSION_LAST_ACCESSED_TIME_KEY = "org.granite.session.lastAccessedTime";

    private static ThreadLocal<GraniteContext> instance = new ThreadLocal<GraniteContext>() {
        @Override
        protected GraniteContext initialValue() {
            return (null);
        }
    };

    private final GraniteConfig graniteConfig;
    private final ServicesConfig servicesConfig;
    private final AMFContext amfContext;
    private final String sessionId;
    private final String clientType;

    public GraniteContext(GraniteConfig graniteConfig, ServicesConfig servicesConfig, String sessionId) {
    	this(graniteConfig, servicesConfig, sessionId, null);
    }
    public GraniteContext(GraniteConfig graniteConfig, ServicesConfig servicesConfig, String sessionId, String clientType) {
        this.servicesConfig = servicesConfig;
        this.graniteConfig = graniteConfig;
        this.amfContext = new AMFContextImpl();
        this.sessionId = sessionId;
        this.clientType = clientType != null ? clientType : "as3";
    }

    public static GraniteContext getCurrentInstance() {
        return instance.get();
    }

    protected static void setCurrentInstance(GraniteContext context) {
        instance.set(context);
    }

    public static void release() {
        instance.set(null);
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public GraniteConfig getGraniteConfig() {
        return graniteConfig;
    }

    public AMFContext getAMFContext() {
        return amfContext;
    }
    
    public String getClientType() {
    	return clientType;
    }
    
    public String getSessionId() {
    	return sessionId;
    }
    
    public abstract Object getSessionLock();

    public abstract Map<String, String> getInitialisationMap();
    public abstract Map<String, Object> getApplicationMap();
    public abstract Map<String, Object> getSessionMap();
    public abstract Map<String, Object> getSessionMap(boolean create);
    public abstract Map<String, Object> getRequestMap();
}
