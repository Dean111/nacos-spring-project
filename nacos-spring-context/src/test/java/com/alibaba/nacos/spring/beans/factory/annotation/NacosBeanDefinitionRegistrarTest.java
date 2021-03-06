/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.spring.beans.factory.annotation;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.spring.context.annotation.*;
import com.alibaba.nacos.spring.context.properties.NacosConfigPropertiesBindingPostProcessor;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.test.Config;
import com.alibaba.nacos.spring.test.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.test.ListenersConfiguration;
import com.alibaba.nacos.spring.util.NacosBeanUtils;
import com.alibaba.nacos.spring.util.NacosConfigLoader;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Properties;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;

/**
 * {@link NacosBeanDefinitionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        ListenersConfiguration.class,
        NacosBeanDefinitionRegistrarTest.class,
})
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${serverAddr}"))
public class NacosBeanDefinitionRegistrarTest {

    private static EmbeddedNacosHttpServer httpServer;

    static {
        System.setProperty("nacos.standalone", "true");
        try {
            httpServer = new EmbeddedNacosHttpServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.setProperty("serverAddr", "127.0.0.1:" + httpServer.getPort());
    }

    @BeforeClass
    public static void startServer() throws Exception {
        httpServer.start(true);
    }

    @AfterClass
    public static void stopServer() {
        httpServer.stop();
    }

    @Bean
    public Config config() {
        return new Config();
    }

    @Autowired
    @Qualifier(NacosBeanUtils.GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
    private Properties globalProperties;

    @Autowired
    @Qualifier(NacosBeanUtils.NACOS_SERVICE_FACTORY_BEAN_NAME)
    private NacosServiceFactory nacosServiceFactory;

    @Autowired
    @Qualifier(NacosBeanUtils.NACOS_PROPERTIES_RESOLVER_BEAN_NAME)
    private NacosPropertiesResolver nacosPropertiesResolver;

    @Autowired
    @Qualifier(NacosBeanUtils.NACOS_CONFIG_LOADER_BEAN_NAME)
    private NacosConfigLoader nacosConfigLoader;

    @Autowired
    @Qualifier(NamingServiceInjectedBeanPostProcessor.BEAN_NAME)
    private NamingServiceInjectedBeanPostProcessor namingServiceInjectedBeanPostProcessor;

    @Autowired
    @Qualifier(NacosConfigPropertiesBindingPostProcessor.BEAN_NAME)
    private NacosConfigPropertiesBindingPostProcessor nacosConfigPropertiesBindingPostProcessor;

    @Autowired
    @Qualifier(NacosConfigListenerMethodProcessor.BEAN_NAME)
    private NacosConfigListenerMethodProcessor nacosConfigListenerMethodProcessor;

    @Autowired
    @Qualifier(NacosPropertySourceProcessor.BEAN_NAME)
    private NacosPropertySourceProcessor nacosPropertySourceProcessor;

    @NacosService
    private ConfigService configService;

    @Autowired
    private Config config;

    @Test
    public void testGetConfig() throws Exception {
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");
        Assert.assertEquals("9527", configService.getConfig(DATA_ID, DEFAULT_GROUP, 5000));
    }

}
