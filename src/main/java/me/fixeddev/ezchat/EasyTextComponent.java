package me.fixeddev.ezchat;

import me.fixeddev.ezchat.util.ReflectionUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.logging.Level;

public class EasyTextComponent {

    private BaseComponent builder;

    public EasyTextComponent() {
        builder = new TextComponent("");
    }

    private EasyTextComponent(@NotNull BaseComponent component) {
        this.builder = component;
    }

    @NotNull
    public EasyTextComponent appendWithNewLine(@NotNull String content) {
        return appendWithNewLine(appendAll(TextComponent.fromLegacyText(content)));
    }

    @NotNull
    public EasyTextComponent appendWithNewLine(@NotNull BaseComponent component) {
        return append(component).addNewLine();
    }

    @NotNull
    public EasyTextComponent append(@NotNull String content) {
        return append(appendAll(TextComponent.fromLegacyText(content)));
    }

    @NotNull
    public EasyTextComponent append(@NotNull BaseComponent component) {
        builder.addExtra(component);

        return this;
    }

    @NotNull
    public EasyTextComponent append(@NotNull EasyTextComponent easyComponent) {
        builder.addExtra(easyComponent.builder);

        return this;
    }


    @NotNull
    public EasyTextComponent addNewLine() {
        builder.addExtra("\n");

        return this;
    }

    @NotNull
    public EasyTextComponent setHoverShowText(@NotNull String content) {
        return setHoverShowText(TextComponent.fromLegacyText(content));
    }

    @NotNull
    public EasyTextComponent setHoverShowText(@NotNull EasyTextComponent component) {
        return setHoverShowText(new BaseComponent[]{component.builder});
    }

    @NotNull
    public EasyTextComponent setHoverShowText(@NotNull BaseComponent[] component) {
        return setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component));
    }

    @NotNull
    public EasyTextComponent setHoverShowItem(@NotNull ItemStack item) {
        return setHoverShowItem(appendAll(ComponentSerializer.parse(convertItemStackToJson(item))));
    }

    @NotNull
    public EasyTextComponent setHoverShowItem(@NotNull EasyTextComponent component) {
        return setHoverShowItem(component.builder);
    }

    @NotNull
    public EasyTextComponent setHoverShowItem(@NotNull BaseComponent component) {
        return setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{component}));
    }

    @NotNull
    public EasyTextComponent setHoverEvent(@NotNull HoverEvent event) {
        builder.setHoverEvent(event);

        return this;
    }

    @NotNull
    public EasyTextComponent setClickRunCommand(@NotNull String command) {
        return setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    @NotNull
    public EasyTextComponent setClickSuggestCommand(@NotNull String command) {
        return setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
    }

    @NotNull
    public EasyTextComponent setClickOpenUrl(@NotNull String url) {
        return setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
    }

    @NotNull
    public EasyTextComponent setClickEvent(@NotNull ClickEvent event) {
         builder.setClickEvent(event);

        return this;
    }


    public BaseComponent build() {
        return builder;
    }

    /**
     * Created by sainttx
     * Taken from here: https://www.spigotmc.org/threads/tut-item-tooltips-with-the-chatcomponent-api.65964/
     *
     * @param itemStack
     * @return
     */
    private String convertItemStackToJson(ItemStack itemStack) {
        // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
        Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        Object nmsNbtTagCompoundObj; // This will just be an empty NBTTagCompound instance to invoke the saveNms method
        Object nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
        Object itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method

        try {
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "failed to serialize itemstack to nms item", t);
            return null;
        }

        // Return a string representation of the serialized object
        return itemAsJsonObject.toString();
    }

    @NotNull
    public static BaseComponent appendAll(BaseComponent[] components) {
        if(components.length == 0){
            throw new IllegalArgumentException("Appending 0 components is not allowed!");
        }

        BaseComponent parent = null;

        for (BaseComponent component : components) {
            if (parent == null) {
                parent = component;
                continue;
            }

            parent.addExtra(component);
        }

        return parent;
    }
}