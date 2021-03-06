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
package org.granite.client.tide.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.Producer;
import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.ResultFaultIssuesResponseListener;
import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.TopicAgent;
import org.granite.client.messaging.channel.AMFChannelFactory;
import org.granite.client.messaging.channel.ChannelBuilder;
import org.granite.client.messaging.channel.ChannelFactory;
import org.granite.client.messaging.channel.JMFChannelFactory;
import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.channel.SessionAwareChannel;
import org.granite.client.messaging.channel.UsernamePasswordCredentials;
import org.granite.client.messaging.codec.MessagingCodec.ClientType;
import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IncomingMessageEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportStatusHandler;
import org.granite.client.platform.Platform;
import org.granite.client.tide.ApplicationConfigurable;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.Identity;
import org.granite.client.tide.impl.FaultHandler;
import org.granite.client.tide.impl.ResultHandler;
import org.granite.client.validation.InvalidValue;
import org.granite.config.GraniteConfig;
import org.granite.logging.Logger;
import org.granite.util.ContentType;


/**
 * ServerSession provides an API to manage all communications with the server
 * It can be setup as a managed bean with Spring or CDI or created manually and attached to a Tide context
 *
 * <pre>
 * {@code
 * ServerSession serverSession = tideContext.set(new ServerSession("/myapp", "localhost", 8080));
 * }
 * </pre>
 *
 * @author William DRAI
 */
@ApplicationConfigurable
@Named
public class ServerSession implements ContextAware {

	private static Logger log = Logger.getLogger(ServerSession.class);
	
	public static final String SERVER_TIME_TAG = "org.granite.time";
	public static final String SESSION_ID_TAG = "org.granite.sessionId";
	public static final String SESSION_EXP_TAG = "org.granite.sessionExp";

    public static final String CONTEXT_RESULT = "org.granite.tide.result";
    public static final String CONTEXT_FAULT = "org.granite.tide.fault";
    
	public static final String LOGIN = "org.granite.client.tide.login";
	public static final String LOGOUT = "org.granite.client.tide.logout";
	public static final String SESSION_EXPIRED = "org.granite.client.tide.sessionExpired";


    private ServerApp serverApp;
	private ContentType contentType = ContentType.JMF_AMF;
	private Class<? extends ChannelFactory> channelFactoryClass = null;
    private Transport remotingTransport = null;
    private Transport messagingTransport = null;
    private Map<String, Transport> messagingTransports = new HashMap<String, Transport>();
    
	private Context context = null;

	private Status status = new DefaultStatus();
	
	private String sessionId = null;

	private LogoutState logoutState = new LogoutState();
		
	private String destination = "server";
	private Object platformContext = null;
    private ChannelBuilder defaultChannelBuilder = null;
    private String defaultChannelType = null;
    private ChannelFactory channelFactory;
    private RemotingChannel remotingChannel = null;
    private Map<String, MessagingChannel> messagingChannelsByType = new HashMap<String, MessagingChannel>();
	protected Map<String, RemoteService> remoteServices = new HashMap<String, RemoteService>();
	protected Map<String, TopicAgent> topicAgents = new HashMap<String, TopicAgent>();
	private Set<String> packageNames = new HashSet<String>();
	
	
    public ServerSession() throws Exception {
    	// Used for testing/proxying
    }

    /**
     * Create a server session for the specified context root and server
     * @param contextRoot context root
     * @param serverName server host name
     * @param serverPort server port
     * @throws Exception
     */
    public ServerSession(String contextRoot, String serverName, int serverPort) {
    	this(contextRoot, false, serverName, serverPort);
    }

    /**
     * Create a server session for the specified context root and server
     * @param contextRoot context root
     * @param secure server app is secured (https/wss/...)
     * @param serverName server host name
     * @param serverPort server port
     * @throws Exception
     */
    public ServerSession(String contextRoot, boolean secure, String serverName, int serverPort) {
        this.serverApp = new ServerApp(contextRoot, secure, serverName, serverPort);
    }

