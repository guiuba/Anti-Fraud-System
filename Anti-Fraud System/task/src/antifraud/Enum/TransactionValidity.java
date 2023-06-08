package antifraud.Enum;

//@Component
public enum TransactionValidity {
    ALLOWED(1),
    PROHIBITED(3),
    MANUAL_PROCESSING(2);

  private final int status;

    TransactionValidity(int status) {
        this.status = status;
    }


    public int getStatus() {
        return status;
    }


    public static boolean isValidTransactionValidity(String transactionValidity) {
        for (TransactionValidity value: values()) {
            if (value.toString().equalsIgnoreCase(transactionValidity)) {
                return true;
            }
        }
        return false;
    }

}
