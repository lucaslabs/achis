package com.lucaslabs.achis.model;

import rx.Observable;

/**
 * Interface to define what is needed from a Social Network service to search by hashtag.
 *
 * @author lucas.nobile
 */
public interface Service<T> {

    Observable<Item> searchByHashtag(String hashtag);

    Observable<Item> transformServiceResponseToItems(Observable<T> responseObservable);
}