    /**
     * Create a server session for the specified server app
     * @param serverApp server application definition
     */
    public ServerSession(ServerApp serverApp) {
        this.serverApp = serverApp;
    }

    /**
     * Serialization type (default is JMF)
     * @return content type
     */
    public ContentType getContentType() {
		return contentType;
	}

    /**
     * Set the serialization type
     * @param contentType serialization type
     */
	public void setContentType(ContentType contentType) {
		if (contentType == null)
			throw new NullPointerException("contentType cannot be null");
		this.contentType = contentType;
	}

    /**
     * Set the default channel builder
     * @param channelBuilder channel builder
     */
    public void setDefaultChannelBuilder(ChannelBuilder channelBuilder) {
        this.defaultChannelBuilder = channelBuilder;
        if (channelFactory != null)
            channelFactory.setDefaultChannelBuilder(defaultChannelBuilder);
    }

    /**
     * Set the default channel type for messaging
     * @param channelType channel type
     */
    public void setDefaultChannelType(String channelType) {
        this.defaultChannelType = channelType;
        if (channelFactory != null)
            channelFactory.setDefaultChannelType(defaultChannelType);
    }

    /**
     * Set a custom channel factory class
     * @param channelFactoryClass channel factory class
     */
	public void setChannelFactoryClass(Class<? extends ChannelFactory> channelFactoryClass) {
	    this.channelFactoryClass = channelFactoryClass;
	}

    /**
     * Set the server app for this server session
     * @param serverApp server application definition
     */
	public void setServerApp(ServerApp serverApp) {
        this.serverApp = serverApp;
    }

    /**
     * Set the Tide context for this server session
     * (internal method, should be set by the context itself)
     * @param context Tide context
     * @see org.granite.client.tide.ContextAware
     */
	public void setContext(Context context) {
		this.context = context;
	}

    /**
     * Current Tide context
     * @return Tide context
     */
	public Context getContext() {
		return this.context;
	}

    /**
     * Set the platform context for this server session
     * @param platformContext
     */
	public void setPlatformContext(Object platformContext) {
	    this.platformContext = platformContext;
	}

    /**
     * Set an implementation of the Status interface to be notified of server related information
     * @param status status
     */
	public void setStatus(Status status) {
		this.status = status;
	}

    /**
     * Status implementation
     * @return status
     */
	public Status getStatus() {
		return status;
	}

    /**
     * Set the remoting transport
     * @param transport remoting transport
     */
	public void setRemotingTransport(Transport transport) {
		this.remotingTransport = transport;
	}

    /**
     * Set the default messaging transport
     * @param transport messaging transport
     */
	public void setMessagingTransport(Transport transport) {
		this.messagingTransport = transport;
	}

    /**
     * Set the messaging transport for the specified channel type
     * @param channelType channel type
     */
    public void setMessagingTransport(String channelType, Transport transport) {
        this.messagingTransports.put(channelType, transport);
    }

    /**
     * Add a package name to scan for remote aliases
     * @param packageName package name
     */
	public void addRemoteAliasPackage(String packageName) {		
		this.packageNames.add(packageName);
	}

    /**
     * Reset all package names to scan for remote aliases
     * @param packageNames package names
     */
	public void setRemoteAliasPackages(Set<String> packageNames) {
		this.packageNames.clear();
		this.packageNames.addAll(packageNames);
	}
	
	private GraniteConfig graniteConfig = null;
	
	public Object convert(Object value, Type expectedType) {
		if (contentType == ContentType.JMF_AMF || graniteConfig == null)
			return value;
		return graniteConfig.getConverters().convert(value, expectedType);
	}

