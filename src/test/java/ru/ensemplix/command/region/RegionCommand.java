package ru.ensemplix.command.region;

import ru.ensemplix.command.Command;
import ru.ensemplix.command.CommandSender;

import java.util.Collection;

public class RegionCommand {

    public String name;
    public Collection<Region> list;

    @Command(main = true)
    public void region(CommandSender sender, Region region) {
        this.name = region.name;
    }

    @Command
    public void list(CommandSender sender, Collection<Region> list) {
        this.list = list;
    }

}
