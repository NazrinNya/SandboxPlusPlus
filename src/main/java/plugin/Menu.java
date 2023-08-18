package plugin;

import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.ui.Menus;

public class Menu {
    private final int register;
    private final Menus.MenuListener listener;
    private final String message;
    private final String title;
    private final String[][] options;

    public Menu(String title, String message, String[][] options, Menus.MenuListener listener){
        register = Menus.registerMenu(listener);
        this.listener = listener;
        this.message = message;
        this.options = options;
        this.title = title;
    }
    public void show(Player p){
        Call.menu(p.con, register, title, message, options);
    }
    public void showAll(){
        Call.menu(register, title, message, options);
    }

    public int getRegister() {
        return register;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public String[][] getOptions() {
        return options;
    }

    public Menus.MenuListener getListener() {
        return listener;
    }
}