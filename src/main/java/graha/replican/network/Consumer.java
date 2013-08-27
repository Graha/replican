package graha.replican.network;

/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple TCP based Consumer Implementation
 * <p/>
 * It does nothing fancy, except receiving the messages, and counting the number of
 * received messages.
 */

public class Consumer extends IoHandlerAdapter {
	Logger log = Logger.getLogger(Consumer.class);

	/**
	 * The listening port (check that it's not already in use)
	 */

	public static final int PORT = 18567;

	protected NioSocketAcceptor acceptor = new NioSocketAcceptor();

	/**
	 * A counter incremented for every recieved message
	 */

	private AtomicInteger nbReceived = new AtomicInteger(0);

	protected NioSocketAcceptor getAcceptor() {
		return acceptor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		cause.printStackTrace();
		session.close(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {

		// If we want to test the write operation, uncomment this line
		session.write(message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		log.info("Session closed...");

		// Reinitialize the counter and expose the number of received messages
		log.info("Total message received : " + nbReceived.get());
		nbReceived.set(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		log.info("Session created...");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		log.info("Session idle...");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		log.info("Session Opened...");
	}

	public void send(String text) {
		try {
			Map<Long, IoSession> map = acceptor.getManagedSessions();
			for (Long sessionId : map.keySet()) {
				map.get(sessionId).write(UTFCoder.encode(text));
			}
		} catch (CharacterCodingException e) {
			throw new RuntimeException("Send failed");
		}
	}


	public Consumer() {
		this(PORT);
	}

	/**
	 * Create the TCP server
	 */
	public Consumer(int port) {

		acceptor.setHandler(this);

		try {
			acceptor.bind(new InetSocketAddress(port));
			acceptor.getSessionConfig().setTcpNoDelay(true);
		} catch (IOException e) {
			throw new RuntimeException("Server Can't be started");
		}

		log.info("Server started @ " + port);
	}


}
