package com.stellarcolonizer.model.diplomacy;

import com.stellarcolonizer.model.faction.Faction;

public class DiplomaticRelationship {
    private Faction sourceFaction;
    private Faction targetFaction;
    private RelationshipStatus status;
    private int relationshipValue; // 关系值，范围-100到100
    private String relationshipDetails; // 关系详情

    public enum RelationshipStatus {
        HOSTILE("交恶", -1),
        NEUTRAL("中立", 0),
        PEACEFUL("和平", 1);

        private final String displayName;
        private final int value;

        RelationshipStatus(String displayName, int value) {
            this.displayName = displayName;
            this.value = value;
        }

        public String getDisplayName() { return displayName; }
        public int getValue() { return value; }
    }

    public DiplomaticRelationship(Faction source, Faction target, RelationshipStatus initialStatus) {
        this.sourceFaction = source;
        this.targetFaction = target;
        this.status = initialStatus;
        this.relationshipValue = initialStatus == RelationshipStatus.NEUTRAL ? 0 : 
                                initialStatus == RelationshipStatus.PEACEFUL ? 50 : -50;
    }

    public Faction getSourceFaction() { return sourceFaction; }
    public Faction getTargetFaction() { return targetFaction; }
    public RelationshipStatus getStatus() { return status; }
    public int getRelationshipValue() { return relationshipValue; }
    public String getRelationshipDetails() { return relationshipDetails; }

    public void setStatus(RelationshipStatus status) { this.status = status; }
    public void setRelationshipValue(int value) { 
        this.relationshipValue = Math.max(-100, Math.min(100, value)); // 限制在-100到100之间
        updateStatusFromValue();
    }

    public void adjustRelationship(int delta) {
        setRelationshipValue(this.relationshipValue + delta);
    }

    private void updateStatusFromValue() {
        if (relationshipValue >= 25) {
            status = RelationshipStatus.PEACEFUL;
        } else if (relationshipValue >= -25) {
            status = RelationshipStatus.NEUTRAL;
        } else {
            status = RelationshipStatus.HOSTILE;
        }
    }

    public void setDetails(String details) { this.relationshipDetails = details; }
}