package ninja.trek.rpg.client.state;

import ninja.trek.rpg.network.payloads.SyncJournalPayload;
import ninja.trek.rpg.network.payloads.SyncQuestStatePayload;

import java.util.*;

public class ClientRpgState {

    private static final ClientRpgState INSTANCE = new ClientRpgState();

    private String partyLeader = "";
    private List<String> partyMembers = new ArrayList<>();
    private final Map<String, String> questStates = new HashMap<>();
    private List<SyncJournalPayload.JournalEntry> journalEntries = new ArrayList<>();

    private ClientRpgState() {}

    public static ClientRpgState getInstance() {
        return INSTANCE;
    }

    // Party
    public void updateParty(String leader, List<String> members) {
        this.partyLeader = leader;
        this.partyMembers = new ArrayList<>(members);
    }

    public String getPartyLeader() {
        return partyLeader;
    }

    public List<String> getPartyMembers() {
        return Collections.unmodifiableList(partyMembers);
    }

    // Quest states
    public void updateQuestStates(List<SyncQuestStatePayload.QuestStateEntry> entries) {
        questStates.clear();
        for (SyncQuestStatePayload.QuestStateEntry entry : entries) {
            questStates.put(entry.questId(), entry.state());
        }
    }

    public String getQuestState(String fullId) {
        return questStates.getOrDefault(fullId, "BLOCKED");
    }

    public Map<String, String> getAllQuestStates() {
        return Collections.unmodifiableMap(questStates);
    }

    // Journal
    public void updateJournal(List<SyncJournalPayload.JournalEntry> entries) {
        this.journalEntries = new ArrayList<>(entries);
    }

    public List<SyncJournalPayload.JournalEntry> getJournalEntries() {
        return Collections.unmodifiableList(journalEntries);
    }
}
