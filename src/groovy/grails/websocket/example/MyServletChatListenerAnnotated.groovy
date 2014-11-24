package grails.websocket.example

import grails.converters.JSON
import grails.web.JSONBuilder

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerContainer
import javax.websocket.server.ServerEndpoint

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@WebListener
@ServerEndpoint("/chatroomServerEndpoint")
public class MyServletChatListenerAnnotated implements ServletContextListener {
	
	private final Logger log = LoggerFactory.getLogger(getClass().name)
	
	static final Set<Session> chatroomUsers = ([] as Set).asSynchronized()

    static final List<String> userNames = []

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext = event.servletContext
		final ServerContainer serverContainer = servletContext.getAttribute("javax.websocket.server.ServerContainer")
		try {
			serverContainer.addEndpoint(MyServletChatListenerAnnotated)

			def ctx = servletContext.getAttribute(GA.APPLICATION_CONTEXT)

			def grailsApplication = ctx.grailsApplication

			def config = grailsApplication.config
			int defaultMaxSessionIdleTimeout = config.myservlet.timeout ?: 0
			serverContainer.defaultMaxSessionIdleTimeout = defaultMaxSessionIdleTimeout
		}
		catch (IOException e) {
			log.error e.message, e
		}
	}
	


    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
	
    @OnOpen
	public void handleOpen(Session userSession) { 
		chatroomUsers.add(userSession)
        Map serverMap=[:]
        serverMap.put("list",userNames)
        def serverJSON=serverMap as JSON
        Iterator<Session> iterator=chatroomUsers.iterator()
        while (iterator.hasNext()) iterator.next().getBasicRemote().sendText(serverJSON as String)
        println "new user connected !"
	}
	@OnMessage
	public String handleMessage(String message,Session userSession) throws IOException {
		Map serverMap=[:]
		JSONBuilder jSON = new JSONBuilder ()
        Iterator<Session> iterator=chatroomUsers.iterator()
        Map clientMap = JSON.parse(message)
        if (clientMap.containsKey("username")) {

            userNames.add(clientMap.username)
            userSession.getUserProperties().put("username", clientMap.username)
            serverMap.put("list",userNames)
            serverMap.put("to", "room")
            serverMap.put("message", "${clientMap.username}:connected")
            def serverJSON=serverMap as JSON
            while (iterator.hasNext()) iterator.next().getBasicRemote().sendText(serverJSON as String)

        } else if (clientMap.containsKey("disconnect")) {

            userNames.remove(clientMap.disconnect)
            serverMap.put("list",userNames)
            serverMap.put("to", "room")
            serverMap.put("message", "${clientMap.username}:disconnected")
            def serverJSON=serverMap as JSON
            while (iterator.hasNext()) iterator.next().getBasicRemote().sendText(serverJSON as String)

        } else if (clientMap.containsKey("to")) {

            String username=(String) userSession.getUserProperties().get("username")
            serverMap.put("message", "${username} -> ${clientMap.message}")
            serverMap.put("to", "${clientMap.to}")
            serverMap.put("from", "${clientMap.from}")
            def serverJSON=serverMap as JSON
            if (clientMap.to == "room") {
                while (iterator.hasNext()) iterator.next().getBasicRemote().sendText(serverJSON as String)
            } else {
                for (Session session : chatroomUsers) {
                    if (session.getUserProperties().get("username").equals(clientMap.get("to"))) {
                        session.getBasicRemote().sendText(serverJSON as String)
                    }
                }
            }

        }

        println clientMap

       /*
		String username=(String) userSession.getUserProperties().get("username")
		if (!username) {
			userSession.getUserProperties().put("username", message)
			myMsg.put("message", "System:connected as ==>"+message)
			def aa=myMsg as JSON
			userSession.getBasicRemote().sendText(aa as String)
		}else{
			Iterator<Session> iterator=chatroomUsers.iterator()
			myMsg.put("message", "${username}:${message}")
			def aa=myMsg as JSON
			while (iterator.hasNext()) iterator.next().getBasicRemote().sendText(aa as String)
		}*/
	}
	@OnClose
	public void handeClose(Session userSession) {
        String username=(String) userSession.getUserProperties().get("username")
        userNames.remove(username)
        chatroomUsers.remove(userSession)
        println "${username} : disconnected"
        Map serverMap=[:]
        JSONBuilder jSON = new JSONBuilder ()
        Iterator<Session> iterator=chatroomUsers.iterator()
        serverMap.put("list",userNames)
        serverMap.put("to", "room")
        serverMap.put("message", "${username}:disconnected")
        def serverJSON=serverMap as JSON
        while (iterator.hasNext()) iterator.next().getBasicRemote().sendText(serverJSON as String)

	}
	@OnError
	public void handleError(Throwable t) {
		t.printStackTrace()
	}
	
}
