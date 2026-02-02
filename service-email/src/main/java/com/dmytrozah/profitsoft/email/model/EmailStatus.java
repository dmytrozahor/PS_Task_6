package com.dmytrozah.profitsoft.email.model;

public enum EmailStatus {

    /**
     * Retrieved from the MB, but has not been yet sent.
     */

    PENDING,

    /**
     * Has been sent.
     */

    SENT,

    /**
     * Error received
     */

    FAILED

}
