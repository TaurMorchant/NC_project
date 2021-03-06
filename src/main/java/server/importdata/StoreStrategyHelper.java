package server.importdata;

import server.importdata.journalstrategy.ExceptionThrowJournalStrategy;
import server.importdata.journalstrategy.IgnoreJournalStrategy;
import server.importdata.journalstrategy.ReplaceJournalStrategy;
import server.importdata.taskstrategy.ExceptionThrowTaskStrategy;
import server.importdata.taskstrategy.IgnoreTaskStrategy;
import server.importdata.taskstrategy.ReplaceTaskStrategy;
import server.model.Journal;
import server.model.Task;

import java.util.HashMap;
import java.util.Map;

public class StoreStrategyHelper {
    private static StoreStrategyHelper ourInstance = new StoreStrategyHelper();
    private Map<String, Map<String, StoreStrategy>> strategies;
    private final String NO_SUCH_STRATEGY = "Could not find suitable strategy!";

    public static StoreStrategyHelper getInstance() {
        return ourInstance;
    }

    private StoreStrategyHelper() {
        strategies = new HashMap<>();

        Map<String, StoreStrategy> journalStrategies = new HashMap<>();
        journalStrategies.put(StoreConstants.REPLACE_STRATEGY, new ReplaceJournalStrategy<Journal>());
        journalStrategies.put(StoreConstants.IGNORE_STRATEGY, new IgnoreJournalStrategy<Journal>());
        journalStrategies.put(StoreConstants.EXCEPTION_THROW_STRATEGY, new ExceptionThrowJournalStrategy<Journal>());
        strategies.put(StoreConstants.JOURNAL, journalStrategies);

        Map<String, StoreStrategy> taskStrategies = new HashMap<>();
        taskStrategies.put(StoreConstants.REPLACE_STRATEGY, new ReplaceTaskStrategy<Task>());
        taskStrategies.put(StoreConstants.IGNORE_STRATEGY, new IgnoreTaskStrategy<Task>());
        taskStrategies.put(StoreConstants.EXCEPTION_THROW_STRATEGY, new ExceptionThrowTaskStrategy<Task>());
        strategies.put(StoreConstants.TASK, taskStrategies);
    }

    public StoreStrategy resolveStrategy(StoreItem item, String type) throws StoreException {
        if (type.equals(StoreConstants.JOURNAL))
            return strategies.get(type).get(item.getJournalStrategy());
        else if (type.equals(StoreConstants.TASK))
            return strategies.get(type).get(item.getTaskStrategy());
        else
            throw new StoreException(NO_SUCH_STRATEGY);
    }
}
