package me.kirimin.mitsumine.bookmarklist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import me.kirimin.mitsumine.R
import me.kirimin.mitsumine._common.domain.model.Bookmark
import me.kirimin.mitsumine.search.AbstractSearchActivity
import me.kirimin.mitsumine.feed.user.UserSearchActivity

import kotlinx.android.synthetic.main.fragment_bookmark_list.view.*
import java.net.URL

class BookmarkListFragment : Fragment(), BookmarkListView {

    val presenter = BookmarkListPresenter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_bookmark_list, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.onCreate(this, arguments.getParcelableArray("bookmarkList").map { it as Bookmark })
    }

    override fun initViews(bookmarks: List<Bookmark>) {
        val view = view ?: return
        val adapter = BookmarkListAdapter(activity, presenter, arguments.getString("entryId"))
        adapter.addAll(bookmarks)
        view.listView.adapter = adapter
    }

    override fun startUserSearchActivity(userId: String) {
        val intent = Intent(activity, UserSearchActivity::class.java)
        intent.putExtras(AbstractSearchActivity.buildBundle(userId))
        startActivity(intent)
    }

    override fun shareCommentLink(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(intent)
    }

    override fun showBrowser(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    companion object {
        fun newFragment(bookmarkList: List<Bookmark>, entryId: String): BookmarkListFragment {
            val fragment = BookmarkListFragment()
            val bundle = Bundle()
            bundle.putParcelableArray("bookmarkList", bookmarkList.toTypedArray())
            bundle.putString("entryId", entryId)
            fragment.arguments = bundle
            return fragment
        }
    }
}