    /**
     * Configure and start the server session
     * @throws Exception
     */
	public void start() throws Exception {
	    if (channelFactory != null)    // Already started
	        return;
	    
	    ClientAliasRegistry aliasRegistry = new ClientAliasRegistry();
	    aliasRegistry.registerAlias(InvalidValue.class);

	    if (channelFactoryClass != null) {
	        Constructor<? extends ChannelFactory> constructor = null;
	        try {
	            constructor = channelFactoryClass.getConstructor(Object.class, Configuration.class);
	            Configuration configuration = Platform.getInstance().newConfiguration();
	            configuration.setClientType(ClientType.JAVA);
	            configuration.load();
	            graniteConfig = configuration.getGraniteConfig();
	            channelFactory = constructor.newInstance(platformContext, configuration);
	        }
	        catch (NoSuchMethodException e) {
	            constructor = channelFactoryClass.getConstructor(Object.class);
                channelFactory = constructor.newInstance(platformContext);
	        }	        
	    }
	    else if (contentType == ContentType.JMF_AMF)
			channelFactory = new JMFChannelFactory(platformContext);
		else {
			Configuration configuration = Platform.getInstance().newConfiguration();
			configuration.setClientType(ClientType.JAVA);
			configuration.load();
			graniteConfig = configuration.getGraniteConfig();
			channelFactory = new AMFChannelFactory(platformContext, configuration);
		}
        channelFactory.setAliasRegistry(aliasRegistry);
		channelFactory.setScanPackageNames(packageNames);

        if (defaultChannelType != null)
            channelFactory.setDefaultChannelType(defaultChannelType);
		
		if (remotingTransport != null) {
			channelFactory.setRemotingTransport(remotingTransport);
            remotingTransport.setStatusHandler(statusHandler);
        }
		if (messagingTransport != null) {
			channelFactory.setMessagingTransport(messagingTransport);
            messagingTransport.setStatusHandler(statusHandler);
        }
        for (Map.Entry<String, Transport> me : messagingTransports.entrySet()) {
            channelFactory.setMessagingTransport(me.getKey(), me.getValue());
            me.getValue().setStatusHandler(statusHandler);
        }
        if (defaultChannelBuilder != null)
            channelFactory.setDefaultChannelBuilder(defaultChannelBuilder);
		
		if (defaultTimeToLive >= 0)
		    channelFactory.setDefaultTimeToLive(defaultTimeToLive);
		
		channelFactory.start();

        remotingChannel = channelFactory.newRemotingChannel("graniteamf", serverApp, 1);

		sessionExpirationTimer = Executors.newSingleThreadScheduledExecutor();
	}

    /**
     * Stop the server session and cleanup resources
     * @throws Exception
     */
	public void stop() throws Exception {
		try {
			if (sessionExpirationFuture != null) {
				sessionExpirationFuture.cancel(false);
				sessionExpirationFuture = null;
			}
			if (sessionExpirationTimer != null) {
				sessionExpirationTimer.shutdownNow();
				sessionExpirationTimer = null;
			}
		}
		finally {
			if (channelFactory != null) {
				channelFactory.stop();
				channelFactory = null;
			}
	            
            remotingChannel = null;
			messagingChannelsByType.clear();
		}
	}

    /**
     * Internal SPI to define how remoting/messaging elements are created
     */
	public static interface ServiceFactory {

        /**
         * Create a remote service for the specified channel and destination
         * @param remotingChannel channel
         * @param destination destination name
         * @return remote service
         */
		public RemoteService newRemoteService(RemotingChannel remotingChannel, String destination);

        /**
         * Create a producer
         * @param messagingChannel channel
         * @param destination destination name
         * @param topic subtopic
         * @return producer
         */
		public Producer newProducer(MessagingChannel messagingChannel, String destination, String topic);

        /**
         * Create a consumer
         * @param messagingChannel channel
         * @param destination destination name
         * @param topic subtopic
         * @return consumer
         */
		public Consumer newConsumer(MessagingChannel messagingChannel, String destination, String topic);
	}
	
