/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.datagrid.remotetasks.tasks;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;
import org.infinispan.stream.CacheAware;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.infinispan.tasks.TaskExecutionMode;
import org.infinispan.util.function.SerializableConsumer;

@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
public class MyRemoteTask implements ServerTask<Void> {
    private TaskContext taskContext;

    @Override
    public void setTaskContext(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public String getName() {
        return "MyRemoteTask";
    }
    
    @Override
    public TaskExecutionMode getExecutionMode() {
        return TaskExecutionMode.ONE_NODE;
//      return TaskExecutionMode.ALL_NODES;
    }
    
    @Override
    public Void call() throws Exception {
        Cache<String, String> cache = (Cache<String, String>) taskContext.getCache().get();
        cache.entrySet().stream().timeout(10, TimeUnit.MINUTES).forEach(new MyConsumer("default"));
        return null;
    }
    
    public static class MyConsumer implements SerializableConsumer<Entry>, CacheAware {

        private String targetCacheName;
        private transient Cache targetCache;
        
        public MyConsumer(String targetCacheName) {
            this.targetCacheName = targetCacheName;
        }


        @Override
        public void accept(Entry e) {            
            targetCache.put(e.getKey(), e.getValue());
        }

        @Override
        public void injectCache(Cache c) {
            targetCache = c.getCacheManager().getCache(targetCacheName);
        }
        
    }
}
