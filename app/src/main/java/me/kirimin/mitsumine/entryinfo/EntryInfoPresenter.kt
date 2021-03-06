package me.kirimin.mitsumine.entryinfo

import android.content.Context
import me.kirimin.mitsumine._common.domain.model.Bookmark
import me.kirimin.mitsumine._common.domain.model.EntryInfo
import me.kirimin.mitsumine._common.domain.model.Star
import me.kirimin.mitsumine._common.network.StarApi
import rx.Observable
import rx.Subscriber
import rx.subscriptions.CompositeSubscription
import java.net.URLEncoder

class EntryInfoPresenter {

    private val subscriptions = CompositeSubscription()
    private var view: EntryInfoView? = null
    private lateinit var repository: EntryInfoRepository

    fun onCreate(entryInfoView: EntryInfoView, repository: EntryInfoRepository, url: String, context: Context) {
        this.view = entryInfoView
        this.repository = repository
        entryInfoView.initActionBar()
        subscriptions.add(repository.requestEntryInfoApi(context, URLEncoder.encode(url, "utf-8"))
                .filter { !it.isNullObject() }
                .subscribe ({ entryInfo ->
                    val view = view ?: return@subscribe
                    view.setEntryInfo(entryInfo)
                    val commentList = entryInfo.bookmarkList.filter { it.hasComment() }
                    view.setBookmarkFragments(entryInfo.bookmarkList, commentList, entryInfo.entryId)
                    view.setCommentCount(commentList.count().toString())
                    if (repository.isLogin()) {
                        view.setRegisterBookmarkFragment(entryInfo.url)
                    }
                    view.setViewPagerSettings(currentItem = 1, offscreenPageLimit = 2)
                }, {
                    it.printStackTrace()
                    view?.showNetworkErrorToast()
                })
        )
    }

    fun onDestroy() {
        subscriptions.unsubscribe()
        view = null
    }

}