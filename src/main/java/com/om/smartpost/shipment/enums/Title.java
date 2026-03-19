package com.om.smartpost.shipment.enums;

import lombok.Getter;

@Getter
public enum Title {
    MR("Mr."),
    MRS("Mrs."),
    MS("Ms.");

    private final String displayName;

    Title(String displayName) {
        this.displayName = displayName;
    }

}


