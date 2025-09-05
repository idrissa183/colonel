package com.longrich.smartgestion.enums;

public enum NiveauAlerte {
    INFO("Information", "🔵"),
    ATTENTION("Attention", "🟡"),
    CRITIQUE("Critique", "🔴");

    private final String displayName;
    private final String icone;

    NiveauAlerte(String displayName, String icone) {
        this.displayName = displayName;
        this.icone = icone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcone() {
        return icone;
    }
}