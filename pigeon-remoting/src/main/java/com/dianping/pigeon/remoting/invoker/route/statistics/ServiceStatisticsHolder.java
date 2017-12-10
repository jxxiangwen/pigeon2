package com.dianping.pigeon.remoting.invoker.route.statistics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.route.balance.AutoawareLoadBalance;

public final class ServiceStatisticsHolder {

	private static final Logger logger = LoggerLoader.getLogger(ServiceStatisticsHolder.class);

	private static ConcurrentHashMap<String, CapacityBucket> serverCapacityBuckets = new ConcurrentHashMap<String, CapacityBucket>();

	public static final boolean statEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.routestat.enable", true);

	/**
	 * 用于负载均衡
	 * @param server 服务器ip
	 * @return 容量
	 */
	public static float getCapacity(String server) {
		CapacityBucket barrel = serverCapacityBuckets.get(server);
		return barrel != null ? barrel.getCapacity() : 0f;
	}

	public static void init() {
	}
	
	public static Map<String, CapacityBucket> getCapacityBuckets() {
		return serverCapacityBuckets;
	}

	public static CapacityBucket getCapacityBucket(String server) {
		CapacityBucket barrel = serverCapacityBuckets.get(server);
		if (barrel == null) {
			CapacityBucket newBarrel = new CapacityBucket(server);
			barrel = serverCapacityBuckets.putIfAbsent(server, newBarrel);
			if (barrel == null) {
				barrel = newBarrel;
			}
		}
		return barrel;
	}

	/**
	 * 借助于容量桶统计toServer这个服务器的请求
	 * @param request 请求
	 * @param toServer 服务器ip
	 */
	public static void flowIn(InvocationRequest request, String toServer) {
		if (checkRequestNeedStat(request)) {
			CapacityBucket barrel = getCapacityBucket(toServer);
			if (barrel != null) {
				barrel.flowIn(request);
			} else {
				logger.error("Got a null barrel with server[" + toServer + "] in flowIn operation.");
			}
		}
	}

	/**
	 * 调用流出
	 * @param request 调用请求
	 * @param fromServer 调用的服务器地址
	 */
	public static void flowOut(InvocationRequest request, String fromServer) {
		if (checkRequestNeedStat(request)) {
			// 获取容量桶
			CapacityBucket barrel = getCapacityBucket(fromServer);
			if (barrel != null) {
				barrel.flowOut(request);
			} else {
				logger.error("Got a null barrel with server[" + fromServer + "] in flowOut operation.");
			}
		}
	}

	public static boolean checkRequestNeedStat(InvocationRequest request) {
		if (request == null || request.getMessageType() != Constants.MESSAGE_TYPE_SERVICE) {
			return false;
		}
		if (AutoawareLoadBalance.NAME.equals(request.getLoadbalance())) {
			return true;
		} else {
			return statEnable;
		}
	}

	public static void removeCapacityBucket(String server) {
		serverCapacityBuckets.remove(server);
	}
}
