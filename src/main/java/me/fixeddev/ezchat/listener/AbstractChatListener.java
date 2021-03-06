package me.fixeddev.ezchat.listener;

import me.fixeddev.ezchat.EasyTextComponent;
import me.fixeddev.ezchat.event.AsyncEzChatEvent;
import me.fixeddev.ezchat.format.ChatFormat;
import me.fixeddev.ezchat.format.ChatFormatManager;
import me.fixeddev.ezchat.format.ChatFormatSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public abstract class AbstractChatListener implements Listener {

    private final ChatFormatManager chatFormatManager;
    private final ChatFormatSerializer chatFormatSerializer;

    public AbstractChatListener(ChatFormatManager chatFormatManager) {
        this.chatFormatManager = chatFormatManager;

        this.chatFormatSerializer = new ChatFormatSerializer();
    }

    public void formatChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);

        Bukkit.getConsoleSender().sendMessage(String.format(event.getFormat(), player.getName(), event.getMessage()));

        ChatFormat chatFormat = chatFormatManager.getChatFormatForPlayer(player).copy();

        String message = ChatFormatSerializer.color(chatFormat.getChatColor()) + event.getMessage();

        if (player.hasPermission("ezchat.color")) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        BaseComponent[] messageComponent = TextComponent.fromLegacyText(message);

        AsyncEzChatEvent chatEvent = new AsyncEzChatEvent(event, chatFormat);

        Bukkit.getPluginManager().callEvent(chatEvent);

        if (chatEvent.isCancelled()) {
            return;
        }

        EasyTextComponent chatFormatComponent = null;

        if (!chatFormat.isUsePlaceholderApi()) {
            chatFormatComponent = chatFormatSerializer.constructJsonMessage(chatFormat, player);
            chatFormatComponent.append(messageComponent);
        }

        for (Player recipient : event.getRecipients()) {
            if (chatFormat.isUsePlaceholderApi()) {
                chatFormatComponent = chatFormatSerializer.constructJsonMessage(chatFormat, player, recipient);

                chatFormatComponent.append(messageComponent);
            }


            recipient.spigot().sendMessage(chatFormatComponent.build());
        }
    }
}
