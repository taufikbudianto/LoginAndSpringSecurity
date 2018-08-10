package taufik.app.web.scope;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * @author m.nikolaev Date: 21.11.12 Time: 0:37
 * https://github.com/michail-nikolaev/primefaces-spring-scopes/blob/master/primefaces-scopes-test/src/main/java/org/nkey/primefaces/scopes/test/spring/scope/ViewScope.java
 * http://stackoverflow.com/questions/6025998/difference-between-view-and-request-scope-in-managed-beans
 */
public class ViewScope implements Scope, Serializable, HttpSessionBindingListener {
    
    private final WeakHashMap<HttpSession, Set<ViewScopeViewMapListener>> sessionToListeners = new WeakHashMap<>();

    @Override
    public Object get(String name, ObjectFactory objectFactory) {
        Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (viewMap) {
            if (viewMap.containsKey(name)) {
                return viewMap.get(name);
            } else {
                Object object = objectFactory.getObject();
                viewMap.put(name, object);

                return object;
            }
        }
    }

    @Override
    public Object remove(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConversationId() {
        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
        ViewScopeViewMapListener listener
                = new ViewScopeViewMapListener(viewRoot, name, callback, this);

        viewRoot.subscribeToViewEvent(PreDestroyViewMapEvent.class, listener);

        HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        final Set<ViewScopeViewMapListener> sessionListeners;
        synchronized (sessionToListeners) {
            if (!sessionToListeners.containsKey(httpSession)) {
                sessionToListeners.put(httpSession, new HashSet<>());
            }
            sessionListeners = sessionToListeners.get(httpSession);
        }
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (sessionListeners) {
            Set<ViewScopeViewMapListener> toRemove = new HashSet<>();
            for (ViewScopeViewMapListener viewMapListener : sessionListeners) {
                if (viewMapListener.checkRoot()) {
                    toRemove.add(viewMapListener);
                }
            }
            sessionListeners.removeAll(toRemove);
            sessionListeners.add(listener);
        }
        if (!FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("sessionBindingListener")) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("sessionBindingListener", this);
        }

    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        final Set<ViewScopeViewMapListener> listeners;
        synchronized (sessionToListeners) {
            if (sessionToListeners.containsKey(event.getSession())) {
                listeners = sessionToListeners.get(event.getSession());
                sessionToListeners.remove(event.getSession());
            } else {
                listeners = null;
            }
        }
        if (listeners != null) {
            for (ViewScopeViewMapListener listener : listeners) {
                listener.doCallback();
            }
        }
    }

    public void clearFromListener(ViewScopeViewMapListener listener) {
        HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (httpSession != null) {
            synchronized (sessionToListeners) {
                if (sessionToListeners.containsKey(httpSession)) {
                    sessionToListeners.get(httpSession).remove(listener);
                }
            }
        }
    }

}
