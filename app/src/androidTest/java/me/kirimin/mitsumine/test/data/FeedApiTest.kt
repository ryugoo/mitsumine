package me.kirimin.mitsumine.test.data

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import me.kirimin.mitsumine.common.network.FeedApi
import me.kirimin.mitsumine.common.domain.enums.Category
import me.kirimin.mitsumine.common.domain.enums.Type

import org.hamcrest.Matchers.not
import org.junit.Assert.*;
import rx.observers.TestSubscriber

@RunWith(AndroidJUnit4::class)
public class FeedApiTest {

    @Test
    public fun requestCategoryはFeedを取得できる() {
        val testSubscriber = TestSubscriber<Int>()
        FeedApi.requestCategory(InstrumentationRegistry.getContext(), Category.MAIN, Type.HOT)
                .count()
                .subscribe(testSubscriber)
        testSubscriber.assertNoErrors()
        assertThat(testSubscriber.onNextEvents.get(0), not(0))
    }

    @Test
    public fun requestKeywordはFeedを取得できる() {
        val testSubscriber = TestSubscriber<Int>()
        FeedApi.requestKeyword(InstrumentationRegistry.getContext(), "java")
                .count()
                .subscribe(testSubscriber)
        testSubscriber.assertNoErrors()
        assertThat(testSubscriber.onNextEvents.get(0), not(0))

    }

    @Test
    public fun requestUserBookmarkはFeedを取得できる() {
        val testSubscriber = TestSubscriber<Int>()
        FeedApi.requestUserBookmark(InstrumentationRegistry.getContext(), "kirimin")
                .count()
                .subscribe(testSubscriber)
        testSubscriber.assertNoErrors()
        assertThat(testSubscriber.onNextEvents.get(0), not(0))

    }
}
