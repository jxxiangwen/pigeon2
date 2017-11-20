package com.dianping.pigeon.registry.listener;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.util.Utils;

public class DefaultServiceChangeListener implements ServiceChangeListener {

	private static final Logger logger = LoggerLoader.getLogger(DefaultServiceChangeListener.class);

	public DefaultServiceChangeListener() {
	}

	@Override
	public synchronized void onServiceHostChange(String serviceName, List<String[]> hostList) {
		try {
			Set<HostInfo> newHpSet = parseHostPortList(serviceName, hostList);
			Set<HostInfo> oldHpSet = RegistryManager.getInstance().getReferencedServiceAddresses(serviceName);
			Set<HostInfo> toAddHpSet = Collections.emptySet();
			Set<HostInfo> toRemoveHpSet = Collections.emptySet();
			if (oldHpSet == null) {
				toAddHpSet = newHpSet;
			} else {
				toRemoveHpSet = new HashSet<HostInfo>(oldHpSet);
				toRemoveHpSet.removeAll(newHpSet);
				toAddHpSet = new HashSet<HostInfo>(newHpSet);
				toAddHpSet.removeAll(oldHpSet);
			}
			if (logger.isInfoEnabled()) {
				logger.info("service hosts changed, to added hosts:" + toAddHpSet);
				logger.info("service hosts changed, to removed hosts:" + toRemoveHpSet);
			}
			for (HostInfo hostPort : toAddHpSet) {
				RegistryEventListener.providerAdded(serviceName, hostPort.getHost(), hostPort.getPort(),
						hostPort.getWeight());
			}
			for (HostInfo hostPort : toRemoveHpSet) {
				RegistryEventListener.providerRemoved(serviceName, hostPort.getHost(), hostPort.getPort());
			}
		} catch (Throwable e) {
			logger.error("error change service host", e);
		}
	}

	private Set<HostInfo> parseHostPortList(String serviceName, List<String[]> hostList) {
		Set<HostInfo> hpSet = new HashSet<HostInfo>();
		if (hostList != null) {
			for (String[] parts : hostList) {
				String host = parts[0];
				String port = parts[1];
				String serviceAddress = host + ":" + port;

				int weight = RegistryManager.getInstance().getServiceWeight(serviceAddress);
				hpSet.add(new HostInfo(host, Integer.parseInt(port), weight));
			}
		}
		return hpSet;
	}

	@Override
	public synchronized void onHostWeightChange(String connect, int weight) {
		HostInfo hostInfo = Utils.parseHost(connect, weight);
		if (hostInfo != null) {
			RegistryEventListener.hostWeightChanged(hostInfo.getHost(), hostInfo.getPort(), weight);
		}
	}

	@Override
	public void onHostAdded(String serviceName, String host) {
		HostInfo hostInfo = Utils.parseHost(host, 1);
		if (hostInfo != null) {
			int weight = RegistryManager.getInstance().getServiceWeight(host);
			RegistryEventListener.providerAdded(serviceName, hostInfo.getHost(), hostInfo.getPort(), weight);
			if (logger.isInfoEnabled()) {
				logger.info("host " + host + " added to service " + serviceName);
			}
		}
	}

	@Override
	public void onHostRemoved(String serviceName, String host) {
		HostInfo hostInfo = Utils.parseHost(host, 1);
		if (hostInfo != null) {
			RegistryEventListener.providerRemoved(serviceName, hostInfo.getHost(), hostInfo.getPort());
			if (logger.isInfoEnabled()) {
				logger.info("host " + host + " removed from service " + serviceName);
			}
		}
	}

}
