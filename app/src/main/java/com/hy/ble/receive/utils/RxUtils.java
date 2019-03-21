package com.hy.ble.receive.utils;

import android.view.View;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;

/**
 * <pre>
 *     author    : Agg
 *     blog      : https://blog.csdn.net/Agg_bin
 *     time      : 2019/03/15
 *     desc      :
 *     reference :
 * </pre>
 */
public class RxUtils {


    /**
     * 倒计时，倒计 time 秒
     *
     * @param time 单位：秒
     * @return Observable
     */
    public static Observable<Integer> countDown(int time) {
        time = time < 0 ? 0 : time;
        final int countTime = time;
        return Subject.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(increaseTime -> countTime - increaseTime.intValue())
                .take(countTime + 1);
    }

    /**
     * 过滤点击时间，在特定的时间内
     * 只取最第一次点击
     * 注意结合RxLife释放引用
     *
     * @param view       view
     * @param durationMs 时间毫秒
     * @return Observable<Object>，Object并无意义
     */
    public static Observable<Object> filterClick(View view, long durationMs) {
        return Observable.create(subscriber -> {
            if (view != null)
                view.setOnClickListener(v -> subscriber.onNext(1));
        }).throttleFirst(durationMs, TimeUnit.MILLISECONDS);
    }

    public static Observable<Object> filterClick(View view) {
        return filterClick(view, 500);
    }

    /**
     * 统计一段时间内，View的点击次数
     *
     * @param view       view
     * @param durationMs 时间为 毫秒
     * @return Observable<Integer> 次数
     */
    public static Observable<Integer> countClickNum(View view, long durationMs) {
        int[] count = {0};
        return Observable.create((ObservableOnSubscribe<Integer>)
                subscriber -> {
                    long[] time = {0, 0};
                    if (view != null)
                        view.setOnClickListener(v -> {
                            time[0] = System.currentTimeMillis();
                            if (time[0] - time[1] <= durationMs) {
                                count[0] = count[0] + 1;
                            } else {
                                count[0] = 1;
                            }
                            subscriber.onNext(count[0]);
                            time[1] = time[0];
                        });
                })
                .throttleLast(durationMs, TimeUnit.MILLISECONDS).map(integer -> {
                    count[0] = 0;
                    return integer;
                });
    }

}
