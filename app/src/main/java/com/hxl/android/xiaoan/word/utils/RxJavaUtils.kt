package com.hxl.android.xiaoan.word.utils

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.internal.functions.Functions
import io.reactivex.rxjava3.internal.observers.LambdaObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.function.Consumer

object RxJavaUtils {
     fun <T  : Any> createObservable(function: ObservableEmitter<T>.() -> Unit): Observable<T> {
        return Observable.create<T> {
            try {
                function.invoke(it)
            } catch (e: Exception) {
                it.onError(e)
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T :Any> Observable<T>.baseSubscribe(
    onNext: io.reactivex.rxjava3.functions.Consumer<in T>,
    onError: io.reactivex.rxjava3.functions.Consumer<Throwable>,
):Disposable {
    return this.subscribe(onNext,onError, Functions.EMPTY_ACTION)
}