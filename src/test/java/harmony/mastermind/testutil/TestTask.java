package harmony.mastermind.testutil;

import java.util.Date;

import guitests.TaskManagerGuiTest;
import harmony.mastermind.model.tag.UniqueTagList;
import harmony.mastermind.model.task.*;

/**
 * A mutable task object. For testing only.
 */
//@@author A0124797R
public class TestTask implements ReadOnlyTask {
    
    private String name;
    private String startDate;
    private String endDate;
    private String recur;
    private UniqueTagList tags;
    private boolean marked;

    public TestTask() {
        tags = new UniqueTagList();
    }
    
    //@@author A0124797R
    public void setName(String name) {
        this.name = name;
    }
    
    //@@author A0124797R
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    //@@author A0124797R
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    //@@author A0124797R
    public void setRecur(String recur) {
        this.recur = recur;
    }


    //@@author A0124797R
    public String getAddCommand() {
        StringBuilder sb = new StringBuilder();
        sb.append("add");
        sb.append(" '" + this.getName() + "' ");
        if (startDate!=null) {
            sb.append("sd/'" + startDate + "' ");
        }
        if (endDate!=null) {
            sb.append("ed/'" + endDate + "' ");
        }
        sb.append("t/'");
        this.getTags().getInternalList().stream().forEach(s -> sb.append(s.tagName + ","));

        sb.deleteCharAt(sb.length()-1);
        sb.append("'");
        return sb.toString();
        
    }

    //@@author A0124797R
    @Override
    public String getName() {
        return name;
    }

    //@@author A0124797R
    @Override
    public Date getStartDate() {
        if (startDate!=null) {
            return TaskManagerGuiTest.prettyTimeParser.parse(startDate).get(0);
        }else {
            return null;
        }
    }

    //@@author A0124797R
    @Override
    public Date getEndDate() {
        if (endDate!=null) {
            return TaskManagerGuiTest.prettyTimeParser.parse(endDate).get(0);
        }else {
            return null;
        }
    }
    
    @Override
    //@@author A0124797R
    public String getRecur() {
        return recur;
    }
    
    @Override
    //@@author A0124797R
    public boolean isRecur() {
        return recur!=null;
    }

    @Override
    // @@author A0124797R
    public boolean isFloating() {
        return startDate == null && endDate == null;
    }

    @Override
    // @@author A0124797R
    public boolean isDeadline() {
        return startDate == null && endDate != null;
    }

    @Override
    // @@author A0124797R
    public boolean isEvent() {
        return startDate != null && endDate != null;
    }

    @Override
    //@@author A0124797R
    public boolean isMarked() {
        return this.marked;
    }
    
    @Override
    public UniqueTagList getTags() {
        return tags;
    }
    
    //@@author A0124797R
    @Override 
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Task // instanceof handles nulls
                && this.toString().equals(((Task) other).toString())); // state check
        
    }
    
    //@@author A0124797R
    @Override
    public boolean isSameTask(ReadOnlyTask task) {
        return this.toString().equals(((Task) task).toString());
    }
    
    //@@author A0124797R
    @Override
    public String toString() {
        return getAsText();
    }
    
    //@@author A0124797R
    public TestTask mark() {
        this.marked = true;
        return this;
    }
    
}
