package com.hy.greenbuilding.presenter;

import android.util.Log;

import com.hy.greenbuilding.ui.activity.BaseActivity;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import io.reactivex.disposables.Disposable;


public abstract class BasePresenter<T extends BaseActivity> {

    protected WeakReference<T> mWrfActivity;

//    protected T mActivity;

    private WeakHashMap<String, Disposable> mDisposableHashMap = new WeakHashMap<>();

    public BasePresenter(T t) {
//        this.mActivity = t;
        mWrfActivity = new WeakReference<T>(t);
      //  t.setPresenter(this);
    }


    public void detachView() {
        removeDisposeAll();
    }

    public void showLoadingDialog() {
        T activity = getActivity();
        if (activity == null) {
            return;
        }
      //  activity.showLoadingDialog();
    }
    public void addDisposable(String hash, Disposable disposable) {

        Disposable oldDisposable = mDisposableHashMap.remove(hash);
        if (oldDisposable != null && (!oldDisposable.isDisposed())) {
            oldDisposable.dispose();
        }

        mDisposableHashMap.put(hash, disposable);
    }

    public void removeDispose(String hash) {
        Disposable disposable = mDisposableHashMap.remove(hash);
        if (disposable != null && (!disposable.isDisposed())) {
            disposable.dispose();
        }

        Log.w("disposable", this.getClass().getSimpleName() + " remove " + hash);
    }

    public Disposable delDispose(String hash) {
        return mDisposableHashMap.remove(hash);
    }

    public void removeDisposeAll() {
        for (Map.Entry<String, Disposable> entry : mDisposableHashMap.entrySet()) {
            Disposable value = entry.getValue();
            if (value != null && (!value.isDisposed())) {
                value.dispose();
            }
        }

        Log.w("disposable", this.getClass().getSimpleName() + " remove all");
    }


    public T getActivity() {
        return mWrfActivity.get();
    }


    public void run(DisposeListener listener) {
        String key = listener.getKey();
        removeDispose(key);
        Disposable disposable = listener.run();
        addDisposable(key, disposable);

    }


    public static interface DisposeListener {
        String getKey();

        Disposable run();
    }
}
