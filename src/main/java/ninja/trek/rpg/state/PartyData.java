package ninja.trek.rpg.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public class PartyData {

    private UUID leader;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> pendingInvites = new HashSet<>();
    private final Set<UUID> pendingRequests = new HashSet<>();

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
        if (leader != null) {
            members.add(leader);
        }
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public boolean isLeader(UUID player) {
        return leader != null && leader.equals(player);
    }

    public void addMember(UUID player) {
        members.add(player);
    }

    public void removeMember(UUID player) {
        members.remove(player);
        pendingInvites.remove(player);
        if (isLeader(player)) {
            leader = members.isEmpty() ? null : members.iterator().next();
        }
    }

    public Set<UUID> getPendingInvites() {
        return Collections.unmodifiableSet(pendingInvites);
    }

    public void addInvite(UUID player) {
        pendingInvites.add(player);
    }

    public void removeInvite(UUID player) {
        pendingInvites.remove(player);
    }

    public Set<UUID> getPendingRequests() {
        return Collections.unmodifiableSet(pendingRequests);
    }

    public void addRequest(UUID player) {
        pendingRequests.add(player);
    }

    public void removeRequest(UUID player) {
        pendingRequests.remove(player);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        if (leader != null) {
            tag.putString("Leader", leader.toString());
        }
        tag.put("Members", uuidSetToList(members));
        tag.put("PendingInvites", uuidSetToList(pendingInvites));
        tag.put("PendingRequests", uuidSetToList(pendingRequests));
        return tag;
    }

    public static PartyData fromNbt(CompoundTag tag) {
        PartyData data = new PartyData();
        if (tag.contains("Leader")) {
            try {
                data.leader = UUID.fromString(tag.getStringOr("Leader", ""));
            } catch (IllegalArgumentException ignored) {}
        }
        listToUuidSet(tag.getListOrEmpty("Members"), data.members);
        listToUuidSet(tag.getListOrEmpty("PendingInvites"), data.pendingInvites);
        listToUuidSet(tag.getListOrEmpty("PendingRequests"), data.pendingRequests);
        return data;
    }

    private static ListTag uuidSetToList(Set<UUID> set) {
        ListTag list = new ListTag();
        for (UUID uuid : set) {
            list.add(StringTag.valueOf(uuid.toString()));
        }
        return list;
    }

    private static void listToUuidSet(ListTag list, Set<UUID> set) {
        for (int i = 0; i < list.size(); i++) {
            try {
                Tag element = list.get(i);
                if (element instanceof StringTag st) {
                    set.add(UUID.fromString(st.value()));
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
