package ninja.trek.rpg.state;

import net.minecraft.nbt.CompoundTag;

public record CharacterExtra(String text, String source) {

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("text", text);
        tag.putString("source", source);
        return tag;
    }

    public static CharacterExtra fromNbt(CompoundTag tag) {
        return new CharacterExtra(
            tag.getStringOr("text", ""),
            tag.getStringOr("source", "")
        );
    }
}
