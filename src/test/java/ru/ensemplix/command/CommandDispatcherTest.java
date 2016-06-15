package ru.ensemplix.command;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.ensemplix.command.argument.Argument;
import ru.ensemplix.command.argument.ArgumentParser.EnumArgumentParser;
import ru.ensemplix.command.exception.CommandAccessException;
import ru.ensemplix.command.exception.CommandException;
import ru.ensemplix.command.exception.CommandNotFoundException;
import ru.ensemplix.command.region.Region;
import ru.ensemplix.command.region.RegionArgumentParser;
import ru.ensemplix.command.region.RegionCommand;
import ru.ensemplix.command.region.RegionCompleter;
import ru.ensemplix.command.simple.SimpleCommand;
import ru.ensemplix.command.simple.SimpleSender;

import java.util.List;

import static org.junit.Assert.*;
import static ru.ensemplix.command.argument.Argument.Result.FAIL;
import static ru.ensemplix.command.argument.Argument.Result.SUCCESS;
import static ru.ensemplix.command.simple.SimpleCommand.SimpleEnum;
import static ru.ensemplix.command.simple.SimpleCommand.SimpleEnum.WORLD;

public class CommandDispatcherTest {

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    private final CommandDispatcher dispatcher = new CommandDispatcher();
    private final CommandSender sender = new SimpleSender();

    @Before
    public void setUp() {
        dispatcher.bind(SimpleEnum.class, new EnumArgumentParser(SimpleEnum.class));
        dispatcher.bind(Region.class, new RegionArgumentParser());
        dispatcher.bind(Region.class, new RegionCompleter());
    }

    @Test
    public void testCommandRegister() {
        SimpleCommand command = new SimpleCommand();

        dispatcher.register(command, "register");
        assertTrue(CommandDispatcher.commands.containsKey("register"));
    }

    @Test
    public void testCommandUnregister() {
        SimpleCommand command = new SimpleCommand();

        dispatcher.register(command, "unregister");
        dispatcher.unregister(SimpleCommand.class);

        assertFalse(CommandDispatcher.commands.containsKey("unregister"));
    }

    @Test
    public void testDispatcher() throws CommandException {
        SimpleCommand command = new SimpleCommand();
        dispatcher.register(command, "test", "test2");

        assertTrue(call("/test"));
        assertTrue(call("/test2"));
        assertFalse(call("/test hello"));
        assertTrue(call("/test2 integer 36"));
        assertFalse(call("/test integer"));
        assertTrue(call("/test2 string koala"));
        assertTrue(call("/test collection i love ensemplix <3"));
        assertTrue(call("/test2 argument koala"));
        assertTrue(call("/test2 argument2"));
        assertTrue(call("/test2 enumm world"));

        assertTrue(command.hello && command.test);
        assertEquals(36, command.integer);
        assertEquals("koala", command.string);
        assertEquals(SUCCESS, command.argument.getResult());
        assertEquals("koala", command.argument.getValue());
        assertEquals(FAIL, command.argument2.getResult());
        assertNull(command.argument2.getValue());
        assertEquals(WORLD, command.enumm);

        assertArrayEquals(new String[] {"i", "love", "ensemplix", "<3"}, command.strings.toArray());
    }

    @Test
    public void testCommandResult() throws CommandException {
        SimpleCommand command = new SimpleCommand();
        dispatcher.register(command, "test", "test2");

        CommandResult result = dispatcher.call(sender, "/test2 integer 36");
        CommandContext context = result.getContext();
        List<Argument<?>> arguments = result.getArguments();

        assertTrue(result.isSuccess());
        assertEquals("test", context.getCommandName());
        assertEquals("integer", context.getActionName());
        assertTrue(arguments.size() == 1);
        assertEquals(SUCCESS, arguments.get(0).getResult());
        assertEquals(36, arguments.get(0).getValue());
        assertEquals("36", arguments.get(0).getText());
    }