	private static class DefaultServiceFactory implements ServiceFactory {
		
		@Override
		public RemoteService newRemoteService(RemotingChannel remotingChannel, String destination) {
			return new RemoteService(remotingChannel, destination);
		}
		
		@Override
		public Producer newProducer(MessagingChannel messagingChannel, String destination, String topic) {
			return new Producer(messagingChannel, destination, topic);
		}
		
		@Override
		public Consumer newConsumer(MessagingChannel messagingChannel, String destination, String topic) {
			return new Consumer(messagingChannel, destination, topic);
		}
	}
	
	private ServiceFactory serviceFactory = new DefaultServiceFactory();

    /**
     * Set a custom service factory to create producer/consumers and remote services
     * @param serviceFactory service factory
     */
	public void setServiceFactory(ServiceFactory serviceFactory) {
		this.serviceFactory = serviceFactory;
	}

    /**
     * Returns remote service for the internal destination
     * Should generally not be used except for very advanced use, use {@link Component} instead
     * @return internal remote service
     */
	public RemoteService getRemoteService() {
		return getRemoteService(destination);
	}

    /**
     * Returns a remote service for the specified destination
     * Should generally not be used except for very advanced use, use {@link Component} instead
     * @param destination destination name
     * @return remote service
     */
	public synchronized RemoteService getRemoteService(String destination) {
		if (remotingChannel == null)
			throw new IllegalStateException("Channel not defined for server session");
		
		RemoteService remoteService = remoteServices.get(destination);
		if (remoteService == null) {
			remoteService = serviceFactory.newRemoteService(remotingChannel, destination);
			remoteServices.put(destination, remoteService);
		}
		return remoteService;
	}

    /**
     * Return a messaging channel for the specified type
     * @param channelType channel type
     * @return messaging channel
     * @see org.granite.client.messaging.channel.ChannelType
     */
    public MessagingChannel getMessagingChannel(String channelType) {
        MessagingChannel messagingChannel = messagingChannelsByType.get(channelType);
        if (messagingChannel != null)
            return messagingChannel;

        messagingChannel = channelFactory.newMessagingChannel(channelType, channelType + "amf", serverApp);
        messagingChannelsByType.put(channelType, messagingChannel);
        return messagingChannel;
    }

    /**
     * Build a consumer for the specified channel type and destination
     * @param destination destination name
     * @param topic subtopic
     * @param channelType channel type
     * @return consumer
     */
	public synchronized Consumer getConsumer(String destination, String topic, String channelType) {
        if (channelType == null)
            channelType = channelFactory.getDefaultChannelType();

        MessagingChannel messagingChannel = getMessagingChannel(channelType);
		if (messagingChannel == null)
			throw new IllegalStateException("Channel not defined in server session for type " + channelType + "");
		
		String key = "C:" + destination + '@' + topic;
		TopicAgent consumer = topicAgents.get(key);
		if (consumer == null) {
			consumer = serviceFactory.newConsumer(messagingChannel, destination, topic);
			topicAgents.put(key, consumer);
		}
		return consumer instanceof Consumer ? (Consumer)consumer : null;
	}

    /**
     * Build a consumer for the default channel type and destination
     * @param destination destination name
     * @param topic subtopic
     * @return consumer
     */
    public synchronized Consumer getConsumer(String destination, String topic) {
        return getConsumer(destination, topic, channelFactory.getDefaultChannelType());
    }

    /**
     * Build a producer for the specified channel type and destination
     * @param destination destination name
     * @param topic subtopic
     * @param channelType channel type
     * @return producer
     */
	public synchronized Producer getProducer(String destination, String topic, String channelType) {
        if (channelType == null)
            channelType = channelFactory.getDefaultChannelType();

        MessagingChannel messagingChannel = getMessagingChannel(channelType);
        if (messagingChannel == null)
			throw new IllegalStateException("Channel not defined for server session");
		
		String key = "P:" + destination + '@' + topic;
		TopicAgent producer = topicAgents.get(key);
		if (producer == null) {
			producer = serviceFactory.newProducer(messagingChannel, destination, topic);
			topicAgents.put(key, producer);
		}
		return producer instanceof Producer ? (Producer)producer : null;
	}

