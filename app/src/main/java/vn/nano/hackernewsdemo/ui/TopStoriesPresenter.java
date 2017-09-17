package vn.nano.hackernewsdemo.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import vn.nano.core_library.mvp.BaseTiPresenter;
import vn.nano.hackernewsdemo.dagger.component.ComponentManager;
import vn.nano.hackernewsdemo.data.model.Story;
import vn.nano.hackernewsdemo.data.remote.HackerNewsService;

/**
 * Created by alex on 9/14/17.
 */

public class TopStoriesPresenter extends BaseTiPresenter<TopStoriesView> {

    @Inject
    HackerNewsService hackerNewsService;

    private List<Integer> storyIds;
    private Map<Integer, Story> mapDownloadedStory;
    private Map<Integer, Boolean> mapDownloadingStory;

    public TopStoriesPresenter() {
        ComponentManager.getInstance().getAppComponent().inject(this);
        mapDownloadedStory = new HashMap<>();
        mapDownloadingStory = new HashMap<>();
    }

    public List<Integer> getStoryIds() {
        return storyIds;
    }

    public void setStoryIds(List<Integer> storyIds) {
        this.storyIds = storyIds;
    }

    public void getTopStoryIds() {
        // with disposable manager
        manageDisposable(
                hackerNewsService.getTopStoryIds()
                        .doOnSubscribe(disposable -> {
                            sendToView(view -> view.showLoading(true));
                        })
                        .doAfterTerminate(() -> sendToView(view -> view.hideLoading()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<List<Integer>>() {
                            @Override
                            public void onNext(@NonNull List<Integer> storyIds) {
                                System.out.println("getTopStoryIds()=>thread=" + Thread.currentThread() + "; hashCode=" + Thread.currentThread().hashCode());
                                sendToView(view -> {
                                    setStoryIds(storyIds);
                                    view.displayStoryIds(storyIds);
                                });
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                throwable.printStackTrace();
                                sendToView(view -> {
                                    view.showError(throwable, HackerNewsService.ApiError.class);
                                });
                            }

                            @Override
                            public void onComplete() {
                            }
                        })
        );

        // without disposable manager
//        hackerNewsService.getTopStoryIds()
//                .doOnSubscribe(disposable -> sendToView(view -> view.showLoading(true)))
//                .doAfterTerminate(() -> sendToView(view -> view.hideLoading()))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(storyIds -> {
//                    sendToView(view -> {
//                        setStoryIds(storyIds);
//                        view.displayStoryIds(storyIds);
//                    });
//                }, throwable -> {
//                    throwable.printStackTrace();
//                    sendToView(view -> {
//                        view.showError(throwable, HackerNewsService.ApiError.class);
//                    });
//                });
    }

    public void getTopStory(int storyId) {
        if (mapDownloadingStory.get(storyId) == null || !mapDownloadingStory.get(storyId)) {
            if (mapDownloadedStory.get(storyId) != null) {
                sendToView(view -> view.displayStory(mapDownloadedStory.get(storyId)));
            } else {
                mapDownloadingStory.put(storyId, true);

                // with disposable manager
                manageDisposable(
                        hackerNewsService.getStory(storyId)
                                .doAfterTerminate(() -> mapDownloadingStory.put(storyId, false))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(new DisposableObserver<Story>() {
                                    @Override
                                    public void onNext(@NonNull Story story) {
                                        System.out.println("getTopStory()=>thread=" + Thread.currentThread() + "; hashCode=" + Thread.currentThread().hashCode());
                                        mapDownloadedStory.put(storyId, story);
                                        sendToView(view -> {
                                            view.displayStory(story);
                                        });
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable throwable) {
                                        throwable.printStackTrace();
                                    }

                                    @Override
                                    public void onComplete() {
                                    }
                                })
                );

                // without disposable manager
//                hackerNewsService.getStory(storyId)
//                        .doAfterTerminate(() -> mapDownloadingStory.put(storyId, false))
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(story -> {
//                            mapDownloadedStory.put(storyId, story);
//                            sendToView(view -> view.displayStory(story));
//                        }, throwable -> {
//                            throwable.printStackTrace();
//                        });
            }
        }
    }

}
