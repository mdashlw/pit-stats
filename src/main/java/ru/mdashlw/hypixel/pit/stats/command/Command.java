package ru.mdashlw.hypixel.pit.stats.command;

import java.util.Collections;
import java.util.List;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;

public abstract class Command implements ICommand {

  @Override
  public String getCommandUsage(final ICommandSender sender) {
    return null;
  }

  @Override
  public List<String> getCommandAliases() {
    return Collections.emptyList();
  }

  @Override
  public boolean canCommandSenderUseCommand(final ICommandSender sender) {
    return true;
  }

  @Override
  public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
    return null;
  }

  @Override
  public boolean isUsernameIndex(final String[] args, final int index) {
    return false;
  }

  @Override
  public int compareTo(final ICommand o) {
    return this.getCommandName().compareTo(o.getCommandName());
  }

  public void register() {
    ClientCommandHandler.instance.registerCommand(this);
  }
}
