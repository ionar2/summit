package com.salhack.summit.command.impl;

import java.util.List;

import com.salhack.summit.command.Command;
import com.salhack.summit.command.util.ModuleCommandListener;
import com.salhack.summit.module.Value;

public class ModuleCommand extends Command {
    private final ModuleCommandListener listener;
    private final List<Value<?>> values;

    public ModuleCommand(String name, String description, ModuleCommandListener listener, final List<Value<?>> values) {
        super(name, description);
        this.listener = listener;
        this.values = values;

        commandChunks.add("hide");
        commandChunks.add("toggle");
        commandChunks.add("rename <newname>");

        /// TODO: Add enum names, etc
        for (Value<?> value : this.values)
            commandChunks.add(String.format("%s <%s>", value.getName(), "value"));
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            /// Print values
            for (Value<?> value : values) {
                SendToChat(String.format("%s : %s", value.getName(), value.getValue()));
            }
            return;
        }

        if (args[1].equalsIgnoreCase("hide")) {
            listener.onHide();
            return;
        }

        if (args[1].equalsIgnoreCase("toggle")) {
            listener.onHide();
            return;
        }

        if (args[1].equalsIgnoreCase("rename")) {
            if (args.length <= 3)
                listener.onRename(args[2]);

            return;
        }

        for (Value value : values) {
            if (value.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                if (args.length <= 2)
                    break;

                String unknownValue = args[2].toLowerCase();

                if (value.getValue() instanceof Number && !(value.getValue() instanceof Enum)) {
                    if (value.getValue() instanceof Integer)
                        value.SetForcedValue(Integer.parseInt(unknownValue));
                    else if (value.getValue() instanceof Float)
                        value.SetForcedValue(Float.parseFloat(unknownValue));
                    else if (value.getValue() instanceof Double)
                        value.SetForcedValue(Double.parseDouble(unknownValue));
                } else if (value.getValue() instanceof Boolean) {
                    value.SetForcedValue(unknownValue.equalsIgnoreCase("true"));
                } else if (value.getValue() instanceof String)
                    value.setStringValue(unknownValue);

                SendToChat(String.format("Set the value of %s to %s", value.getName(), value.getValue()));

                break;
            }
        }
    }

    @Override
    public String getHelp() {
        return getDescription();
    }
}
