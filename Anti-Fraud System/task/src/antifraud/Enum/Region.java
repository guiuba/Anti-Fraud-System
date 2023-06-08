package antifraud.Enum;

public enum Region {
    EAP, ECA, HIC, LAC, MENA, SA, SSA;

    public static boolean isValidRegion(String region) {
        for (Region value: values()) {
            if (value.toString().equalsIgnoreCase(region)) {
                return true;
            }
        }
        return false;
    }
}