    @Test
    public void testTypeParser() throws CommandException {
        RegionCommand region = new RegionCommand();

        dispatcher.bind(Region.class, new RegionArgumentParser());
        dispatcher.register(region, "parser");

        assertTrue(call("/parser Project:Id"));
        assertEquals("Project:Id", region.name);
    }

    @Test
    public void testTypeParserIterable() throws CommandException {
        RegionCommand region = new RegionCommand();

        dispatcher.bind(Region.class, new RegionArgumentParser());
        dispatcher.register(region, "iterable");

        assertTrue(call("/iterable list home spawn koala"));
        assertEquals(3, region.list.size());
    }

    @Test
    public void testCompleteObject() throws CommandException {
        RegionCommand region = new RegionCommand();
        dispatcher.register(region, "object2");

        String[] regions = dispatcher.complete(sender, "/object2").toArray(new String[4]);

        assertEquals("home", regions[0]);
        assertEquals("spawn", regions[1]);
        assertEquals("spawn123", regions[2]);
        assertEquals("spb", regions[3]);
    }

    @Test
    public void testCompleteObjectPartial() throws CommandException {
        RegionCommand region = new RegionCommand();
        dispatcher.register(region, "object");

        String[] regions = dispatcher.complete(sender, "/object sp").toArray(new String[3]);

        assertEquals("spawn", regions[0]);
        assertEquals("spawn123", regions[1]);
        assertEquals("spb", regions[2]);
    }

    @Test
    public void testCompleteIterable() throws CommandException {
        RegionCommand region = new RegionCommand();
        dispatcher.register(region, "iterable2");

        String[] regions = dispatcher.complete(sender, "/iterable2 list").toArray(new String[4]);

        assertEquals("home", regions[0]);
        assertEquals("spawn", regions[1]);
        assertEquals("spawn123", regions[2]);
        assertEquals("spb", regions[3]);
    }

    @Test
    public void testCompleteIterablePartial() throws CommandException {
        RegionCommand region = new RegionCommand();
        dispatcher.register(region, "iterable3");

        String[] regions = dispatcher.complete(sender, "/iterable3 list sp").toArray(new String[3]);

        assertEquals("spawn", regions[0]);
        assertEquals("spawn123", regions[1]);
        assertEquals("spb", regions[2]);
    }

    @Test
    public void testCompleteActions() throws CommandException {
        dispatcher.register(new Actions(), "actions");

        String[] actions = dispatcher.complete(sender, "/actions").toArray(new String[4]);

        assertEquals("add", actions[0]);
        assertEquals("addMember", actions[1]);
        assertEquals("view", actions[2]);
        assertEquals("list", actions[3]);
    }

    @Test
    public void testCompleteActionsPartial() throws CommandException {
        dispatcher.register(new Actions(), "actions2");

        String[] actions = dispatcher.complete(sender, "/actions2 ad").toArray(new String[2]);

        assertEquals("add", actions[0]);
        assertEquals("addMember", actions[1]);
    }

    @Test
    public void testCompleteCommand() {
        dispatcher.register(new SimpleCommand(), "simple");
        dispatcher.register(new SimpleCommand(), "simple2");

        String[] commands = dispatcher.complete(sender, "/sim").toArray(new String[2]);

        assertEquals("simple", commands[0]);
        assertEquals("simple2", commands[1]);
    }

    @Test
    public void testRegisterNoObject() {
        expected.expect(NullPointerException.class);
        expected.expectMessage("Please provide command object");

        dispatcher.register(null, "name");
    }

    @Test
    public void testRegisterNameNull() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Please provide valid command name");

