package com.salhack.summit.command.util;

public interface ModuleCommandListener {
    void onHide();

    void onToggle();

    void onRename(String newName);
}
