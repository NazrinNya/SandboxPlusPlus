package plugin;

import mindustry.gen.Player;

import static plugin.Main.plots;

public class Menus {
    public static void callMenu(Plot plot, Player plr) {
        String[][] buttons = {{"Immortality: "+ plot.settings.isImmortal}, {"Close"}};
        Menu settingsMenu = new Menu("Settings", "", buttons, ((player, option) -> {
            switch (option) {
                case -1 -> {
                    return;
                }
                case 0 -> {
                    PlotSettings settings = new PlotSettings(!plot.settings.isImmortal);
                    Plot newPlot = plot.updatePlotSettings(settings);
                    plots.replace(plot, newPlot);
                }
                case 1 -> {
                    return;
                }
            }
        }));
        settingsMenu.show(plr);
    }
}
