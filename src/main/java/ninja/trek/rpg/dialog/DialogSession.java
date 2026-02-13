package ninja.trek.rpg.dialog;

import ninja.trek.rpg.data.DialogResponse;
import ninja.trek.rpg.data.DialogTreeData;

import java.util.List;

public class DialogSession {

    private final DialogTreeData tree;
    private final String questFullId;
    private String currentNodeId;
    private String selectedBranch;
    private List<DialogResponse> visibleResponses;

    public DialogSession(DialogTreeData tree, String questFullId) {
        this.tree = tree;
        this.questFullId = questFullId;
        this.currentNodeId = tree.startNode();
    }

    public DialogTreeData getTree() {
        return tree;
    }

    public String getQuestFullId() {
        return questFullId;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String nodeId) {
        this.currentNodeId = nodeId;
    }

    public String getSelectedBranch() {
        return selectedBranch;
    }

    public void setSelectedBranch(String branch) {
        this.selectedBranch = branch;
    }

    public List<DialogResponse> getVisibleResponses() {
        return visibleResponses;
    }

    public void setVisibleResponses(List<DialogResponse> responses) {
        this.visibleResponses = responses;
    }
}
