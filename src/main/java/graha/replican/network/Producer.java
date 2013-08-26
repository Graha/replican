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

import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.util.Map;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * Simple TCP based Producer Implementation
 * <p/>
 * This class is used for performance test purposes. It does nothing at all, but send a message
 * repetitively to a server.
 * <p/>
 */
public class Producer extends IoHandlerAdapter {

	// The connector
	private IoConnector connector;

	// The session
	protected  IoSession session;

	// Any Message been Recieved
	private boolean received = false;

	public Producer(String host, int port) {

		connector = new NioSocketConnector();

		connector.setHandler(this);

		ConnectFuture connFuture = connector.connect(new InetSocketAddress(host, port));

		connFuture.awaitUninterruptibly();

		session = connFuture.getSession();

	}

	/**
	 * Create the Producer instance with default values
	 */
	public Producer() {
		this("localhost", Consumer.PORT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		cause.printStackTrace();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// If we want to test the write operation, uncomment this line
		session.write(UTFCoder.encode("ACK"));

		received = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		String text = UTFCoder.decode(message);
		System.out.printf("Sent Out : %s \n", text);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
	}

	public void send(String text) {
		try {
			session.write(UTFCoder.encode(text));
		} catch (CharacterCodingException e) {
			System.out.println("Send Failed");
		}
	}

}
