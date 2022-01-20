package com.jay.wj_remember.ui.main

import com.jay.common.makeLog
import com.jay.domain.usecase.GithubUseCase
import com.jay.wj_remember.mapper.Mapper
import com.jay.wj_remember.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val githubUseCase: GithubUseCase
) : BaseViewModel() {

    private val _searchClick = PublishSubject.create<Unit>()
    private val _querySubject = BehaviorSubject.createDefault("")

    init {
        val button = _searchClick.throttleFirst(1, TimeUnit.SECONDS)
            .map { _querySubject.value }
            .toFlowable(BackpressureStrategy.DROP)
        val query = _querySubject.debounce(1500, TimeUnit.MILLISECONDS)
            .toFlowable(BackpressureStrategy.DROP)

        compositeDisposable.addAll(
            Flowable.merge(button, query)
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { showLoading() }
                .switchMap(githubUseCase::searchUser)
                .onErrorReturn { listOf() }
                .observeOn(Schedulers.computation())
                .map { it.map(Mapper::mapToPresentation) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { hideLoading() }
                .subscribe { makeLog(javaClass.simpleName, "ok: $it") }
        )
    }

    fun debounceQuery(query: String) = _querySubject.onNext(query)

    fun searchClick() = _searchClick.onNext(Unit)

}