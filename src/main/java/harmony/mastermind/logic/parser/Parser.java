package harmony.mastermind.logic.parser;

import static harmony.mastermind.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static harmony.mastermind.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;
import static harmony.mastermind.commons.core.Messages.MESSAGE_INVALID_TAB;


import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

import harmony.mastermind.commons.exceptions.IllegalValueException;
import harmony.mastermind.commons.util.StringUtil;
import harmony.mastermind.logic.commands.*;
import harmony.mastermind.model.ModelManager;
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
    private static final Pattern TASK_ARCHIVE_ARGS_FORMAT = Pattern.compile("(?<type>[^/]+)");

    public Parser() {
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput
     *            full user input string
     * @return the command based on the user input
     */
    public Command parseCommand(String userInput, String currentTab) {
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

            case DeleteCommand.COMMAND_WORD:
                return prepareDelete(arguments);

            case ClearCommand.COMMAND_WORD:
                return new ClearCommand();

            case FindCommand.COMMAND_WORD:
                return prepareFind(arguments);

            case FindTagCommand.COMMAND_WORD:
                return prepareFindTag(arguments);

            case ListCommand.COMMAND_WORD:
                return prepareList(arguments);

            case PreviousCommand.COMMAND_WORD:
                return new PreviousCommand();

            case MarkCommand.COMMAND_WORD:
                return prepareMark(arguments, currentTab);

            case EditCommand.COMMAND_KEYWORD_EDIT:
            case EditCommand.COMMAND_KEYWORD_UPDATE:
                return prepareEdit(arguments);

            case UndoCommand.COMMAND_WORD:
                return new UndoCommand();
                
            case UnmarkCommand.COMMAND_WORD:
                return prepareUnmark(arguments, currentTab);

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
    // @@author A0138862W
    private Command prepareAdd(String args) {
        final Matcher matcher = AddCommand.COMMAND_ARGUMENTS_PATTERN.matcher(args.trim());

        // Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        try {

            // mandatory
            // there's no need to check for existence as the regex only capture full match of mandatory components
            final String name = matcher.group("name");

            // optionals
            final Optional<String> startDate = Optional.ofNullable(matcher.group("startDate"));
            final Optional<String> endDate = Optional.ofNullable(matcher.group("endDate"));
            final Optional<String> tags = Optional.ofNullable(matcher.group("tags"));

            // return internal value if present. else, return empty string
            Set<String> tagSet = getTagsFromArgs(tags.map(val -> val).orElse(""));

            if (startDate.isPresent() && endDate.isPresent()) {
                // event
                return new AddCommand(name, startDate.get(), endDate.get(), tagSet);
            } else if (!startDate.isPresent() && endDate.isPresent()) {
                // deadline
                return new AddCommand(name, endDate.get(), tagSet);
            } else if (startDate.isPresent() && !endDate.isPresent()) {
                // task with only startdate is not supported.
                throw new IllegalValueException("Cannot create a task with only start date.");
            } else {
                // floating
                return new AddCommand(name, tagSet);
            }

        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        } catch (ParseException pe) {
            pe.printStackTrace();
            return new IncorrectCommand(pe.getMessage());
        }
    }
    
    /**
     * Parses arguments in the context of the edit task command.
     * 
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareEdit(String args) {
        final Matcher matcher = EditCommand.COMMAND_ARGUMENTS_PATTERN.matcher(args.trim());
        // Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        try {
            
            // mandatory
            // regex accept only numbers in index field, encountering NumberFormatException is impossible
            final int index = Integer.parseInt(matcher.group("index"));
            
            //optional
            final Optional<String> name = Optional.ofNullable(matcher.group("name"));
            final Optional<String> startDate = Optional.ofNullable(matcher.group("startDate"));
            final Optional<String> endDate = Optional.ofNullable(matcher.group("endDate"));
            final Optional<String> tags = Optional.ofNullable(matcher.group("tags"));
            
            Optional<Set<String>> tagSet = Optional.empty();
            if(tags.isPresent()){
                tagSet = Optional.ofNullable(getTagsFromArgs(tags.get()));
            };
            
            return new EditCommand(index, name, startDate, endDate, tagSet);
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        } catch (ParseException pe) {
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
    //@@author A0124797R
    private Command prepareMark(String args, String currentTab) {

        Optional<Integer> index = parseIndex(args);
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkCommand.MESSAGE_USAGE));
        }
        return new MarkCommand(index.get(), currentTab);
    }
    
    /**
     * Parses arguments in the context of the list task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    //@@author A0124797R
    private Command prepareList(String args) {
        Optional<String> type = parseType(args);
        if (!type.isPresent()) {
            return new ListCommand();
        }else {
            if (type.get().equals(ListCommand.LISTING_ARCHIVES)) {
                return new ListCommand(type);
            }else {
                return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
            }
        }
    }

    /**
     * Parses arguments in the context of the unmark task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    //@@author A0124797R
    private Command prepareUnmark(String args, String currentTab) {
        if (!currentTab.equals("Archives")) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_TAB, UnmarkCommand.COMMAND_WORD, UnmarkCommand.MESSAGE_USAGE));
        }

        Optional<Integer> index = parseIndex(args);
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnmarkCommand.MESSAGE_USAGE));
        }
        return new UnmarkCommand(index.get());
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
     * checks if have the type to list archive
     */
    private Optional<String> parseType(String command) {
        final Matcher matcher = TASK_ARCHIVE_ARGS_FORMAT.matcher(command.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String type = matcher.group("type");

        return Optional.of(type);

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