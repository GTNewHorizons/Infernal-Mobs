package atomicstryker.infernalmobs.common.modifiers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraftforge.common.config.Configuration;

public abstract class ModifierLoader<T extends MobModifier> {

    public static String[] emptyString = new String[] {};
    public static String[] creeperString = new String[] { "net.minecraft.entity.monster.EntityCreeper" };
    public static String[] spiderString = new String[] { "net.minecraft.entity.monster.EntitySpider" };
    private final String modifierClassName;

    public final List<Class<?>> bannedClasses = new ArrayList<>();
    final String[] blacklistedMobs;

    protected ModifierLoader(Class<T> modifierClass, String[] blacklistedMobs) {
        this.modifierClassName = modifierClass.getSimpleName();
        this.blacklistedMobs = blacklistedMobs;
    }

    public abstract T make(@Nullable MobModifier next);

    public void loadConfig(Configuration config) {
        String[] bannedClassString = config.getStringList(
            "Disallowed Mob Classes",
            getModifierClassName(),
            blacklistedMobs,
            "Fully Qualified Mob classes which can not have this effect.");
        try {
            for (int i = 0; i < bannedClassString.length; i++) {
                Class<?> clazz = Class.forName(bannedClassString[i]);
                bannedClasses.add(clazz);
            }
        } catch (Exception e) {

        }

    }

    public String getModifierClassName() {
        return modifierClassName;
    }

    public Class<?>[] getBannedClassesToArray() {
        return bannedClasses.toArray(new Class<?>[0]);
    }
}
