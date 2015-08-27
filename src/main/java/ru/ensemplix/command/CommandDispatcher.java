package ru.ensemplix.command;

import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class CommandDispatcher {

    private final Multimap<String, CommandHandler> commands = ArrayListMultimap.create();

    private final Map<Class, TypeParser> parsers = new HashMap<>();

    private final boolean prefix;

    public CommandDispatcher() {
        this(true);
    }

    public CommandDispatcher(boolean prefix) {
        this.prefix = prefix;

        bind(String.class, new StringParser());
        bind(Integer.class, new IntegerParser());
        bind(int.class, new IntegerParser());
        bind(Boolean.class, new BooleanParser());
        bind(boolean.class, new BooleanParser());
    }

    public boolean call(CommandSender sender, String cmd) throws CommandNotFoundException {
        checkNotNull(sender, "Please provide command sender");
        checkNotNull(cmd, "Please provide command line");
        checkArgument(cmd.length() > 1, "Please provide valid command line");

        if(prefix) {
            cmd = cmd.substring(1);
        }

        String[] args = cmd.split(" ");
        CommandHandler command = getCommand(args[0], null);
        int argsFrom = 0;

        if(args.length > 1) {
            CommandHandler subcommand = getCommand(args[0], args[1]);
            argsFrom = 1;

            if(subcommand != null) {
                command = subcommand;
            }
        }

        if(command == null) {
            throw new CommandNotFoundException();
        }

        Method method = command.getMethod();
        Parameter[] parameters = method.getParameters();
        int length = parameters.length;

        Object[] parsed = new Object[length];
        parsed[0] = sender;

        for (int i = 1; i < length; i++) {
            TypeParser parser = parsers.get(parameters[i].getType());

            if(args.length - 1 >= argsFrom + i) {
                parsed[i] = parser.parse(args[argsFrom + i]);
            } else {
                parsed[i] = parser.parse(null);
            }
        }

        try {
            Object result = method.invoke(command.getObject(), parsed);
            return result == null || (boolean) result;
        } catch (Exception e) {
            Throwables.propagate(e);
        }

        return false;
    }

    public void register(Object obj, String... names) {
        checkNotNull(obj, "Please provide valid command");
        checkNotNull(names, "Please provide valid command name");

        for (Method method : obj.getClass().getMethods()) {
            Command annotation = method.getAnnotation(Command.class);

            if(annotation == null) {
                continue;
            }

            if(method.getReturnType() != void.class && method.getReturnType() != boolean.class) {
                throw new IllegalArgumentException(method + " must return void or boolean");
            }

            Parameter[] parameters = method.getParameters();
            int length = parameters.length;

            if(length == 0 || !CommandSender.class.isAssignableFrom(parameters[0].getType())) {
                throw new IllegalArgumentException("Please provide command sender for " + method);
            }

            for(int i = 1; i < length; i++) {
                if(!parsers.containsKey(parameters[i].getType())) {
                    throw new IllegalArgumentException("Please provide type parser for " + parameters[i].getType());
                }
            }

            CommandHandler handler = new CommandHandler(names[0], method.getName(), method, obj);

            for(String name : names) {
                commands.put(name, handler);
            }
        }
    }

    public void bind(Class<?> clz, TypeParser parser) {
        parsers.put(clz, parser);
    }

    private CommandHandler getCommand(String command, String subCommand) {
        for (CommandHandler handler : commands.get(command)) {
            if(subCommand == null) {
                if(handler.getOrigin().equals(handler.getName())) {
                    return handler;
                }
            } else if (handler.getName().equals(subCommand)) {
                return handler;
            }
        }

        return null;
    }

    private class StringParser implements TypeParser<String> {
        @Override
        public String parse(String value) {
            return value;
        }
    }

    private class IntegerParser implements TypeParser<Integer> {
        @Override
        public Integer parse(String value) {
            try {
                return Integer.parseInt(value);
            } catch(NumberFormatException e) {
                return 0;
            }
        }
    }

    private class BooleanParser implements TypeParser<Boolean> {
        @Override
        public Boolean parse(String value) {
            try {
                return Boolean.parseBoolean(value);
            } catch(NumberFormatException e) {
                return false;
            }
        }
    }

}
