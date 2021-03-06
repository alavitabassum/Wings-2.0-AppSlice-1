package com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerLandingPageTabLayout;

public class DeliverySummary_Model {
    private String username;
    private String unpicked;
    private String withoutStatus;
    private String onHold;
    private String cash;
    private String cashrecv;
    private String returnrecv;

    public String getReAttempt() {
        return reAttempt;
    }

    public void setReAttempt(String reAttempt) {
        this.reAttempt = reAttempt;
    }

    private String returnRequest;
    private String reAttempt;

    public DeliverySummary_Model(String username, String unpicked, String withoutStatus, String onHold, String cash, String returnRequest, String returnList, String reAttempt, String returnrecv, String cashrecv,int status) {
        this.username = username;
        this.unpicked = unpicked;
        this.withoutStatus = withoutStatus;
        this.onHold = onHold;
        this.cash = cash;
        this.returnRequest = returnRequest;
        this.returnList = returnList;
        this.reAttempt = reAttempt;
        this.status = status;
        this.returnrecv = returnrecv;
        this.cashrecv = cashrecv;
    }

    public String getReturnID() {
        return returnID;
    }

    public void setReturnID(String returnID) {
        this.returnID = returnID;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    private String returnList;
    private String returnID;
    private String reason;
    int key_id;
    int status;

    public DeliverySummary_Model(String username, String unpicked, String withoutStatus, String onHold, String cash, String returnRequest, String returnList, int status) {
        this.username = username;
        this.unpicked = unpicked;
        this.withoutStatus = withoutStatus;
        this.onHold = onHold;
        this.cash = cash;
        this.returnRequest = returnRequest;
        this.returnList = returnList;
        this.status = status;

    }

    public DeliverySummary_Model(String username, String unpicked, String withoutStatus, String onHold, String cash, String returnRequest, String returnList) {
        this.username = username;
        this.unpicked = unpicked;
        this.withoutStatus = withoutStatus;
        this.onHold = onHold;
        this.cash = cash;
        this.returnRequest = returnRequest;
        this.returnList = returnList;
    }

    public DeliverySummary_Model(String returnID, String reason) {
        this.returnID = returnID;
        this.reason = reason;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUnpicked() {
        return unpicked;
    }

    public void setUnpicked(String unpicked) {
        this.unpicked = unpicked;
    }

    public String getWithoutStatus() {
        return withoutStatus;
    }

    public void setWithoutStatus(String withoutStatus) {
        this.withoutStatus = withoutStatus;
    }

    public String getOnHold() {
        return onHold;
    }

    public void setOnHold(String onHold) {
        this.onHold = onHold;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }

    public String getReturnRequest() {
        return returnRequest;
    }

    public void setReturnRequest(String returnRequest) {
        this.returnRequest = returnRequest;
    }

    public String getReturnList() {
        return returnList;
    }

    public void setReturnList(String returnList) {
        this.returnList = returnList;
    }

    public int getKey_id() {
        return key_id;
    }

    public void setKey_id(int key_id) {
        this.key_id = key_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