    /**
     * Build a producer for the default channel type and destination
     * @param destination destination name
     * @param topic subtopic
     * @return producer
     */
    public synchronized Producer getProducer(String destination, String topic) {
        return getProducer(destination, topic, channelFactory.getDefaultChannelType());
    }

    /**
     * Current remote session id
     * @return session id
     */
	public String getSessionId() {
		return sessionId;
	}

    /**
     * Is logging out ?
     * @return true if logout in progress
     */
	public boolean isLogoutInProgress() {
		return logoutState.logoutInProgress;
	}

	private ScheduledExecutorService sessionExpirationTimer = null;
	private ScheduledFuture<?> sessionExpirationFuture = null;
	
	private Runnable sessionExpirationTask = new Runnable() {
		@Override
		public void run() {
			Identity identity = context.byType(Identity.class);
			identity.checkLoggedIn(null);
		}
	};
	
	private void rescheduleSessionExpirationTask(long serverTime, int sessionExpirationDelay) {
		Identity identity = context.byType(Identity.class);
		if (identity == null || !identity.isLoggedIn())	// No session expiration tracking if user not logged in
			return;
		
		long clientOffset = serverTime - new Date().getTime();
		sessionExpirationFuture = sessionExpirationTimer.schedule(sessionExpirationTask, clientOffset + sessionExpirationDelay*1000L + 1500L, TimeUnit.MILLISECONDS);
	}

    /**
     * Callback called when a remoting response is received
     * @param event event
     */
	public void onResultEvent(Event event) {
		if (sessionExpirationFuture != null)
			sessionExpirationFuture.cancel(false);
		
		String oldSessionId = sessionId;
		
		if (event instanceof ResultEvent) {
			ResultMessage message = ((ResultEvent)event).getMessage();
			sessionId = (String)message.getHeader(SESSION_ID_TAG);
			if (sessionId != null) {
				long serverTime = (Long)message.getHeader(SERVER_TIME_TAG);
				int sessionExpirationDelay = (Integer)message.getHeader(SESSION_EXP_TAG);
				rescheduleSessionExpirationTask(serverTime, sessionExpirationDelay);
			}
		}
		else if (event instanceof IncomingMessageEvent<?>)
			sessionId = (String)((IncomingMessageEvent<?>)event).getMessage().getHeader(SESSION_ID_TAG);
		
		if (sessionId == null || !sessionId.equals(oldSessionId))
		    log.info("Received new sessionId %s", sessionId);
		
		if (oldSessionId != null || sessionId != null) {
            for (MessagingChannel messagingChannel : messagingChannelsByType.values())
			    messagingChannel.setSessionId(sessionId);
        }
		
		status.setConnected(true);
	}

    /**
     * Callback called when a remoting fault is received
     * @param event fault event
     * @param emsg fault message
     */
	public void onFaultEvent(FaultEvent event, FaultMessage emsg) {
		if (sessionExpirationFuture != null)
			sessionExpirationFuture.cancel(false);
				
        String oldSessionId = sessionId;

		sessionId = (String)event.getMessage().getHeader(SESSION_ID_TAG);
		if (sessionId != null) {
			long serverTime = (Long)event.getMessage().getHeader(SERVER_TIME_TAG);
			int sessionExpirationDelay = (Integer)event.getMessage().getHeader(SESSION_EXP_TAG);
			rescheduleSessionExpirationTask(serverTime, sessionExpirationDelay);
		}
		
        if (sessionId == null || !sessionId.equals(oldSessionId))
            log.info("Received new sessionId %s", sessionId);
        
		if (oldSessionId != null || sessionId != null) {
            for (MessagingChannel messagingChannel : messagingChannelsByType.values())
                messagingChannel.setSessionId(sessionId);
        }
		
        if (emsg != null && emsg.getCode().equals(Code.SERVER_CALL_FAILED))
        	status.setConnected(false);            
	}

