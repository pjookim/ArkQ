package me.pjookim.arkq;

public class ItemObject {
    private String server;
    private int queue;
    private int id;


    public ItemObject(String server, int queue, int id){
        this.server = server;
        this.queue = queue;
        this.id = id;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getQueue() {
        return queue;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}