package server.beans;

import server.exportdata.ExportException;
import server.exportdata.ExportList;
import server.exportdata.ExportListFactory;
import server.importdata.*;
import server.model.Journal;
import server.model.JournalContainer;
import server.model.Task;

import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class ExportImportBean implements EIBeanLocal {

    private Marshaller marshaller = Marshaller.getInstance();

    /**
     * 1. Config file parsing(Done in init-ion of AuthServlet because ServletContext.getRealPath is needed)
     * (In: file path; Out: <code>ExportConfigHelper</code> with parsed export strategies for tasks and journals)
     * 2. Filling in ExportList object using strategies.
     * (In: IDs of objects, what user has selected, config info;
     * Out: ExportList with received IDs).
     * 3. Getting objects from DB.
     * (In: Filled ExportList; Out: java objects from DB.)
     * 4. Transformation received objects to xml file
     * (In: java objects from DB; Out: xml-file)
     */

    @Override
    public String exportData(List<Integer> journalIDs, List<Integer> taskIDs) throws ExportException {
        ExportList exportList = ExportListFactory.getInstance().createList(journalIDs, taskIDs);
        // todo вызов методов контроллера, которые достанут и вернут объекты по полученным айди
        // todo вызов маршаллера, который сформирует xml по полученным объектам
        return null;
    }

    /**
     * 1. Parse received xml (In: xml; Out: parsed java object)
     * 2. Call <code>StoreStrategyHelper</code> to perform needed strategy and import received data into DB.
     * (In: strategy type, java object for import; Out: nothing, execution exception(if something gone wrong while
     * performing a strategy) or strategy exception)
     */

    @Override
    public void importData(String xml, String journalStrategy, String taskStrategy) throws StoreException {
        StoreItem storeItem = marshaller.unmarshal(xml);
        storeItem.setJournalStrategy(journalStrategy);
        storeItem.setTaskStrategy(taskStrategy);
        StoreStrategyHelper storeStrategyHelper = StoreStrategyHelper.getInstance();
        StoreStrategy<Journal> journalStoreStrategy = storeStrategyHelper.resolveStrategy(storeItem, StoreConstants.JOURNAL);
        StoreStrategy<Task> taskStoreStrategy = storeStrategyHelper.resolveStrategy(storeItem, StoreConstants.TASK);

        List<Journal> journals = storeItem.getContainer().getJournals();
        for (Journal j : journals) {
            if (journalStoreStrategy.store(j))
                for (Task t : j.getTasks()) {
                    taskStoreStrategy.store(t);
                }
        }
    }
}
