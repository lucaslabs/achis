package com.lucaslabs.achis;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.lucaslabs.achis.model.Item;
import com.lucaslabs.achis.model.SocialNetwork;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

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
    private Scheduler observeOnScheduler;
    private Subscriber<Item> subscriber;

    // Polling strategy
    private static final int INITIAL_DELAY = 0;
    private static final int POLLING_INTERVAL = 1; // 1 minute
    private int initialDelay;
    private int pollingInterval;


    private Achis(List<SocialNetwork> socialNetworks,
            String hashtag,
            Scheduler observeOnScheduler,
            Subscriber<Item> subscriber,
            int initialDelay,
            int pollingInterval) {
        this.socialNetworks = socialNetworks;
        this.hashtag = hashtag;
        this.observeOnScheduler = observeOnScheduler;
        this.subscriber = subscriber;
        this.initialDelay = initialDelay;
        this.pollingInterval = pollingInterval;
    }

    public static class Builder {
        private List<SocialNetwork> networks;
        private String hashtag;
        private Scheduler observeOnScheduler;
        private Subscriber<Item> subscriber;
        // Polling strategy
        private int initialDelay;
        private int pollingInterval;

        public Builder socialNetworks(List<SocialNetwork> networks) {
            this.networks = networks;
            return this;
        }

        public Builder hastag(String hashtag) {
            this.hashtag = hashtag;
            return this;
        }

        public Builder observeOnScheduler(Scheduler observeOnScheduler) {
            this.observeOnScheduler = observeOnScheduler;
            return this;
        }

        public Builder subscriber(Subscriber<Item> subscriber) {
            this.subscriber = subscriber;
            return this;
        }

        public Builder withPolling() {
            this.initialDelay = INITIAL_DELAY;
            this.pollingInterval = POLLING_INTERVAL;
            return this;
        }

        public Builder withPolling(int initialDelay, int pollingInterval) {
            Preconditions.checkArgument(initialDelay >= 0, "initialDelay must be 0 or greater.");
            Preconditions.checkArgument(pollingInterval >= 1, "pollingInterval must be 1 minute or greater.");
            this.initialDelay = initialDelay;
            this.pollingInterval = pollingInterval;
            return this;
        }

        public Achis build() {
            return new Achis(networks, hashtag, observeOnScheduler, subscriber, initialDelay, pollingInterval);
        }
    }

    public void searchByHashtag() {
        Scheduler.Worker worker = Schedulers.io().createWorker();
        worker.schedulePeriodically(new Action0() {
            @Override
            public void call() {
                searchByHashtagObservable()
                        .subscribeOn(Schedulers.io()) // performs networking on background thread
                        .observeOn(observeOnScheduler) // sends notifications to another Scheduler, usually the UI thread
                        .subscribe(subscriber);
            }
        }, initialDelay, pollingInterval, TimeUnit.MINUTES);
    }


    public void manualRecursionPollingStrategy() {
        Observable.create(new Observable.OnSubscribe<Item>() {
            @Override
            public void call(final Subscriber<? super Item> innerSubscriber) {
                Schedulers.io().createWorker()
                          .schedulePeriodically(new Action0() {
                              @Override
                              public void call() {
                                  searchByHashtagObservable()
                                          .doOnNext(new Action1<Item>() {
                                              @Override
                                              public void call(Item item) {
                                                  innerSubscriber.onNext(item);
                                              }
                                          })
                                          .doOnError(new Action1<Throwable>() {
                                              @Override
                                              public void call(Throwable throwable) {
                                                  if (throwable != null) {
                                                      innerSubscriber.onError(throwable);
                                                  }
                                              }
                                          })
                                          .doOnCompleted(new Action0() {
                                              @Override
                                              public void call() {
                                                  innerSubscriber.onCompleted();
                                              }
                                          }).subscribe();
                              }
                          }, INITIAL_DELAY, POLLING_INTERVAL, TimeUnit.MILLISECONDS);
            }
        })
                .subscribeOn(Schedulers.io()) // performs networking on background thread
                .observeOn(observeOnScheduler) // sends notifications to another Scheduler, usually the UI thread
                .subscribe(subscriber);
    }

    private Observable<Item> searchByHashtagObservable() {
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