        dispatcher.register(new Object(), "name", null);
    }

    @Test
    public void testRegisterNameEmpty() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Please provide valid command name");

        dispatcher.register(new Object(), "");

    }

    @Test
    public void testRegisterNameWhitespace() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Please provide command name with no whitespace");

        dispatcher.register(new Object(), "test test");
    }

    @Test
    public void testRegisterAlreadyExists() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Command with name exists already exists");

        dispatcher.register(new SimpleCommand(), "exists");
        dispatcher.register(new SimpleCommand(), "exists");
    }

    @Test
    public void testRegisterNoAnnotations() {
        expected.expect(IllegalStateException.class);
        expected.expectMessage("Not found any method marked with @Command");

        dispatcher.register(new Object());
    }

    @Test
    public void testRegisterInvalidReturn() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("invalidReturn must return void or boolean");

        dispatcher.register(new InvalidReturn());
    }

    @Test
    public void testRegisterNoSender() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Please provide command sender for noSender");

        dispatcher.register(new NoSender());
    }

    @Test
    public void testRegisterNoTypeParser() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Please provide type parser for class java.lang.Object");

        dispatcher.register(new NoTypeParser());
    }

    @Test
    public void testRegisterIterableLastParameter() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Iterable must be last parameter in iterableLastParameter");

        dispatcher.register(new IterableLastParameter());
    }

    @Test
    public void testCallSenderNull() throws CommandException {
        expected.expect(NullPointerException.class);
        expected.expectMessage("Please provide command sender");

        dispatcher.call(null, null);
    }

    @Test(expected = CommandNotFoundException.class)
    public void testCallCmdNull() throws CommandException {
        call(null);
    }

    @Test(expected = CommandNotFoundException.class)
    public void testCallCmdEmpty() throws CommandException {
        call("");
    }

    @Test(expected = CommandNotFoundException.class)
    public void testCallCmdEmptyPrefixed() throws CommandException {
        call("/");
    }

    @Test(expected = CommandNotFoundException.class)
    public void testCallCommandNotFound() throws CommandException {
        call("not existing command");
    }

    @Test(expected = CommandNotFoundException.class)
    public void testCallCommandNoMain() throws CommandException {
        dispatcher.register(new NoMain(), "no_main");
        call("/no_main");
    }

    @Test(expected = CommandAccessException.class)
    public void testCallCommandNoAccess() throws CommandException {
        dispatcher.register(new Access(), "access");
        call("/access");
    }

    @Test
    public void testCallMain() throws CommandException {
        Main main = new Main();

        dispatcher.register(main, "main");
        call("/main bro");

        assertTrue(main.main);
    }

    @Test(expected = RuntimeException.class)
    public void testCallPropagateException() throws CommandException {
        dispatcher.register(new PropagateException(), "exception");
        call("/exception");
    }

    public boolean call(String command) throws CommandException {
        return dispatcher.call(sender, command).isSuccess();
    }

    public class InvalidReturn {
        @Command
        public Object invalidReturn() {
            return new Object();
        }
    }

    public class NoSender {
        @Command
        public boolean noSender() {
            return true;
        }
    }

    public class NoTypeParser {
        @Command
        public boolean noTypeParser(CommandSender sender, Object obj) {
            return true;
        }
    }

    public class IterableLastParameter {
        @Command
        public boolean iterableLastParameter(CommandSender sender, Iterable<String> iterable, int value) {
            return true;
        }
    }

    public class NoMain {
        @Command
        public void nomain(CommandSender sender) {
            fail();
        }
    }

    public class Main {
        public boolean main;

        @Command(main = true)
        public void expectedCall(CommandSender sender) {
            main = true;
        }
    }

    public class Access {
        @Command(main = true, permission = true)
        public void access(CommandSender sender) {

        }
    }

    public class PropagateException {
        @Command(main = true)
        public void exception(CommandSender sender) {
            throw new RuntimeException();
        }
    }

    public class Actions {
        @Command
        public void list(CommandSender sender) {

        }

        @Command
        public void view(CommandSender sender) {

        }

        @Command
        public void add(CommandSender sender) {

        }

        @Command
        public void addMember(CommandSender sender) {

        }
    }

}
