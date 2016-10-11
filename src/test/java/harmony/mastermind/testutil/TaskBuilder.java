package harmony.mastermind.testutil;

import java.util.Date;

import harmony.mastermind.commons.exceptions.IllegalValueException;
import harmony.mastermind.model.tag.Tag;
import harmony.mastermind.model.task.*;

/**
 *
 */
public class TaskBuilder {

    private TestTask task;

    public TaskBuilder() {
        this.task = new TestTask();
    }

    //@@author A0124797R
    public TaskBuilder withName(String name) {
        this.task.setName(name);
        return this;
    }


    public TaskBuilder withTags(String ... tags) throws IllegalValueException {
        for (String tag: tags) {
            task.getTags().add(new Tag(tag));
        }
        return this;
    }
    
    //@@author A0138863W
    public TaskBuilder withStartDate(Date startDate){
        this.task.setStartDate(startDate);
        return this;
    }

    //@@author A0138863W
    public TaskBuilder withEndDate(Date endDate){
        this.task.setEndDate(endDate);
        return this;
    }
    
    //@@author A0138863W
    public TaskBuilder withIsMarked(boolean isMarked){
        this.task.setMarked(isMarked);
        return this;
    }

    public TestTask build() {
        return this.task;
    }

}
