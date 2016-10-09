package harmony.mastermind.logic.parser;

import static harmony.mastermind.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static harmony.mastermind.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

import harmony.mastermind.commons.exceptions.IllegalValueException;
import harmony.mastermind.commons.util.StringUtil;
import harmony.mastermind.logic.commands.*;
import harmony.mastermind.model.tag.Tag;

/**
 * Parses user input.
 */
public class Parser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<keyword>\\S+)(?<arguments>.*)");

    private static final Pattern KEYWORDS_ARGS_FORMAT = Pattern.compile("(?<keywords>\\S+(?:\\s+\\S+)*)"); // one
                                                                                                           // or
                                                                                                           // more
                                                                                                           // keywords
                                                                                                           // separated
                                                                                                           // by
                                                                                                           // whitespace

    private static final Pattern TASK_DATA_ARGS_FORMAT = // '/' forward slashes
                                                         // are reserved for
                                                         // delimiter prefixes
            Pattern.compile(
                    "(?<name>[^/]+)" + " at/(?<time>[^/]+)" + " on/(?<date>[^/]+)" + "(?<tagArguments>(?: t/[^/]+)*)"); // variable
                                                                                                                        // number
                                                                                                                        // of
                                                                                                                        // tags

    private static final Pattern TASK_INDEX_ARGS_FORMAT = Pattern.compile("(?<targetIndex>.+)");

    public Parser() {
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput
     *            full user input string
     * @return the command based on the user input
     */
    public Command parseCommand(String userInput) {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());

        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String keyword = matcher.group("keyword");
        final String arguments = matcher.group("arguments");

        switch (keyword) {

            case AddCommand.COMMAND_KEYWORD_ADD: // main command
            case AddCommand.COMMAND_KEYWORD_DO: // alias (fall through)
                return prepareAdd(arguments);

            case SelectCommand.COMMAND_WORD:
                return prepareSelect(arguments);

            case DeleteCommand.COMMAND_WORD:
                return prepareDelete(arguments);

            case ClearCommand.COMMAND_WORD:
                return new ClearCommand();

            case FindCommand.COMMAND_WORD:
                return prepareFind(arguments);

            case FindTagCommand.COMMAND_WORD:
                return prepareFindTag(arguments);

            case ListCommand.COMMAND_WORD:
                return new ListCommand();

            case PreviousCommand.COMMAND_WORD:
                return new PreviousCommand();

            case MarkCommand.COMMAND_WORD:
                return prepareMark(arguments);

            case EditCommand.COMMAND_WORD:
                return prepareEdit(arguments);

            case UndoCommand.COMMAND_WORD:
                return new UndoCommand();

            case ExitCommand.COMMAND_WORD:
                return new ExitCommand();

            case HelpCommand.COMMAND_WORD:
                return new HelpCommand();

            default:
                return new IncorrectCommand(MESSAGE_UNKNOWN_COMMAND);
        }
    }

    /**
     * Parses arguments in the context of the add task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    //@@author A0138862W
    private Command prepareAdd(String args) {
        final Matcher matcher = AddCommand.COMMAND_ARGUMENTS_PATTERN.matcher(args.trim());

        // Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        try {
            
            final String name = matcher.group("name"); // mandatory
            final String startDate = (Strings.isNullOrEmpty(matcher.group("startDate")))? "": matcher.group("startDate"); //optional
            final String endDate = (Strings.isNullOrEmpty(matcher.group("endDate")))? "": matcher.group("endDate"); //optional
            final String tags = (Strings.isNullOrEmpty(matcher.group("tags")))? "": matcher.group("tags"); //optional
            
            if(Strings.isNullOrEmpty(startDate) && Strings.isNullOrEmpty(endDate)){
                // floating
                return new AddCommand(name, getTagsFromArgs(tags));
            }else if(Strings.isNullOrEmpty(startDate) && !Strings.isNullOrEmpty(endDate)){
                // deadline
                return new AddCommand(name, endDate, getTagsFromArgs(tags));
            }else{
                // event
                return new AddCommand(name, startDate, endDate, getTagsFromArgs(tags));
            }
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        } catch (ParseException pe){
            pe.printStackTrace();
            return new IncorrectCommand(pe.getMessage());
        }
    }

    /**
     * Extracts the new task's tags from the add command's tag arguments string.
     * Merges duplicate tag strings.
     */
    private static Set<String> getTagsFromArgs(String tagArguments) throws IllegalValueException {
        // no tags
        if (Strings.isNullOrEmpty(tagArguments)) {
            return Collections.emptySet();
        }
        // replace first delimiter prefix, then split
        final Collection<String> tagStrings = Arrays.asList(tagArguments.split(","));
        return new HashSet<>(tagStrings);
    }

    /**
     * Parses arguments in the context of the delete task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareDelete(String args) {

        Optional<Integer> index = parseIndex(args);
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }
        Command result = new DeleteCommand(index.get());

        return result;
    }

    /**
     * Parses arguments in the context of the mark task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareMark(String args) {

        Optional<Integer> index = parseIndex(args);
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkCommand.MESSAGE_USAGE));
        }
        Command result = new MarkCommand(index.get());

        return result;
    }

    /**
     * Parses arguments in the context of the edit task command.
     * 
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareEdit(String args) {
        final Matcher matcher = TASK_DATA_ARGS_FORMAT.matcher(args.trim());
        // Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        Optional<Integer> index = parseIndex(args.substring(1, 2));
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        try {
            return new EditCommand(index.get(), matcher.group("name").substring(2), matcher.group("time"),
                    matcher.group("date"), getTagsFromArgs(matcher.group("tagArguments")));
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        } catch (ParseException pe) {
            return new IncorrectCommand(pe.getMessage());
            
        }

    }

    /**
     * Parses arguments in the context of the select task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareSelect(String args) {
        Optional<Integer> index = parseIndex(args);
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE));
        }

        return new SelectCommand(index.get());
    }

    /**
     * Returns the specified index in the {@code command} IF a positive unsigned
     * integer is given as the index. Returns an {@code Optional.empty()}
     * otherwise.
     */
    private Optional<Integer> parseIndex(String command) {
        final Matcher matcher = TASK_INDEX_ARGS_FORMAT.matcher(command.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String index = matcher.group("targetIndex");
        if (!StringUtil.isUnsignedInteger(index)) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(index));

    }

    /**
     * Parses arguments in the context of the find task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareFind(String args) {
        final Matcher matcher = KEYWORDS_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        // keywords delimited by whitespace
        final String[] keywords = matcher.group("keywords").split("\\s+");
        final Set<String> keywordSet = new HashSet<>(Arrays.asList(keywords));
        return new FindCommand(keywordSet);
    }

    /**
     * Parses arguments in the context of the find tag command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareFindTag(String args) {
        final Matcher matcher = KEYWORDS_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        // keywords delimited by whitespace
        final String[] keywords = matcher.group("keywords").split("\\s+");

        final Set<Tag> tagSet = new HashSet<>();

        try {

            for (String tagName : keywords) {
                tagSet.add(new Tag(tagName));
            }

            return new FindTagCommand(tagSet);
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }

}