    /**
     * Callback called when a remoting failure is received
     * @param event failure event
     */
	public void onIssueEvent(IssueEvent event) {
		if (event.getType() != IssueEvent.Type.CANCELLED)
			status.setConnected(false);            
	}
	
	private final TransportStatusHandler statusHandler = new TransportStatusHandler() {
		
		private int busyCount = 0;
		
		@Override
		public void handleIO(boolean active) {			
			if (active)
				busyCount++;
			else
				busyCount--;
			status.setBusy(busyCount > 0);
			notifyIOListeners(status.isBusy());
		}
		
		@Override
		public void handleException(TransportException e) {
			log.warn(e, "Transport failed");
			notifyExceptionListeners(e);
		}
	};

    /**
     * Current remoting transport
     * @return remoting transport
     */
	public Transport getRemotingTransport() {
		return channelFactory != null ? channelFactory.getRemotingTransport() : null;
	}

    /**
     * Current messaging transport
     * @return messaging transport
     */
	public Transport getMessagingTransport() {
		return channelFactory != null ? channelFactory.getMessagingTransport() : null;
	}
	
	
	/**
	 * 	Implementation of login
     * 	Should not be called directly, called by {@link org.granite.client.tide.Identity#login}
	 * 	
	 * 	@param username user name
	 *  @param password password
	 */
    public void login(String username, String password) {
    	remotingChannel.setCredentials(new UsernamePasswordCredentials(username, password));
        for (MessagingChannel messagingChannel : messagingChannelsByType.values())
    	    messagingChannel.setCredentials(new UsernamePasswordCredentials(username, password));
    }

	/**
	 * 	Implementation of login using a specific charset for username/password encoding
     * 	Should not be called directly, called by {@link org.granite.client.tide.Identity#login}
	 *
	 * 	@param username user name
	 *  @param password password
	 *  @param charset charset used for encoding
	 */
    public void login(String username, String password, Charset charset) {
    	remotingChannel.setCredentials(new UsernamePasswordCredentials(username, password, charset));
        for (MessagingChannel messagingChannel : messagingChannelsByType.values())
    	    messagingChannel.setCredentials(new UsernamePasswordCredentials(username, password, charset));
    }

    /**
     * Called by {@link org.granite.client.tide.Identity} after login has succeeded
     */
    public void afterLogin() {
		log.info("Application session authenticated");
		
		context.getEventBus().raiseEvent(context, LOGIN);
    }

    /**
     * Called by {@link org.granite.client.tide.Identity} after session has expired
     */
    public void sessionExpired() {
		log.info("Application session expired");

        if (remotingChannel.isAuthenticated())
            remotingChannel.logout(false);
        for (MessagingChannel messagingChannel : messagingChannelsByType.values()) {
            if (messagingChannel.isAuthenticated())
                messagingChannel.logout(false);
        }

		sessionId = null;
		if (remotingChannel instanceof SessionAwareChannel)
		    ((SessionAwareChannel)remotingChannel).setSessionId(null);
        for (MessagingChannel messagingChannel : messagingChannelsByType.values())
            messagingChannel.setSessionId(null);
		
		logoutState.sessionExpired();
		
		context.getEventBus().raiseEvent(context, SESSION_EXPIRED);		
		context.getEventBus().raiseEvent(context, LOGOUT);		
    }

