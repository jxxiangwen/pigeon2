/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author xiangwu
 * @Sep 11, 2013
 * 使用ServiceLoader在在META-INF/services下文件中加载类相应的实例
 */
public final class ExtensionLoader {

	private static final Logger logger = LoggerLoader.getLogger(ExtensionLoader.class);

	private static Map<Class<?>, Object> extensionMap = new ConcurrentHashMap<Class<?>, Object>();

	private static Map<Class<?>, List<?>> extensionListMap = new ConcurrentHashMap<Class<?>, List<?>>();

	public static <T> T getExtension(Class<T> clazz) {
		T extension = (T) extensionMap.get(clazz);
		if (extension == null) {
			extension = newExtension(clazz);
			if (extension != null) {
				extensionMap.put(clazz, extension);
			}
		}
		return extension;
	}

	public static <T> List<T> getExtensionList(Class<T> clazz) {
		List<T> extensions = (List<T>) extensionListMap.get(clazz);
		if (extensions == null) {
			extensions = newExtensionList(clazz);
			if (!extensions.isEmpty()) {
				extensionListMap.put(clazz, extensions);
			}
		}
		return extensions;
	}

	public static <T> T newExtension(Class<T> clazz) {
		// 在META-INF/services下面找对应的文件
		ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
		for (T service : serviceLoader) {
			return service;
		}
		logger.warn("no extension found for class:" + clazz.getName());
		return null;
	}

	public static <T> List<T> newExtensionList(Class<T> clazz) {
		ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
		List<T> extensions = new ArrayList<T>();
		for (T service : serviceLoader) {
			extensions.add(service);
		}
		if (extensions.isEmpty()) {
			logger.warn("no extension found for class:" + clazz.getName());
		}
		return extensions;
	}
}
