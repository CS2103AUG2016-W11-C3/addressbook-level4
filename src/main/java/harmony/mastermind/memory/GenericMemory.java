package harmony.mastermind.memory;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class GenericMemory implements Comparable<GenericMemory> {
    private static final String TYPE_STRING = "Type: ";
    private static final String NAME_STRING = "\nName: ";
    private static final String DESCRIPTION_STRING = "\nDescription : ";
    private static final String START_STRING = "\nStart: ";
    private static final String END_STRING = "\nEnd: ";
    private static final String DUE_BY = "\nDue by: ";
    private static final String STATUS_INCOMPLETE = "\nStatus: Incomplete";
    private static final String STATUS_COMPLETED = "\nStatus: Completed";
    private static final String STATUS_OVERDUE = "\nStatus: Overdue";
    private static final String STATUS_UPCOMING = "\nStatus: Upcoming";
    private static final String STATUS_OVER = "\nStatus: Over";
    private static final String STATUS_ONGOING = "\nStatus: Ongoing";
    private static final String PM = "PM";
    private static final String AM = "AM";
    private static final String SUN = "Sun";
    private static final String SAT = "Sat";
    private static final String FRI = "Fri";
    private static final String THURS = "Thurs";
    private static final String WED = "Wed";
    private static final String TUES = "Tues";
    private static final String MON = "Mon";
    private static final String EVENT = "Event";
    private static final String DEADLINE = "Deadline";
    private static final String TASK = "Task";

    private static final String ONGOING = "Ongoing";
    private static final String OVER = "Over";
    private static final String UPCOMING = "Upcoming";
    private static final String OVERDUE = "Overdue";
    private static final String COMPLETED = "Completed";
    private static final String INCOMPLETE = "Incomplete";

    private String type;
    private String name;
    private String description;
    private Calendar start;
    private Calendar end;
    private int state;  

    // State
    public static final int INT_OVERDUE = 2;
    public static final int INT_ONGOING = 2;
    public static final int INT_COMPLETED = 1;
    public static final int INT_OVER = 1;
    public static final int INT_INCOMPLETE = 0;
    public static final int INT_UPCOMING = 0;

    //@@author A0143378Y
    // Setting up tasks
    public GenericMemory(String type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.state = 0;
    }

    //@@author A0143378Y
    // Setting up deadlines 
    public GenericMemory(String type, String name, String description, Calendar end) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.end = end;
        this.state = 0;
    }

    //@@author A0143378Y
    // Events and constructor used to load from storage
    public GenericMemory(String type, String name, String description, Calendar startDate, Calendar end, int state) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.start = startDate;
        this.end = end;
        this.state = state;
    }

    //@@author A0143378Y
    // Returns type of the to do item
    public String getType() {
        return type;
    }

    //@@author A0143378Y
    // Returns name of the to do item
    public String getName() {
        return name;
    }

    //@@author A0143378Y
    // Returns description of the to do item
    public String getDescription() {
        return description;
    }

    //@@author A0143378Y
    // Returns Calendar start of the to do item
    public Calendar getStart() {
        return start;
    }

    //@@author A0143378Y
    // Returns Calendar end of the to do item
    public Calendar getEnd() {
        return end;
    }

    //@@author A0143378Y
    // Returns the state of the to do item
    public int getState() {
        return state;
    }

    //@@author A0143378Y
    // Initializes start calendar - having a real calendar instead of hard coding everything
    public void initStart(){
        start = new GregorianCalendar();
    }

    //@@author A0143378Y
    // Initializes end calendar
    public void initEnd(){
        end = new GregorianCalendar();
    }

    //@@author A0143378Y
    // Set type of to do item
    public void setType(String type) {
        this.type = type;
    }

    //@@author A0143378Y
    // Set name of to do item
    public void setName(String name) {
        this.name = name;
    }

    //@@author A0143378Y
    // Set description of to do item
    public void setDescription(String description) {
        this.description  = description;
    }

    //@@author A0143378Y
    // Set start time of to do item using hours and minutes
    public void setStartTime(int hourOfDay, int minute) {
        start.set(Calendar.HOUR_OF_DAY, hourOfDay);
        start.set(Calendar.MINUTE, minute);
    }

    //@@author A0143378Y
    // Set start time of to do item using hours, minutes and seconds
    public void setStartTime(int hourOfDay, int minute, int second) {
        start.set(Calendar.HOUR_OF_DAY, hourOfDay);
        start.set(Calendar.MINUTE, minute);
        start.set(Calendar.SECOND, second);
    }

    //@@author A0143378Y
    // Set end time of to do item using hours and minutes
    public void setEndTime(int hourOfDay, int minute) {
        end.set(Calendar.HOUR_OF_DAY, hourOfDay);
        end.set(Calendar.MINUTE, minute);
    }

    //@@author A0143378Y
    // Set end time of to do item using hours, minutes and seconds
    public void setEndTime(int hourOfDay, int minute, int second) {
        end.set(Calendar.HOUR_OF_DAY, hourOfDay);
        end.set(Calendar.MINUTE, minute);
        end.set(Calendar.SECOND, second);
    }

    //@@author A0143378Y
    // Set start date of to do item
    public void setStartDate(int year, int month, int date) {
        start.set(Calendar.YEAR, year);
        start.set(Calendar.MONTH, month);
        start.set(Calendar.DATE, date);
    }

    //@@author A0143378Y
    // Set end date of to do item
    public void setEndDate(int year, int month, int date) {
        end.set(Calendar.YEAR, year);
        end.set(Calendar.MONTH, month);
        end.set(Calendar.DATE, date);
    }

    //@@author A0143378Y
    // Set state of to do item
    public void setState(int newstate) {
        this.state = newstate;
    }

    //@@author A0143378Y
    /* Converts GenericEvents object into string representation
     * Outputs in the format
     * Name
     * Description
     * Start
     * End
     * State
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String output = TYPE_STRING + getType() + 
                        NAME_STRING + getName();
        output = descriptionToString(output);

        if (getType().equals(TASK)) { // Task
            output = taskDeadlineStateToString(output);
            return output;
        }

        if (getType().equals(DEADLINE)) { // Deadline
            output = deadlineDateToString(output);
            output = taskDeadlineStateToString(output);
            return output;
        }

        if (getType().equals(EVENT)) { // Event
            output = eventDatesToString(output);
            output = eventStateToString(output);
            return output;
        }
        return output;
    }
    
    //@@author A0143378Y
    // Converts description into string representation
    String descriptionToString(String output) {
        if (description != null) { // If description exists
            output +=   DESCRIPTION_STRING + getDescription();
        }
        return output;
    }

    //@@author A0143378Y
    // Converts due date into string representation
    String deadlineDateToString(String output) {
        if (end != null) {
            output += DUE_BY + getDate(end) + " " + getTime(end);
        }
        return output;
    }

    //@@author A0143378Y
    // Converts event start and end dates into string representation
    String eventDatesToString(String output) {
        if (start != null) {
            output += START_STRING + getDate(start) + " " + getTime(start);
        }
        if (end != null) {
            output += END_STRING + getDate(end) + " " + getTime(end);
        }
        return output;
    }

    //@@author A0143378Y
    // Converts event state into string representation
    String eventStateToString(String output) {
        if (getState() == 0) { // Printing of state into string
            output+= STATUS_UPCOMING;
        } else if (getState() == 1) {
            output+= STATUS_OVER;
        } else {
            output+= STATUS_ONGOING;
        }
        return output;
    }

    //@@author A0143378Y
    // Converts task or deadline state into string representation
    String taskDeadlineStateToString(String output) {
        if (getState() == 0) { // Printing of state into string
            output+= STATUS_INCOMPLETE;
        } else if (getState() == 1) {
            output+= STATUS_COMPLETED;
        } else {
            output+= STATUS_OVERDUE;
        }
        return output;
    }

    //@@author A0143378Y
    // Returns string representation of date as DD/MM/YY
    public static String getDate(Calendar item){
        if(item != null){
            return item.get(Calendar.DATE) + "/"
                    + (item.get(Calendar.MONTH) + 1) + "/"
                    + (item.get(Calendar.YEAR)%100) + " "
                    + dayOfTheWeek(item);
        }else{
            return null;
        }
    }

    //@@author A0143378Y
    // Returns string representation of time as HH:MM AM/PM
    public static String getTime(Calendar item){
        if(item != null){
            return hour(item) + ":"
                    + min(item) + " "
                    + AM_PM(item);
        }else{
            return null;
        }
    }

    //@@author A0143378Y
    // Return string representation of day of the week of calendar object
    public static String dayOfTheWeek(Calendar item){
        int dayOfTheWeek = item.get(Calendar.DAY_OF_WEEK);

        switch(dayOfTheWeek){
        case Calendar.MONDAY:
            return MON;

        case Calendar.TUESDAY:
            return TUES;

        case Calendar.WEDNESDAY:
            return WED;

        case Calendar.THURSDAY:
            return THURS;

        case Calendar.FRIDAY:
            return FRI;

        case Calendar.SATURDAY:
            return SAT;

        case Calendar.SUNDAY:
            return SUN;
        }
        return null;
    }

    //@@author A0143378Y
    // Check item for AM/PM and return the correct time period
    public static String AM_PM(Calendar item){
        if(item.get(Calendar.AM_PM) == Calendar.AM){
            return AM;
        }else{
            return PM;
        }
    }

    //@@author A0143378Y
    // Return the hour of time to form of HH
    public static String hour(Calendar item){
        if(item.get(Calendar.HOUR_OF_DAY)==12){
            return "12";
        }else if( item.get(Calendar.HOUR)<10){
            return "0" + item.get(Calendar.HOUR);
        }else{
            return Integer.toString(item.get(Calendar.HOUR));
        }
    }

    //@@author A0143378Y
    // Return minute of time to form of MM
    public static String min(Calendar item){
        if( item.get(Calendar.MINUTE)<10){
            return "0" + item.get(Calendar.MINUTE);
        }else{
            return Integer.toString(item.get(Calendar.MINUTE));
        }
    }

    //@@author A0143378Y
    /* Comparator between to do item and o
     * Only valid when comparing items of the same type
     * eg. Task vs Task, Deadline vs Deadline, Event vs Event
     * Task: Compare names lexicographically, ignoring case differences -> alphabetical order 
     * Deadline: Compare due dates
     *  Event: Compare start dates, else end dates
     */
    public int compareTo(GenericMemory o) {
        if (this.start == null && this.end == null && o.start == null && o.end == null){ //both task
            return this.name.compareToIgnoreCase(o.name);
        }

        if (this.start == null && this.end != null && o.start == null && o.end != null){ //both deadline
            return this.end.compareTo(o.end);
        }

        if (this.start != null && this.end != null && o.start != null && o.end != null){ //both event
            return eventCompare(o);
        }
        return 0;
    }

    //@@author A0143378Y
    // Compare's start date followed by end dates
    int eventCompare(GenericMemory o) {
        if (this.start.compareTo(o.start) != 0) {
            return this.start.compareTo(o.start);
        } else {
            return this.end.compareTo(o.end);
        }
    }

    //@@author A0143378Y
    // Return state of the item in the form of a string
    public String getStateType(){

        if(type.equals(DEADLINE)|| type.equals(TASK)){
            if(state == INT_INCOMPLETE){
                return INCOMPLETE;
            }else if (state == INT_COMPLETED){
                return COMPLETED;
            }else if (state == INT_OVERDUE){
                return OVERDUE;
            }
        }
        if(type.equals(EVENT)){
            if(state == INT_UPCOMING){
                return UPCOMING;
            }else if(state == INT_OVER){
                return OVER;
            }else if (state == INT_ONGOING){
                return ONGOING;
            }
        }
        return null;
    }
}