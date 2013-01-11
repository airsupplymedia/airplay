package de.airsupply.commons.core.context;

import java.lang.reflect.Field;

import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

public class LoggerBeanFactoryPostProcessor implements BeanPostProcessor {

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
		ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {

			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				ReflectionUtils.makeAccessible(field);
				if (field.getAnnotation(Loggable.class) != null) {
					field.set(bean, LoggerFactory.getLogger(bean.getClass()));
				}
			}

		});
		return bean;
	}

}