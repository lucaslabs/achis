package com.lucaslabs.achis;

import com.google.common.collect.Lists;
import com.lucaslabs.achis.model.Item;
import com.lucaslabs.achis.model.SocialNetwork;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.FuncN;
import rx.subscriptions.CompositeSubscription;

/**
 * Main class of the library.
 * Provides a Fluent API to search Social Networks by hashtag
 * and returns an Observable list of items.
 *
 * @author lucas.nobile
 */
public class Achis {

    private List<SocialNetwork> socialNetworks;
    private String hashtag;

    // Polling strategy
    private CompositeSubscription subscription;
    private static final int INITIAL_DELAY = 0;
    private static final int POLLING_INTERVAL = 1; // 1 minute


    private Achis(List<SocialNetwork> socialNetworks, String hashtag) {
        this.socialNetworks = socialNetworks;
        this.hashtag = hashtag;
        //   TODO-LMN Create subscription = new CompositeSubscription();
    }

    public void unsubscribe() {
        subscription.unsubscribe();
    }

    public static class Builder {
        private List<SocialNetwork> networks;
        private String hashtag;

        public Builder socialNetworks(List<SocialNetwork> networks) {
            this.networks = networks;
            return this;
        }

        public Builder hastag(String hashtag) {
            this.hashtag = hashtag;
            return this;
        }

        public Achis build() {
            return new Achis(networks, hashtag);
        }
    }


    public Observable<Item> searchByHashtag() {
        // TODO-LMN Add polling strategy
        return searchSocialNetworksByHashtag();
    }

    //    private Observable<Item> pollingObservable() {
    //        return Observable.create(new Observable.OnSubscribe<Item>() {
    //            @Override
    //            public void call(final Subscriber<? super Item> subscriber) {
    //                Schedulers.newThread().createWorker()
    //                        .schedulePeriodically(new Action0() {
    //                            @Override
    //                            public void call() {
    //                                subscriber.onNext(searchSocialNetworksByHashtag());
    //                            }
    //                        }, INITIAL_DELAY, POLLING_INTERVAL, TimeUnit.MINUTES);
    //            }
    //        });
    //    }

    private Observable<Item> searchSocialNetworksByHashtag() {
        Observable<Item> observableSocialNetworks =
                Observable.from(socialNetworks)
                          .flatMap(new Func1<SocialNetwork, Observable<Item>>() {
                              @Override
                              public Observable<Item> call(SocialNetwork socialNetwork) {
                                  return socialNetwork.getService().searchByHashtag(hashtag);
                              }
                          });

        List<Observable<Item>> observableList = Lists.newArrayList();
        observableList.add(observableSocialNetworks);

        return Observable.zip(observableList, new FuncN<Item>() {

            @Override
            public Item call(Object... args) {
                if (args.length == 1) {
                    return (Item) args[0];
                }
                return Item.empty();
            }
        });
    }
}
