package org.jboss.as.quickstarts.datagrid.remotetasks.tasks;

import static org.infinispan.stream.CacheCollectors.serializableCollector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.jboss.as.quickstarts.datagrid.remotetasks.Book;

/**
 * Task necessary for querying over the existing Books.
 * The query is done based on the user input - title, author, pageNumbers and publicationYear.
 * If user hasn't entered any of them, then all books will be listed.
 * The title and the author can be entered as a regular expression.
 *
 * @author Anna Manukyan
 */
@SuppressWarnings({ "unchecked" })
public class BooksQueryingTask implements ServerTask<List<Book>> {
    public static String BOOKS_QUERYING_TASK_NAME = "booksQueryingTask";

    private TaskContext taskContext;
    private Map<String, Object> parameters;
    public static String authorParamName = "author";
    public static String titleParamName = "title";
    public static String pubYearParamName = "pubYear";
    public static String pageNumParamName = "pageNum";

    @Override
    public void setTaskContext(TaskContext taskContext) {
        this.taskContext = taskContext;
        this.parameters = (Map<String, Object>) taskContext.getParameters().get();
    }

    @Override
    public String getName() {
        return BOOKS_QUERYING_TASK_NAME;
    }

    @Override
    public List<Book> call() throws Exception {
        Cache<UUID, Book> cache = (Cache<UUID, Book>)taskContext.getCache().get();

        String author = (String)parameters.get(authorParamName);
        String title = (String)parameters.get(titleParamName);
        Integer pubYear = (Integer)parameters.get(pubYearParamName);
        Integer pageNum = (Integer)parameters.get(pageNumParamName);
        
        return cache.entrySet().parallelStream()
                .map(Map.Entry::getValue)
                .filter(book ->
                        (author == null || book.getAuthor().contains(author))
                                && (title == null || book.getTitle().contains(title))
                                && (pageNum == null || pageNum.equals(book.getNumberOfPages()))
                                && (pubYear == null || pubYear.equals(book.getPublicationYear())))
                .collect(serializableCollector(() -> Collectors.toList()));
    }
}
