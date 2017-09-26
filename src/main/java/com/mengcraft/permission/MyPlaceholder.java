package com.mengcraft.permission;

import lombok.val;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;

import static com.mengcraft.permission.$.nil;

/**
 * Created on 17-6-28.
 */
public class MyPlaceholder extends EZPlaceholderHook {

    enum _$ {

        EXPIRE((p, input) -> {
            val attach = Fetcher.INSTANCE.getFetched(p.getName());
            if (nil(attach)) return "-1";
            val look = attach.look(input.next(), false);
            if (nil(look)) return "-1";
            return "" + (look.getValue().getOutdated() - $.now());
        }),

        EXPIRETIME((p, input) -> {
            val time = EXPIRE.func.apply(p, input);
            if (time.equals("-1")) return "-1";
            return TimeHelper.encode(Long.parseLong(time));
        });

        private final BiFunction<Player, Iterator<String>, String> func;

        _$(BiFunction<Player, Iterator<String>, String> func) {
            this.func = func;
        }
    }

    MyPlaceholder(Plugin plugin) {
        super(plugin, "permission");
    }

    @Override
    public String onPlaceholderRequest(Player p, String input) {
        val i = Arrays.asList(input.split("_")).iterator();
        try {
            return _$.valueOf(i.next().toUpperCase()).func.apply(p, i);
        } catch (IllegalArgumentException ignore) {
        } catch (Exception e) {
            Main.log(new IllegalStateException(p + " -> " + input, e));
        }
        return null;
    }

}
