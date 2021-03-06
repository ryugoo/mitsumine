package me.kirimin.mitsumine._common.network

import android.content.Context
import me.kirimin.mitsumine._common.domain.model.EntryInfo
import me.kirimin.mitsumine._common.network.parser.EntryInfoApiParser

import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

object EntryInfoApi {

    private val REQUEST_URL = "http://b.hatena.ne.jp/entry/jsonlite/?url="

    fun request(context: Context, url: String): Observable<EntryInfo> {
        return ApiAccessor.request(context, REQUEST_URL + url)
                .map { EntryInfoApiParser.parseResponse(it) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())

    }
}