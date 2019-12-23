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

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.Cache;
import org.infinispan.stream.CacheAware;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.infinispan.util.function.SerializableConsumer;
import org.jboss.as.quickstarts.datagrid.remotetasks.Book;

/**
 * Task, which finds and removes the books from the cache, according to it's title and author.
 *
 * @author Anna Manukyan
 */
@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
public class BooksRemovingTask implements ServerTask<Integer> {
    public static String BOOKS_REMOVING_TASK_NAME = "booksRemovingTask";

    private TaskContext taskContext;
    private Map<String, String> parameters;
    private static String authorParamName = "author";
    private static String titleParamName = "title";

    @Override
    public void setTaskContext(TaskContext taskContext) {
        this.taskContext = taskContext;
        this.parameters = (Map<String, String>) taskContext.getParameters().get();
    }

    @Override
    public String getName() {
        return BOOKS_REMOVING_TASK_NAME;
    }

    @Override
    public Integer call() throws Exception {
        Cache<UUID, Book> cache = (Cache<UUID, Book>)taskContext.getCache().get();

        String author = parameters.get(authorParamName);
        String title = parameters.get(titleParamName);
        
        AtomicInteger i = new AtomicInteger();
        cache.entrySet().parallelStream().filter( book ->
                (author == null || book.getValue().getAuthor().contains(author))
                        && (title == null || book.getValue().getTitle().contains(title)))
                .forEach(new MyConsumer(i));

        System.out.println("Successfully finished the action.");
        return i.get();
    }
    
    public static class MyConsumer implements SerializableConsumer<Entry>, CacheAware {

        private transient Cache cache;
        private AtomicInteger i;

        public MyConsumer(AtomicInteger i) {
            this.i = i;
        }

        @Override
        public void accept(Entry e) {            
            cache.remove(e.getKey());
            i.incrementAndGet();
        }

        @Override
        public void injectCache(Cache c) {
            cache = c;
        }
        
    }
}
