package com.lucaslabs.achis.model;

/**
 * Abstract class to define what is needed from a Social Network to search by hashtag.
 *
 * @author lucas.nobile
 */
public abstract class SocialNetwork {

    protected String name;
    protected Service service;

    public SocialNetwork(String name) {
        this.name = name;
        service = provideService();
    }

    protected abstract Service provideService();

    public String getName() {
        return name;
    }

    public Service getService() {
        return service;
    }
}
