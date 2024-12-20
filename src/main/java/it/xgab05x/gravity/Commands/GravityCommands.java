package it.xgab05x.gravity.Commands;

import it.xgab05x.gravity.Events.EventsListener;
import it.xgab05x.gravity.Gravity;
import it.xgab05x.gravity.utils.FileManager;
import it.xgab05x.gravity.utils.HeadUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GravityCommands implements CommandExecutor {
    String prefix = Gravity.getInstance().getPrefix();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("gravity.info")) {
                sender.sendMessage("§bGravity §7by xgab05x (@Poliformica)");
            } else {
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "exclude":
                return handleExclude(sender, args);
            case "include":
                return handleInclude(sender, args);
            case "excludedlist":
                return handleExcludedList(sender);
            case "reload":
                return handleReload(sender);
            case "help":
                return handleHelp(sender);
            case "wearspacehelmet":
                return handleWearSpaceHelmet(sender, args);
            default:
                return false;
        }
    }

    private boolean handleExclude(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gravity.exclude")) {
            sender.sendMessage(prefix + " §cNon hai il permesso per eseguire questo comando.");
            return true;
        }

        if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                if (!Gravity.getInstance().getExcludedPlayers().contains(target.getName())) {
                    Gravity.getInstance().getExcludedPlayers().add(target.getName());
                    FileManager.saveExcludedPlayers();
                    EventsListener.removeGravity(target); // Rimuove immediatamente la gravità
                    sender.sendMessage(prefix + " §7" + target.getName() + " è stato escluso dalla gravità.");
                } else {
                    sender.sendMessage(prefix + " §7" + target.getName() + " è già escluso dalla gravità.");
                }
            } else {
                sender.sendMessage(prefix + " §cGiocatore non trovato.");
            }
        } else {
            sender.sendMessage(prefix + " §cUso corretto: /gravity exclude <giocatore>");
        }
        return true;
    }

    private boolean handleInclude(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gravity.include")) {
            sender.sendMessage(prefix + " §cNon hai il permesso per eseguire questo comando.");
            return true;
        }

        if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                if (Gravity.getInstance().getExcludedPlayers().contains(target.getName())) {
                    Gravity.getInstance().getExcludedPlayers().remove(target.getName());
                    FileManager.saveExcludedPlayers();
                    EventsListener.applyGravity(target); // Applica immediatamente la gravità
                    sender.sendMessage(prefix + " §7" + target.getName() + " è stato incluso di nuovo nella gravità.");
                } else {
                    sender.sendMessage(prefix + " §7" + target.getName() + " non è escluso dalla gravità.");
                }
            } else {
                sender.sendMessage(prefix + " §cGiocatore non trovato.");
            }
        } else {
            sender.sendMessage(prefix + " §cUso corretto: /gravity include <giocatore>");
        }
        return true;
    }

    private boolean handleExcludedList(CommandSender sender) {
        if (!sender.hasPermission("gravity.excludedlist")) {
            sender.sendMessage(prefix + " §cNon hai il permesso per eseguire questo comando.");
            return true;
        }
        if (Gravity.getInstance().getExcludedPlayers().isEmpty()) {
            sender.sendMessage(prefix + " §7Non ci sono giocatori esclusi dalla gravità.");
        } else {
            sender.sendMessage(prefix + " §7Giocatori esclusi dalla gravità:");
            for (String playerName : Gravity.getInstance().getExcludedPlayers()) {
                sender.sendMessage("§7- " + playerName);
            }
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("gravity.*")) {
            sender.sendMessage(prefix + " §cNon hai il permesso per eseguire questo comando.");
            return true;
        }
        Gravity.getInstance().reloadConfig();
        FileManager.reloadExcludedPlayers();
        sender.sendMessage(prefix + " §7Plugin ricaricato con successo.");
        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        if (!sender.hasPermission("gravity.help")) {
            sender.sendMessage(prefix + " §cNon hai il permesso per eseguire questo comando.");
            return true;
        }
        sender.sendMessage("§7Comandi disponibili:");
        sender.sendMessage("§7/gravity info - Mostra informazioni sul plugin.");
        sender.sendMessage("§7/gravity exclude <giocatore> - Esclude un giocatore dalla gravità.");
        sender.sendMessage("§7/gravity include <giocatore> - Includi un giocatore nella gravità.");
        sender.sendMessage("§7/gravity excludedlist - Mostra la lista di giocatori esclusi.");
        sender.sendMessage("§7/gravity reload - Ricarica il plugin.");
        sender.sendMessage("§7/gravity help - Mostra questa lista di comandi.");
        sender.sendMessage("§7/gravity wearspacehelmet [giocatore] - Indossa un casco spaziale.");
        return true;
    }

    private boolean handleWearSpaceHelmet(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("gravity.helmetwear.self")) {
                    player.sendMessage(prefix + " §cNon hai il permesso per eseguire questo comando.");
                    return true;
                }
                if (player.getInventory().getHelmet() != null) {
                    player.sendMessage(prefix + " §cHai già un altro casco. Questo verrà sostituito.");
                    ItemStack previousHelmet = player.getInventory().getHelmet();
                    if (!player.getInventory().addItem(previousHelmet).isEmpty()) {
                        player.getWorld().dropItem(player.getLocation(), previousHelmet);
                    }
                }
                String helmetName = Gravity.getInstance().getConfig().getString("spacehelmet.name", "");
                String helmetTexture = Gravity.getInstance().getConfig().getString("spacehelmet.texture", "");
                ItemStack spaceHelmet = HeadUtils.getCustomHead(helmetTexture);
                ItemMeta meta = spaceHelmet.getItemMeta();
                assert meta != null;
                meta.setDisplayName(helmetName);
                spaceHelmet.setItemMeta(meta);
                player.getInventory().setHelmet(spaceHelmet);
                player.sendMessage(prefix + " §7Hai indossato il casco spaziale.");
            }
        } else if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                if (!sender.hasPermission("gravity.helmetwear.others")) {
                    sender.sendMessage(prefix + " §cNon hai il permesso per eseguire questo comando.");
                    return true;
                }
                if (target.getInventory().getHelmet() != null) {
                    sender.sendMessage(prefix + " §7" + target.getName() + " ha già un altro casco. Questo verrà sostituito.");
                    ItemStack previousHelmet = target.getInventory().getHelmet();
                    if (!target.getInventory().addItem(previousHelmet).isEmpty()) {
                        target.getWorld().dropItem(target.getLocation(), previousHelmet);
                    }
                }
                String helmetName = Gravity.getInstance().getConfig().getString("spacehelmet.name", "");
                String helmetTexture = Gravity.getInstance().getConfig().getString("spacehelmet.texture", "");
                ItemStack spaceHelmet = HeadUtils.getCustomHead(helmetTexture);
                ItemMeta meta = spaceHelmet.getItemMeta();
                assert meta != null;
                meta.setDisplayName(helmetName);
                spaceHelmet.setItemMeta(meta);
                target.getInventory().setHelmet(spaceHelmet);
                sender.sendMessage(prefix + " §7Hai dato il casco spaziale a " + target.getName() + ".");
                target.sendMessage(prefix + " §7Hai indossato un casco spaziale.");
            } else {
                sender.sendMessage(prefix + " §cGiocatore non trovato.");
            }
        } else {
            sender.sendMessage(prefix + " §cUso corretto: /gravity wearspacehelmet <giocatore>");
        }
        return true;
    }
}
