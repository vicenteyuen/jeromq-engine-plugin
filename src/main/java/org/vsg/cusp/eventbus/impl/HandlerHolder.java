package org.vsg.cusp.eventbus.impl;

import org.vsg.cusp.eventbus.Context;

public class HandlerHolder<T> {

	private Context context;
	private HandlerRegistration<T> handler;
	private boolean replyHandler;
	private boolean localOnly;
	private boolean removed;

	public HandlerHolder(HandlerRegistration<T> handler, boolean replyHandler,
			boolean localOnly, Context context) {

		this.context = context;
		this.handler = handler;
		this.replyHandler = replyHandler;
		this.localOnly = localOnly;
	}

	// We use a synchronized block to protect removed as it can be unregistered
	// from a different thread
	public void setRemoved() {
		boolean unregisterMetric = false;
		synchronized (this) {
			if (!removed) {
				removed = true;
				unregisterMetric = true;
			}
		}

	}

	// Because of biased locks the overhead of the synchronized lock should be
	// very low as it's almost always
	// called by the same event loop
	public synchronized boolean isRemoved() {
		return removed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		HandlerHolder that = (HandlerHolder) o;
		if (handler != null ? !handler.equals(that.handler)
				: that.handler != null)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return handler != null ? handler.hashCode() : 0;
	}

	public Context getContext() {
		return context;
	}

	public HandlerRegistration<T> getHandler() {
		return handler;
	}

	public boolean isReplyHandler() {
		return replyHandler;
	}

	public boolean isLocalOnly() {
		return localOnly;
	}
}
