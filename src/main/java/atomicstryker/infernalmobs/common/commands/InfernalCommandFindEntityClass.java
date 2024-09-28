package atomicstryker.infernalmobs.common.commands;

import java.util.Collection;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLCommonHandler;

public class InfernalCommandFindEntityClass extends CommandBase {

    @Override
    public String getCommandName() {
        return "feclass";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/feclass X returns all currently registered Entities containing X in their classname's";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            throw new WrongUsageException("Invalid Usage of FindEntityClass command", (Object) args);
        } else {
            StringBuilder classname = new StringBuilder(args[0]);
            for (int i = 1; i < args.length; i++) {
                classname.append(" ")
                    .append(args[i]);
            }

            StringBuilder result = new StringBuilder("Found Entity classes: ");
            final Collection<String> classes = EntityList.classToStringMapping.values();
            boolean found = false;
            for (String entclass : classes) {
                if (entclass.toLowerCase()
                    .contains(
                        classname.toString()
                            .toLowerCase())) {
                    if (!found) {
                        result.append(entclass);
                        found = true;
                    } else {
                        result.append(", ")
                            .append(entclass);
                    }
                }
            }

            if (!found) {
                result.append("Nothing found.");
            }

            FMLCommonHandler.instance()
                .getFMLLogger()
                .log(Level.INFO, sender.getCommandSenderName() + ": " + result);
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ICommand) {
            return ((ICommand) o).getCommandName()
                .compareTo(getCommandName());
        }
        return 0;
    }

}