	/**
	 * 	Implementation of logout
     * 	Should not be called directly, called by {@link org.granite.client.tide.Identity#logout}
	 * 	
	 * 	@param logoutObserver observer that will be notified of logout result
	 */
	public void logout(final Observer logoutObserver) {
		if (sessionExpirationFuture != null) {
			sessionExpirationFuture.cancel(false);
			sessionExpirationFuture = null;
		}
		
		logoutState.logout(logoutObserver, new TimerTask() {
			@Override
			public void run() {
				log.info("Force session logout");
				logoutState.logout(logoutObserver);
				tryLogout();
			}
		});
		
		context.getEventBus().raiseEvent(context, LOGOUT);
		
        tryLogout();
    }
	
	/**
	 * 	Notify the framework that it should wait for a async operation before effectively logging out.
	 *  Only if a logout has been requested.
	 */
	public void checkWaitForLogout() {
		logoutState.checkWait();
	}
	
	/**
	 * 	Called after all remote operations on a component are finished.
	 *  The actual logout is done when all remote operations on all components have been notified as finished.
	 */
	public void tryLogout() {
		if (logoutState.stillWaiting())
			return;
		
		if (logoutState.isSessionExpired()) {  // Don't remotely logout again if we detected a session expired
		    logoutState.loggedOut(null);
		    return;
		}
		
		if (remotingChannel.isAuthenticated()) {
			remotingChannel.logout(new ResultFaultIssuesResponseListener() {
				@Override
				public void onResult(final ResultEvent event) {
					context.callLater(new Runnable() {
						public void run() {
							log.info("Application session logged out");

                            new ResultHandler<Object>(ServerSession.this, null, "logout").handleResult(context, null, null, null);
							context.getContextManager().destroyContexts();
							
							logoutState.loggedOut(new TideResultEvent<Object>(context, ServerSession.this, null, event.getResult()));
						}
					});
				}
	
				@Override
				public void onFault(final FaultEvent event) {
					context.callLater(new Runnable() {
						public void run() {
							log.error("Could not log out %s", event.getDescription());

                            new FaultHandler<Object>(ServerSession.this, null, "logout").handleFault(context, event.getMessage());

					        Fault fault = new Fault(event.getCode(), event.getDescription(), event.getDetails());
					        fault.setContent(event.getMessage());
					        fault.setCause(event.getCause());				        
							logoutState.loggedOut(new TideFaultEvent(context, ServerSession.this, null, fault, event.getExtended()));
						}
					});
				}
				
				@Override
				public void onIssue(final IssueEvent event) {
					context.callLater(new Runnable() {
						public void run() {
							log.error("Could not logout %s", event.getType());

                            new FaultHandler<Object>(ServerSession.this, null, "logout").handleFault(context, null);
							
					        Fault fault = new Fault(Code.SERVER_CALL_FAILED, event.getType().name(), "");
							logoutState.loggedOut(new TideFaultEvent(context, ServerSession.this, null, fault, null));
						}
					});
				}
			});
		}

        for (MessagingChannel messagingChannel : messagingChannelsByType.values()) {
		    if (messagingChannel != remotingChannel && messagingChannel.isAuthenticated())
			    messagingChannel.logout();
        }
	}


	private static class LogoutState extends Observable {
		
		private boolean logoutInProgress = false;
		private int waitForLogout = 0;
		private boolean sessionExpired = false;
		private Timer logoutTimeout = null;
		
		public synchronized void logout(Observer logoutObserver, TimerTask forceLogout) {
			logout(logoutObserver);
			logoutTimeout = new Timer(true);
			logoutTimeout.schedule(forceLogout, 1000L);
		}
		
		public synchronized void logout(Observer logoutObserver) {
			if (logoutObserver != null)
				addObserver(logoutObserver);
			if (!logoutInProgress) {
		        logoutInProgress = true;
			    waitForLogout = 1;
			}
		}
		
		public synchronized void checkWait() {
			if (logoutInProgress)
				waitForLogout++;
		}
		
		public synchronized boolean stillWaiting() {
			if (sessionExpired)
				return false;
			
			if (!logoutInProgress)
				return true;
			
			waitForLogout--;
			if (waitForLogout > 0)
				return true;
			
			return false;
		}
		
