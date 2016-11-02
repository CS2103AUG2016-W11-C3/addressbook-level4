package harmony.mastermind.storage;

import com.google.common.eventbus.Subscribe;

import harmony.mastermind.commons.core.ComponentManager;
import harmony.mastermind.commons.core.LogsCenter;
import harmony.mastermind.commons.events.model.TaskManagerChangedEvent;
import harmony.mastermind.commons.events.storage.RelocateFilePathEvent;
import harmony.mastermind.commons.events.storage.DataSavingExceptionEvent;
import harmony.mastermind.commons.exceptions.DataConversionException;
import harmony.mastermind.commons.util.ConfigUtil;
import harmony.mastermind.commons.util.StringUtil;
import harmony.mastermind.model.ReadOnlyTaskManager;
import harmony.mastermind.model.UserPrefs;
import harmony.mastermind.commons.core.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
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
    /**
     * Handles RelocateFilePathEvent by first changing taskManger's file path, then moving
     * the file over and deleting the old one
     */
    @Subscribe
    public void handleRelocateEvent(RelocateFilePathEvent event) {
        assert event != null;
        assert event.getFilePath() != null;
        String oldPath = taskManagerStorage.getTaskManagerFilePath();
        String newPath = correctFilePathFormat(event.getFilePath());
        taskManagerStorage.setTaskManagerFilePath(newPath);
        try {
            taskManagerStorage.migrateIntoNewFolder(oldPath, newPath);
        } catch (AccessDeniedException ade) {
            logger.warning("Permission to access " + newPath + " denied." );
            logger.warning("Reverting save location back to " + oldPath);
            taskManagerStorage.setTaskManagerFilePath(oldPath);
        } catch (IOException e) {
            logger.warning("Error occured while handling relocate event");
            logger.warning("Reverting save location back to " + oldPath);
            taskManagerStorage.setTaskManagerFilePath(oldPath);
        }
        updateConfig(newPath);
    }
   
    //@@author A0139194X
    //Appends the '/' if it is not that for a valid file path
    public String correctFilePathFormat(String newPath) {
        assert newPath != null;
        if (newPath.endsWith("/")) {
            newPath = newPath + "mastermind.xml";
        } else {
            newPath = newPath + "/mastermind.xml";
        }
        return newPath;
    }
    
    //@@author A0139194X
    public void updateConfig(String newPath) {
        assert newPath != null;
        Config config;
        String defaultConfigLocation = Config.DEFAULT_CONFIG_FILE;
        
        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(defaultConfigLocation);
            config = configOptional.orElse(new Config());
        } catch (DataConversionException e) {
            logger.warning("Config file at " + defaultConfigLocation + " is not in the correct format. " +
                    "Using default config properties");
            config = new Config();
        }

        config.setTaskManagerFilePath(newPath);
        
        //Update config file in case it was missing to begin with or there are new/unused fields
        try {
            ConfigUtil.saveConfig(config, defaultConfigLocation);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
    }
}
