package harmony.mastermind.ui;

import org.ocpsoft.prettytime.PrettyTime;

import harmony.mastermind.commons.util.AppUtil;
import harmony.mastermind.commons.util.FxViewUtil;
import harmony.mastermind.logic.Logic;
import harmony.mastermind.model.task.ReadOnlyTask;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class TasksTableView extends DefaultTableView {
    
    private static final String FXML = "TasksTableView.fxml";
    
    private static final PrettyTime prettyTime = new PrettyTime();
    
    private AnchorPane placeholder;
    
    private Logic logic;
    
    @FXML
    private TableView<ReadOnlyTask> tasksTableView;

    @FXML
    private TableColumn<ReadOnlyTask, ReadOnlyTask> indexColumn;

    @FXML
    private TableColumn<ReadOnlyTask, ReadOnlyTask> nameColumn;

    @FXML
    private TableColumn<ReadOnlyTask, ReadOnlyTask> startDateColumn;

    @FXML
    private TableColumn<ReadOnlyTask, ReadOnlyTask> endDateColumn;

    @FXML
    private TableColumn<ReadOnlyTask, ReadOnlyTask> tagsColumn;

    @FXML
    private TableColumn<ReadOnlyTask, Boolean> recurColumn;

    
    public static TasksTableView load(Stage primaryStage, AnchorPane defaultTableViewPlaceholder, Logic logic){
        TasksTableView ui = UiPartLoader.loadUiPart(primaryStage, defaultTableViewPlaceholder, new TasksTableView());
        ui.configure(logic);
        return ui;
    }
    
    /**
     * Initialize the displaying of tabs
     */
    @FXML
    private void initialize() {
        this.initIndex();
        this.initName();
        this.initStartDate();
        this.initEndDate();
        this.initTags();
        this.initRecur();
    }
    
    
    @Override
    public void setPlaceholder(AnchorPane placeholder){
        this.placeholder = placeholder;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void setNode(Node node) {
        this.tasksTableView = (TableView<ReadOnlyTask>) node;
    }

    @Override
    public String getFxmlPath() {
        return FXML;
    }
    
    protected void configure(Logic logic){
        this.logic = logic;
        this.addToPlaceholder();
        this.registerAsAnEventHandler(this);
    }

    private void addToPlaceholder(){
        placeholder.getChildren().add(tasksTableView);
        FxViewUtil.applyAnchorBoundaryParameters(tasksTableView, 0, 0, 0, 0);
    }
    
    @Override
    public TableView<ReadOnlyTask> getTableView(){
        return tasksTableView;
    }

    /**
     * Initializes the indexing of tasks
     */
    protected void initIndex() {
        indexColumn.prefWidthProperty().bind(tasksTableView.widthProperty().multiply(WIDTH_MULTIPLIER_INDEX));
        indexColumn.setCellFactory(column -> renderIndexCell());
    }

    /**
     * Initialize the Names of the tasks
     */
    protected void initName() {
        nameColumn.prefWidthProperty().bind(tasksTableView.widthProperty().multiply(WIDTH_MULTIPLIER_NAME));
        nameColumn.setCellValueFactory(cellValue -> new SimpleObjectProperty<>(cellValue.getValue()));
        nameColumn.setCellFactory(col -> renderNameCell());
    }

    /**
     * Initialize the start dates of the tasks
     */
    protected void initStartDate() {
        startDateColumn.prefWidthProperty().bind(tasksTableView.widthProperty().multiply(WIDTH_MULTIPLIER_STARTDATE));
        startDateColumn.setCellValueFactory(cellValue -> new SimpleObjectProperty<>(cellValue.getValue()));

        startDateColumn.setCellFactory(col -> renderStartDateCell());

    }

    /**
     * Initialize the end dates of the tasks
     */
    protected void initEndDate() {
        endDateColumn.prefWidthProperty().bind(tasksTableView.widthProperty().multiply(WIDTH_MULTIPLIER_ENDDATE));
        endDateColumn.setCellValueFactory(cellValue -> new SimpleObjectProperty<>(cellValue.getValue()));

        endDateColumn.setCellFactory(col -> renderEndDateCell());

    }


    /**
     * Initialize the tags of the tasks
     */
    protected void initTags() {
        tagsColumn.prefWidthProperty().bind(tasksTableView.widthProperty().multiply(WIDTH_MULTIPLIER_TAGS));
        tagsColumn.setCellValueFactory(cellValue -> new SimpleObjectProperty<>(cellValue.getValue()));

        tagsColumn.setCellFactory(col -> renderTagsCell());

    }


    /**
     * Initialize a checkbox to determine whether task is recurring
     */
    protected void initRecur() {
        recurColumn.prefWidthProperty().bind(tasksTableView.widthProperty().multiply(WIDTH_MULTIPLIER_RECUR));
        
        recurColumn.setGraphic(new ImageView(AppUtil.getImage("/images/recur_white.png")));
        recurColumn.setCellValueFactory(task -> new SimpleBooleanProperty(task.getValue().isRecur()));
        recurColumn.setCellFactory(col -> renderRecurCell());
    }
}

