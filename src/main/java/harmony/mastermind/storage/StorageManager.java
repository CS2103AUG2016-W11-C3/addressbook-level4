package harmony.mastermind.storage;

import com.google.common.eventbus.Subscribe;

import harmony.mastermind.commons.core.ComponentManager;
import harmony.mastermind.commons.core.LogsCenter;
import harmony.mastermind.commons.events.model.TaskManagerChangedEvent;
import harmony.mastermind.commons.events.storage.RelocateFilePathEvent;
import harmony.mastermind.commons.events.storage.DataSavingExceptionEvent;
import harmony.mastermind.commons.exceptions.DataConversionException;
import harmony.mastermind.commons.exceptions.FolderDoesNotExistException;
import harmony.mastermind.commons.exceptions.UnwrittableFolderException;
import harmony.mastermind.model.ReadOnlyTaskManager;
import harmony.mastermind.model.UserPrefs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Manages storage of TaskManager data in local storage.
 */
public class StorageManager extends ComponentManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private XmlTaskManagerStorage taskManagerStorage;
    private JsonUserPrefStorage userPrefStorage;

    public StorageManager(XmlTaskManagerStorage addressBookStorage, JsonUserPrefStorage userPrefsStorage) {
        super();
        this.taskManagerStorage = addressBookStorage;
        this.userPrefStorage = userPrefsStorage;
    }

    public StorageManager(String taskManagerFilePath, String userPrefsFilePath) {
        super();
        this.taskManagerStorage = new XmlTaskManagerStorage(taskManagerFilePath);
        this.userPrefStorage = new JsonUserPrefStorage(userPrefsFilePath);
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataConversionException, IOException {
        return userPrefStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(UserPrefs userPrefs) throws IOException {
        userPrefStorage.saveUserPrefs(userPrefs);
    }


    // ================ TaskManager methods ==============================

    @Override
    public String getTaskManagerFilePath() {
        return taskManagerStorage.getTaskManagerFilePath();
    }

    @Override
    public Optional<ReadOnlyTaskManager> readTaskManager() throws DataConversionException, FileNotFoundException {
        logger.fine("Attempting to read data from file: " + taskManagerStorage.getTaskManagerFilePath());

        return taskManagerStorage.readTaskManager(taskManagerStorage.getTaskManagerFilePath());
    }

    @Override
    public void saveTaskManager(ReadOnlyTaskManager taskManager) throws IOException {
        taskManagerStorage.saveTaskManager(taskManager, taskManagerStorage.getTaskManagerFilePath());
    }


    @Override
    @Subscribe
    public void handleTaskManagerChangedEvent(TaskManagerChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event, "Local data changed, saving to file"));
        try {
            saveTaskManager(event.data);
        } catch (IOException e) {
            raise(new DataSavingExceptionEvent(e));
        }
    }
    
    //@@author A0139194X
    public void checkSaveLocation(String newFilePath) throws FolderDoesNotExistException {
        Path filePath = Paths.get(newFilePath);
        if (!Files.exists(filePath)) {
            throw new FolderDoesNotExistException(newFilePath + " does not exist");
        }
    }
    
    //@@author A0139194X
    //Checks if directory is writtable
    public void checkWrittableDirectory(String newFilePath) throws UnwrittableFolderException {
        File newFile = new File(newFilePath);
        if (!(newFile.isDirectory() && newFile.canWrite())) {
            throw new UnwrittableFolderException(newFilePath + " is not writtable.");
        }
    }
    
    //@@author A0139194X
    @Subscribe
    public void handleRelocateEvent(RelocateFilePathEvent event) {
        assert event.newFilePath != null;
        String oldPath = taskManagerStorage.getTaskManagerFilePath();
        String newPath = correctFilePathFormat(event.newFilePath);
        taskManagerStorage.setTaskManagerFilePath(newPath);
        try {
            taskManagerStorage.migrateIntoNewFolder(oldPath, newPath);
        } catch (AccessDeniedException ade) {
            logger.warning("Permission to access " + newPath + " denied." );
            logger.warning("Reverting save location back to " + oldPath);
            taskManagerStorage.setTaskManagerFilePath(oldPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateUserPrefs(newPath);
    }
   
    //@@author A0139194X
    //Appends the '/' if it is not that for a valid file path
    public String correctFilePathFormat(String newPath) {
        if (newPath.endsWith("/")) {
            newPath = newPath + "mastermind.xml";
        } else {
            newPath = newPath + "/mastermind.xml";
        }
        return newPath;
    }
    
    public void updateUserPrefs(String newPath) {
        userPrefStorage.setFilePath(newPath);
    }
}
