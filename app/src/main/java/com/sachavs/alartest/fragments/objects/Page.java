package com.sachavs.alartest.fragments.objects;

import java.io.Serializable;
import java.util.List;

public class Page implements Serializable {

    private String status;
    private String page;
    private List<Item> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public List<Item> getData() {
        return data;
    }

    public void setData(List<Item> data) {
        this.data = data;
    }
}
