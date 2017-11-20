/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author xiangwu
 * @Oct 11, 2013
 * 
 */
public class NetUtils {

	public static List<InetAddress> getAllLocalAddress() {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			List<InetAddress> addresses = new ArrayList<InetAddress>();

			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					addresses.add(inetAddress);
				}
			}

			return addresses;
		} catch (SocketException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static List<String> getAllLocalIp() {
		List<String> noLoopbackAddresses = new ArrayList<String>();
		List<InetAddress> allInetAddresses = getAllLocalAddress();

		for (InetAddress address : allInetAddresses) {
			if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
				noLoopbackAddresses.add(address.getHostAddress());
			}
		}

		return noLoopbackAddresses;
	}

	public static String getFirstLocalIp() {
		List<String> allNoLoopbackAddresses = getAllLocalIp();
		if (allNoLoopbackAddresses.isEmpty()) {
			throw new IllegalStateException("Sorry, seems you don't have a network card :( ");
		}
		return allNoLoopbackAddresses.get(allNoLoopbackAddresses.size() - 1);
	}

	public static int getAvailablePort() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket();
			ss.bind(null);
			return ss.getLocalPort();
		} catch (IOException e) {
			throw new IllegalStateException("", e);
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static int getAvailablePort(int defaultPort) {
		int port = defaultPort;
		while (port < 65535) {
			if (!isPortInUse(port)) {
				return port;
			} else {
				port++;
			}
		}
		while (port > 0) {
			if (!isPortInUse(port)) {
				return port;
			} else {
				port--;
			}
		}
		throw new IllegalStateException("no available port");
	}

	public static boolean isPortInUse(int port) {
		boolean inUse = false;
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			inUse = false;
		} catch (IOException e) {
			inUse = true;
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
				}
			}
		}
		return inUse;
	}
}