		public boolean isSessionExpired() {
			return sessionExpired;
		}
		
		public synchronized void loggedOut(TideRpcEvent event) {
			if (logoutTimeout != null) {
				logoutTimeout.cancel();
				logoutTimeout = null;
			}
			
			if (event != null) {
    			setChanged();
    			notifyObservers(event);
    			deleteObservers();
			}
			
			logoutInProgress = false;
			waitForLogout = 0;
			sessionExpired = false;
		}
		
		public synchronized void sessionExpired() {
			logoutInProgress = false;
			waitForLogout = 0;
			sessionExpired = true;
		}
	}


    /**
     * Status notified of network related events
     */
	public interface Status {

        /**
         * Network I/O busy
         * @return true is busy
         */
		public boolean isBusy();

        /**
         * Set I/O busy, called by transport listeners
         * @param busy true if busy
         */
		public void setBusy(boolean busy);

        /**
         * Network connected
         * @return true if connected
         */
		public boolean isConnected();

        /**
         * Set connected state, called by transport listeners
         * @param connected true if connected
         */
		public void setConnected(boolean connected);

        /**
         * Busy cursor enabled ?
         * @return true if busy cursor enabled
         */
		public boolean isShowBusyCursor();

        /**
         * Enable/disable busy cursor
         * @param showBusyCursor true if enabled
         */
		public void setShowBusyCursor(boolean showBusyCursor);
	}

	
	public static class DefaultStatus implements Status {
		
		private boolean showBusyCursor = true;
		
		private boolean connected = false;
		private boolean busy = false;

		@Override
		public boolean isBusy() {
			return busy;
		}
		
		public void setBusy(boolean busy) {
			this.busy = busy;
		}

		@Override
		public boolean isConnected() {
			return connected;
		}

		public void setConnected(boolean connected) {
			this.connected = connected;
		}

		@Override
		public boolean isShowBusyCursor() {
			return showBusyCursor;
		}

		@Override
		public void setShowBusyCursor(boolean showBusyCursor) {
			this.showBusyCursor = showBusyCursor;			
		}		
	}
	
	
	private long defaultTimeToLive = -1;

    /**
     * Set default time to live on all channels
     * @param timeToLive time to live in milliseconds
     */
	public void setDefaultTimeToLive(long timeToLive) {
	    defaultTimeToLive = timeToLive;
	    
	    if (channelFactory != null)
	        channelFactory.setDefaultTimeToLive(timeToLive);

        for (MessagingChannel messagingChannel : messagingChannelsByType.values())
            messagingChannel.setDefaultTimeToLive(timeToLive);

	    if (remotingChannel != null)
	        remotingChannel.setDefaultTimeToLive(timeToLive);
	}
	
	private List<TransportIOListener> transportIOListeners = new ArrayList<TransportIOListener>();
	private List<TransportExceptionListener> transportExceptionListeners = new ArrayList<TransportExceptionListener>();
	
	public void addListener(TransportIOListener listener) {
		transportIOListeners.add(listener);
	}
	public void removeListener(TransportIOListener listener) {
		transportIOListeners.remove(listener);
	}
	
	public void addListener(TransportExceptionListener listener) {
		transportExceptionListeners.add(listener);
	}
	public void removeListener(TransportExceptionListener listener) {
		transportExceptionListeners.remove(listener);
	}
	
	public interface TransportIOListener {		
		public void handleIO(boolean busy);
	}
	
	public interface TransportExceptionListener {		
		public void handleException(TransportException e);
	}
	
	public void notifyIOListeners(boolean busy) {
		for (TransportIOListener listener : transportIOListeners)
			listener.handleIO(busy);
	}
	
	public void notifyExceptionListeners(TransportException e) {
		for (TransportExceptionListener listener : transportExceptionListeners)
			listener.handleException(e);
	}
}
