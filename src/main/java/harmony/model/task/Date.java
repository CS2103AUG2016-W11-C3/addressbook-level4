package harmony.model.task;


import harmony.commons.exceptions.IllegalValueException;

/**
 * Represents a Task's date in the task manager.
 * Guarantees: immutable; is valid as declared in {@link #isValidDate(String)}
 */
public class Date {

    public static final String MESSAGE_DATE_CONSTRAINTS =
            "Task date should be in DDMM format";
    public static final String DATE_VALIDATION_REGEX = "\\d{4}";

    public final String value;

    /**
     * Validates given date.
     *
     * @throws IllegalValueException if given date string is invalid.
     */
    public Date(String date) throws IllegalValueException {
        assert date != null;
        date = date.trim();
        if (!isValidDate(date)) {
            throw new IllegalValueException(MESSAGE_DATE_CONSTRAINTS);
        }
        this.value = date;
    }

    /**
     * Returns if a given string is a valid date.
     */
    public static boolean isValidDate(String test) {
        return test.matches(DATE_VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Date // instanceof handles nulls
                && this.value.equals(((Date) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}