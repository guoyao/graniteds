/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.config.servlet3;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.granite.config.ConfigProvider;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.messaging.service.security.SecurityService;


@Inherited
@Retention(RUNTIME)
@Target({TYPE})
public @interface ServerFilter {
	
	String graniteUrlMapping() default "/graniteamf/*";
	
	String gravityUrlMapping() default "/gravityamf/*";
	
	boolean tide() default false;
	
	String type() default "";
	
	Class<? extends ConfigProvider> configProviderClass() default ConfigProvider.class;
	
	Class<? extends ServiceFactory> factoryClass() default ServiceFactory.class;

    String factoryClassName() default "";
	
	Class<? extends SecurityService> securityServiceClass() default SecurityService.class;
	
	Class<?>[] tideInterfaces() default {};
	
	Class<? extends Annotation>[] tideAnnotations() default { RemoteDestination.class };
	
	String[] tideNames() default {};
	
	String[] tideTypes() default {};
	
	String[] tideRoles() default {};
	
	Class<? extends ExceptionConverter>[] exceptionConverters() default {};
	
	Class<? extends AMF3MessageInterceptor> amf3MessageInterceptor() default AMF3MessageInterceptor.class;
	
	String ejbLookup() default "";
	
	String entityManagerFactoryJndiName() default "";
	
	String entityManagerJndiName() default "";
	
	String validatorClassName() default "";
	
	boolean useBigDecimal() default false;
	
	boolean useBigInteger() default false;
	
	boolean useLong() default false;
}
